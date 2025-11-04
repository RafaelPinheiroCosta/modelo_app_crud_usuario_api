package com.rafaelcosta.modelo_app_crud_usuario_api.data.mapper

import com.rafaelcosta.modelo_app_crud_usuario_api.data.local.entity.UsuarioEntity
import com.rafaelcosta.modelo_app_crud_usuario_api.data.remote.dto.UsuarioDto
import com.rafaelcosta.modelo_app_crud_usuario_api.domain.model.Usuario

fun UsuarioDto.toEntity(
    pending: Boolean = false,
    oldLocalPath: String? = null
) = UsuarioEntity(
    id = id,
    nome = nome,
    email = email,
    cpf = cpf,
    senha = null,
    fotoPerfilUrl = fotoPerfilUrl,
    fotoLocalPath = oldLocalPath,
    anexos = anexos,
    updatedAt = updatedAt,
    pendingSync = pending
)

fun UsuarioDto.toDomain() = Usuario(
    id = id,
    nome = nome,
    email = email,
    cpf = cpf,
    senha = senha,
    fotoPerfilUrl = fotoPerfilUrl,
    fotoLocalPath = fotoLocalPath,
    anexos = anexos ?: emptyList(),
    updatedAt = updatedAt
)

fun UsuarioEntity.toDomain() = Usuario(
    id = id,
    nome = nome,
    email = email,
    cpf = cpf,
    senha = senha,
    fotoPerfilUrl = fotoPerfilUrl,
    fotoLocalPath = fotoLocalPath,
    anexos = anexos,
    updatedAt = updatedAt,
    pendingSync = pendingSync
)

fun Usuario.toDto() = UsuarioDto(
    id = id,
    nome = nome,
    email = email,
    cpf = cpf,
    senha = senha,
    fotoPerfilUrl = fotoPerfilUrl,
    fotoLocalPath = fotoLocalPath,
    anexos=anexos ?: emptyList(),
    updatedAt = updatedAt
)

fun Usuario.toEntity(pending: Boolean = false): UsuarioEntity = UsuarioEntity(
    id = id,
    nome = nome,
    email = email,
    cpf = cpf,
    senha = senha,
    fotoPerfilUrl = fotoPerfilUrl,
    fotoLocalPath = fotoLocalPath,
    anexos = anexos,
    updatedAt = updatedAt,
    pendingSync = pending
)