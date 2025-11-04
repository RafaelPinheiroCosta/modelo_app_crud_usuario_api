package com.rafaelcosta.modelo_app_crud_usuario_api.ui.feature.usuario.list

import com.rafaelcosta.modelo_app_crud_usuario_api.domain.model.Usuario

data class UsuariosListUiState(
    val items: List<Usuario> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)