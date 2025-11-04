package com.rafaelcosta.app_modelo_login_jwt.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelcosta.modelo_app_crud_usuario_api.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun onEmailChange(v: String) = _state.update { it.copy(email = v) }
    fun onSenhaChange(v: String) = _state.update { it.copy(senha = v) }

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val ok = repo.login(_state.value.email, _state.value.senha)
                if (ok) onSuccess()
                else _state.update { it.copy(loading = false, error = "Falha no login") }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message ?: "Erro no login") }
            }
        }
    }
}
