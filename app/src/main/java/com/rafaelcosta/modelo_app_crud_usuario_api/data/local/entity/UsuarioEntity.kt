package com.rafaelcosta.modelo_app_crud_usuario_api.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: String,
    val nome: String,
    val email: String,
    val cpf: String,
    val senha: String?,
    val fotoPerfilUrl: String?,
    val fotoLocalPath: String?,
    val anexos: List<String>?,
    val updatedAt: Long,
    val pendingSync: Boolean,
    val localOnly: Boolean = false,
    val operationType: String? = null,
    val deleted: Boolean = false
)