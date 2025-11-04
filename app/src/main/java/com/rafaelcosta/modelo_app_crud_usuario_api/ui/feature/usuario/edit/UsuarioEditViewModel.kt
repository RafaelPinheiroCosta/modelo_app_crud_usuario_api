package com.rafaelcosta.modelo_app_crud_usuario_api.ui.feature.usuario.edit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelcosta.modelo_app_crud_usuario_api.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UsuarioEditViewModel @Inject constructor(
    private val repo: UsuarioRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UsuarioEditUiState())
    val state: StateFlow<UsuarioEditUiState> = _state

    fun load(id: String?) = viewModelScope.launch {
        if (id == null) {
            _state.value = UsuarioEditUiState(isNew = true)
            return@launch
        }
        repo.observeUsuario(id).collect { u ->
            if (u != null) _state.value = UsuarioEditUiState(
                nome = u.nome,
                email = u.email,
                cpf = u.cpf,
                senha = "",
                isNew = false
            )
        }
    }

    fun onNome(v: String) {
        _state.update { it.copy(nome = v) }
    }

    fun onEmail(v: String) {
        _state.update { it.copy(email = v) }
    }

    fun onCpf(v: String) {
        _state.update { it.copy(cpf = v) }
    }

    fun onSenha(v: String) {
        _state.update { it.copy(senha = v) }
    }

    fun onFotoPicked(uri: Uri?) {
        _state.update { it.copy(fotoUri = uri) }
    }

    fun onAnexosPicked(uris: List<Uri>) {
        _state.update { it.copy(anexosUris = uris) }
    }

    fun save(usuarioId: String?, onDone: () -> Unit) = viewModelScope.launch {
        val s = _state.value
        _state.update { it.copy(loading = true, error = null) }

        try {
            if (usuarioId == null) {
                if (s.senha.isBlank()) {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = "Senha é obrigatória no cadastro"
                        )
                    }
                    return@launch
                }

                repo.create(
                    nome = s.nome, email = s.email, cpf = s.cpf, senha = s.senha,
                    fotoUri = s.fotoUri, anexosUris = s.anexosUris
                )
            } else {
                repo.update(
                    usuarioId, s.nome, s.email, s.cpf, s.senha.ifBlank { null },
                    fotoUri = s.fotoUri, anexosUris = s.anexosUris
                )
            }

            _state.update { it.copy(loading = false) }
            onDone()

        } catch (e: Exception) {
            _state.update { it.copy(loading = false, error = e.message ?: "Erro desconhecido") }
        }
    }

}