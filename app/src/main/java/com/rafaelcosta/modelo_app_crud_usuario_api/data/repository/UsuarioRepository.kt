package com.rafaelcosta.modelo_app_crud_usuario_api.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.google.gson.Gson
import com.rafaelcosta.modelo_app_crud_usuario_api.data.local.dao.UsuarioDao
import com.rafaelcosta.modelo_app_crud_usuario_api.data.local.entity.UsuarioEntity
import com.rafaelcosta.modelo_app_crud_usuario_api.data.mapper.toDomain
import com.rafaelcosta.modelo_app_crud_usuario_api.data.mapper.toEntity
import com.rafaelcosta.modelo_app_crud_usuario_api.data.remote.UsuarioApi
import com.rafaelcosta.modelo_app_crud_usuario_api.data.worker.UsuarioSyncScheduler
import com.rafaelcosta.modelo_app_crud_usuario_api.di.IoDispatcher
import com.rafaelcosta.modelo_app_crud_usuario_api.domain.model.Usuario
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsuarioRepository @Inject constructor(
    private val api: UsuarioApi,
    private val dao: UsuarioDao,
    @IoDispatcher private val io: CoroutineDispatcher,
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {

    private val jsonMedia = "application/json".toMediaType()

    private fun partJsonDados(dados: Any): RequestBody =
        gson.toJson(dados).toRequestBody(jsonMedia)

    private fun partFromUri(fieldName: String, uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null
        val cr: ContentResolver = context.contentResolver
        val type = cr.getType(uri) ?: "application/octet-stream"
        val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "arquivo"
        val input = cr.openInputStream(uri) ?: return null
        val tmp = File.createTempFile("up_", "_tmp", context.cacheDir)
        tmp.outputStream().use { out -> input.copyTo(out) }
        val body = tmp.asRequestBody(type.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, fileName, body)
    }

    private fun partsFromUris(uris: List<Uri>?): List<MultipartBody.Part>? {
        if (uris.isNullOrEmpty()) return null
        return uris.mapNotNull { partFromUri("anexos", it) }
    }

    fun observeUsuarios(): Flow<List<Usuario>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeUsuario(id: String): Flow<Usuario?> =
        dao.observeById(id).map { it?.toDomain() }

    suspend fun refresh(): Result<Unit> = runCatching {
        val remote = api.list()
        val current = dao.getAll().associateBy { it.id }

        val merged = remote.map { dto ->
            val old = current[dto.id]
            if (old?.deleted == true) old else dto.toEntity(pending = false, oldLocalPath = old?.fotoLocalPath)
        }

        dao.upsertAll(merged)

        val remoteIds = merged.map { it.id }.toSet()
        val toDelete = current.values.filter { it.id !in remoteIds && !it.pendingSync && !it.localOnly }
        toDelete.forEach { dao.deleteById(it.id) }
    }


    suspend fun create(
        nome: String,
        email: String,
        cpf: String,
        senha: String,
        fotoUri: Uri?,
        anexosUris: List<Uri>?
    ): Usuario {
        return withContext(io) {

            val tempId = "local-${System.currentTimeMillis()}"
            val localUsuario = UsuarioEntity(
                id = tempId,
                nome = nome,
                email = email,
                cpf = cpf,
                senha = senha,
                fotoPerfilUrl = fotoUri?.toString(),
                fotoLocalPath = saveLocalCopy(fotoUri),
                anexos = anexosUris?.map { it.toString() },
                updatedAt = System.currentTimeMillis(),
                pendingSync = true,
                localOnly = true,
                deleted = false,
                operationType = "CREATE"
            )

            dao.upsert(localUsuario)

            UsuarioSyncScheduler.enqueueNow(context)

            localUsuario.toDomain()
        }
    }

    suspend fun update(
        id: String,
        nome: String,
        email: String,
        cpf: String,
        senha: String?,
        fotoUri: Uri?,
        anexosUris: List<Uri>?
    ): Usuario {
        return withContext(io) {
            val local = dao.getById(id) ?: throw IllegalArgumentException("Usuário não encontrado")
            val updated = local.copy(
                nome = nome,
                email = email,
                cpf = cpf,
                senha = senha ?: local.senha,
                fotoPerfilUrl = fotoUri?.toString() ?: local.fotoPerfilUrl,
                fotoLocalPath = fotoUri?.let { saveLocalCopy(it) } ?: local.fotoLocalPath,
                anexos = anexosUris?.map { it.toString() } ?: local.anexos,
                updatedAt = System.currentTimeMillis(),
                pendingSync = true,
                localOnly = local.localOnly,
                deleted = false,
                operationType = "UPDATE"
            )

            dao.upsert(updated)
            UsuarioSyncScheduler.enqueueNow(context)
            updated.toDomain()
        }
    }


    suspend fun delete(id: String): Result<Unit> = runCatching {
        val local = dao.getById(id) ?: return@runCatching
        dao.upsert(
            local.copy(
                deleted = true,
                pendingSync = true,
                updatedAt = System.currentTimeMillis(),
                operationType = "DELETE"
            )
        )
        UsuarioSyncScheduler.enqueueNow(context)
    }

    suspend fun sincronizarUsuarios() {
        val pendentes = dao.getPendingSync()

        pendentes.filter { it.operationType == "DELETE" }.forEach { u ->
            try {
                runCatching { api.delete(u.id) }
                dao.deleteById(u.id)
            } catch (e: Exception) {
            }
        }

        pendentes.filter { it.operationType == "CREATE" && !it.deleted }.forEach { u ->
            try {
                val dados = mapOf("nome" to u.nome, "email" to u.email, "cpf" to u.cpf, "senha" to u.senha)
                val resp = api.create(
                    dadosJson = partJsonDados(dados),
                    foto = partFromUri("foto", u.fotoPerfilUrl?.toUri()),
                    anexos = partsFromUris(u.anexos?.mapNotNull { it.toUri() })
                )
                dao.deleteById(u.id)
                dao.upsert(resp.toEntity(pending = false, oldLocalPath = u.fotoLocalPath))
            } catch (_: Exception) {
            }
        }

        pendentes.filter { it.operationType == "UPDATE" && !it.deleted }.forEach { u ->
            try {
                val dados = buildMap<String, Any> {
                    put("nome", u.nome)
                    put("email", u.email)
                    put("cpf", u.cpf)
                    u.senha?.takeIf { it.isNotBlank() }?.let { put("senha", it) }
                }

                val resp = api.update(
                    id = u.id,
                    dadosJson = partJsonDados(dados),
                    foto = partFromUri("foto", u.fotoPerfilUrl?.toUri()),
                    anexos = partsFromUris(u.anexos?.mapNotNull { it.toUri() })
                )

                dao.upsert(
                    resp.toEntity(
                        pending = false,
                        oldLocalPath = u.fotoLocalPath
                    ).copy(
                        updatedAt = System.currentTimeMillis(),
                        pendingSync = false,
                        localOnly = false,
                        operationType = null
                    )
                )

            } catch (e: Exception) {
                android.util.Log.w("UsuarioRepository", "Falha ao sincronizar UPDATE ${u.id}: ${e.message}")
            }
        }

        try {
            val listaApi = api.list()
            val atuais = dao.getAll().associateBy { it.id }

            val remotos = listaApi.map { dto ->
                val antigo = atuais[dto.id]

                // 1️⃣ se foi deletado localmente, não ressuscita
                if (antigo?.deleted == true) return@map antigo

                val remoto = dto.toEntity(pending = false, oldLocalPath = antigo?.fotoLocalPath)

                // 2️⃣ se o local tem pendingSync, ele é mais novo → mantém local
                if (antigo?.pendingSync == true) return@map antigo

                // 3️⃣ se o local tem updatedAt mais recente, mantém local
                if (antigo != null && antigo.updatedAt > remoto.updatedAt) return@map antigo

                // 4️⃣ caso contrário, aceita o remoto (API)
                remoto
            }

            dao.upsertAll(remotos)

            val idsRemotos = remotos.map { it.id }.toSet()
            val locais = dao.getAll()
            locais.filter { local ->
                local.id !in idsRemotos && !local.pendingSync && !local.localOnly
            }.forEach { dao.deleteById(it.id) }

        } catch (e: Exception) {
            android.util.Log.w("UsuarioRepository", "Sem conexão no pull: ${e.message}")
        }
    }

    @Suppress("unused")
    suspend fun syncAll(): Result<Unit> = runCatching {
        val pendentes = dao.getPendingSync()

        for (e in pendentes) {
            try {
                if (e.localOnly) {
                    val dados = mapOf(
                        "nome" to e.nome,
                        "email" to e.email,
                        "cpf" to e.cpf,
                        "senha" to e.senha
                    )
                    val resp = api.create(
                        dadosJson = partJsonDados(dados),
                        foto = partFromUri("foto", e.fotoPerfilUrl?.toUri()),
                        anexos = partsFromUris(e.anexos?.map { it.toUri() })
                    )
                    dao.deleteById(e.id)
                    dao.upsert(resp.toEntity(pending = false))
                } else {
                    val dados = buildMap<String, Any> {
                        put("nome", e.nome)
                        put("email", e.email)
                        put("cpf", e.cpf)
                        e.senha?.let { put("senha", it) }
                    }
                    val resp = api.update(
                        id = e.id,
                        dadosJson = partJsonDados(dados),
                        foto = partFromUri("foto", e.fotoPerfilUrl?.toUri()),
                        anexos = partsFromUris(e.anexos?.map { it.toUri() })
                    )
                    dao.upsert(resp.toEntity(pending = false))
                }
            } catch (_: Exception) {
            }
        }
        refresh()
    }

    @Suppress("unused")
    private suspend fun tryPushOne(id: String) {
        val e = dao.getById(id) ?: return

        val dados = buildMap<String, Any> {
            put("nome", e.nome)
            put("email", e.email)
            put("cpf", e.cpf)
            e.senha?.takeIf { it.isNotBlank() }?.let { put("senha", it) }
        }

        val fotoUri = e.fotoPerfilUrl?.toUri()
        val anexosUris = e.anexos?.mapNotNull { runCatching { it.toUri() }.getOrNull() }

        val pushed = if (existsRemote(id)) {
            api.update(
                id = id,
                dadosJson = partJsonDados(dados),
                foto = partFromUri("foto", fotoUri),
                anexos = partsFromUris(anexosUris)
            )
        } else {
            api.create(
                dadosJson = partJsonDados(dados),
                foto = partFromUri("foto", fotoUri),
                anexos = partsFromUris(anexosUris)
            )
        }

        dao.upsert(pushed.toEntity(pending = false).copy(senha = null))
    }

    private suspend fun existsRemote(id: String): Boolean = runCatching {
        api.get(id); true
    }.getOrDefault(false)

    private fun saveLocalCopy(uri: Uri?): String? {
        if (uri == null) return null
        return try {
            val cr = context.contentResolver
            val input = cr.openInputStream(uri) ?: return null
            val fotosDir = File(context.filesDir, "fotos").apply { mkdirs() }
            val destFile = File(fotosDir, "foto_${System.currentTimeMillis()}.jpg")
            input.use { src -> destFile.outputStream().use { dst -> src.copyTo(dst) } }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

}
