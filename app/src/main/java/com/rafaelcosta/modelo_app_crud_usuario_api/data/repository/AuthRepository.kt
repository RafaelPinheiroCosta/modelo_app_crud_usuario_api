package com.rafaelcosta.modelo_app_crud_usuario_api.data.repository

import com.rafaelcosta.modelo_app_crud_usuario_api.data.local.TokenStore
import com.rafaelcosta.modelo_app_crud_usuario_api.data.remote.AuthApiService
import com.rafaelcosta.modelo_app_crud_usuario_api.data.remote.LoginRequest
import com.rafaelcosta.modelo_app_crud_usuario_api.data.remote.MeResponse
import com.rafaelcosta.modelo_app_crud_usuario_api.data.remote.RefreshRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
@Singleton
class AuthRepository @Inject constructor(
    @Named("authApi") private val authApi: AuthApiService,
    @Named("secureApi") private val secureApi: AuthApiService,
    private val store: TokenStore
) {

    suspend fun login(email: String, senha: String): Boolean {
        val resp = authApi.login(LoginRequest(email, senha))
        store.saveTokens(resp.accessToken, resp.refreshToken)

        val me = secureApi.me()
        store.saveMeCached(me)
        return true
    }

    suspend fun bootstrapSession(): MeResponse? {
        return if (store.isRefreshValidNow()) {
            store.getMeCached()
        } else null
    }

    suspend fun ensureFreshAccess(): Boolean {
        if (store.isAccessValidNow()) return true
        val refresh = store.getRefreshToken()
        if (refresh.isNullOrBlank()) return false
        if (!store.isRefreshValidNow()) return false
        return runCatching {
            val rt = authApi.refresh(RefreshRequest(refresh))
            store.saveTokens(rt.accessToken, rt.refreshToken ?: refresh)
            true
        }.getOrDefault(false)
    }

    suspend fun meAndCache(): MeResponse {
        val me = secureApi.me()
        store.saveMeCached(me)
        return me
    }

    suspend fun logout() = store.clearTokens()

    fun tokenFlow(): Flow<String?> = store.token

    suspend fun ensureFreshAccessLocallyOnly(): Boolean {
        return store.isRefreshValidNow()
    }

}
