package com.rafaelcosta.modelo_app_crud_usuario_api.ui.feature.usuario.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelcosta.modelo_app_crud_usuario_api.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class UsuariosListViewModel @Inject constructor(
    private val repo: UsuarioRepository,
    @Named("baseUrl") private val baseUrl: String
) : ViewModel() {

    private val _state = MutableStateFlow(
        UsuariosListUiState(
            loading = true
        )
    )
    val state: StateFlow<UsuariosListUiState> = _state

    init {
        viewModelScope.launch {
            repo.observeUsuarios().collect { list ->
                _state.update { it.copy(items = list, loading = false) }
            }
        }

        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        repo.refresh()
            .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } }
            .onSuccess { _state.update { it.copy(loading = false) } }
    }

    fun delete(id: String) = viewModelScope.launch {
        repo.delete(id)
    }

    fun fotoUrl(relativeOrAbsolute: String?, localPath: String? = null): String? {
        if (!localPath.isNullOrBlank()) return localPath

        if (relativeOrAbsolute.isNullOrBlank()) return null
        val url = relativeOrAbsolute.trim()
        return if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            baseUrl.trimEnd('/') + "/" + url.trimStart('/')
        }
    }
}
