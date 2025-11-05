package com.rafaelcosta.modelo_app_crud_usuario_api.data.local.dao

import androidx.room.*
import com.rafaelcosta.modelo_app_crud_usuario_api.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE deleted = 0 ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE id = :id AND deleted = 0")
    fun observeById(id: String): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun getById(id: String): UsuarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(usuario: UsuarioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(usuarios: List<UsuarioEntity>)

    @Delete
    suspend fun delete(usuario: UsuarioEntity)

    @Query("DELETE FROM usuarios WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM usuarios WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<UsuarioEntity>

    @Query("SELECT id FROM usuarios")
    suspend fun getAllIds(): List<String>

    @Query("SELECT * FROM usuarios")
    suspend fun getAll(): List<UsuarioEntity>
}
