package com.rafaelcosta.app_modelo_login_jwt.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelcosta.modelo_app_crud_usuario_api.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val meCachedNome: String?, val meCachedEmail: String?) : AuthState
}

@HiltViewModel
class AuthStateViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state

    init {
        viewModelScope.launch {
            val me = repo.bootstrapSession()
            if (me != null) {
                _state.value = AuthState.Authenticated(me.nome, me.email)
            } else {
                _state.value = AuthState.Unauthenticated
            }
        }
    }
}
