package com.rafaelcosta.modelo_app_crud_usuario_api.ui.feature.usuario.edit

import android.net.Uri

data class UsuarioEditUiState(
    val nome: String = "",
    val email: String = "",
    val cpf: String = "",
    val senha: String = "",
    val isNew: Boolean = true,
    val loading: Boolean = false,
    val error: String? = null,
    val fotoUri: Uri? = null,
    val anexosUris: List<Uri> = emptyList()
)