package com.rafaelcosta.modelo_app_crud_usuario_api.data.remote.dto

data class UsuarioDto(
    val id: String,
    val nome: String,
    val email: String,
    val cpf: String,
    val senha: String?,
    val fotoPerfilUrl: String?,
    val fotoLocalPath: String?,
    val anexos: List<String>?,
    val updatedAt: Long
)