package com.rafaelcosta.modelo_app_crud_usuario_api.data.worker

import android.content.Context
import android.widget.Toast
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rafaelcosta.modelo_app_crud_usuario_api.data.repository.UsuarioRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class UsuarioSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: UsuarioRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        android.util.Log.i("UsuarioSyncWorker", "Executando sincronização...")
        return try {
            repository.sincronizarUsuarios()

            android.util.Log.i("UsuarioSyncWorker", "Sincronização concluída com sucesso.")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("UsuarioSyncWorker", "Erro na sincronização", e)
            Result.retry()
        }

    }

}
