package com.rafaelcosta.modelo_app_crud_usuario_api.data.local

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.rafaelcosta.modelo_app_crud_usuario_api.data.remote.MeResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenStore(
    private val context: Context,
    private val gson: Gson = Gson()
) {

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val ACCESS_EXP   = longPreferencesKey("access_exp")
        private val REFRESH_EXP  = longPreferencesKey("refresh_exp")
        private val ME_CACHED    = stringPreferencesKey("me_cached_json")
    }
    val token: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN] }

    suspend fun saveTokens(access: String, refresh: String?) {
        val accessExp = extractExp(access)
        val refreshExp = refresh?.let { extractExp(it) }
        context.dataStore.edit {
            it[ACCESS_TOKEN] = access
            accessExp?.let { exp -> it[ACCESS_EXP] = exp }
            if (!refresh.isNullOrBlank()) {
                it[REFRESH_TOKEN] = refresh
                refreshExp?.let { exp -> it[REFRESH_EXP] = exp }
            }
        }
    }

    suspend fun saveAccessToken(access: String) {
        val exp = extractExp(access)
        context.dataStore.edit {
            it[ACCESS_TOKEN] = access
            exp?.let { e -> it[ACCESS_EXP] = e }
        }
    }

    suspend fun saveRefreshToken(refresh: String) {
        val exp = extractExp(refresh)
        context.dataStore.edit {
            it[REFRESH_TOKEN] = refresh
            exp?.let { e -> it[REFRESH_EXP] = e }
        }
    }

    suspend fun clearTokens() {
        context.dataStore.edit {
            it.remove(ACCESS_TOKEN); it.remove(REFRESH_TOKEN)
            it.remove(ACCESS_EXP);   it.remove(REFRESH_EXP)
            it.remove(ME_CACHED)
        }
    }

    suspend fun getAccessToken(): String? =
        context.dataStore.data.first()[ACCESS_TOKEN]

    suspend fun getRefreshToken(): String? =
        context.dataStore.data.first()[REFRESH_TOKEN]

    suspend fun isAccessValidNow(leewaySeconds: Long = 20): Boolean {
        val now = epochSeconds()
        val exp = context.dataStore.data.first()[ACCESS_EXP] ?: return false
        return now + leewaySeconds < exp
    }

    suspend fun isRefreshValidNow(leewaySeconds: Long = 20): Boolean {
        val refresh = context.dataStore.data.first()[REFRESH_TOKEN]
        if (refresh.isNullOrBlank()) return false
        val exp = context.dataStore.data.first()[REFRESH_EXP] ?: extractExp(refresh) ?: return false
        return epochSeconds() + leewaySeconds < exp
    }

    suspend fun saveMeCached(me: MeResponse) {
        context.dataStore.edit { it[ME_CACHED] = gson.toJson(me) }
    }
    suspend fun getMeCached(): MeResponse? {
        val json = context.dataStore.data.first()[ME_CACHED] ?: return null
        return runCatching { gson.fromJson(json, MeResponse::class.java) }.getOrNull()
    }

    fun saveRefreshTokenSync(refresh: String) = runBlocking { saveRefreshToken(refresh) }
    fun getAccessTokenSync(): String? = runBlocking { getAccessToken() }
    fun getRefreshTokenSync(): String? = runBlocking { getRefreshToken() }

    private fun epochSeconds(): Long = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())

    private fun extractExp(jwt: String?): Long? {
        if (jwt.isNullOrBlank()) return null
        val parts = jwt.split(".")
        if (parts.size < 2) return null
        val payload = try {
            val decoded = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP)
            String(decoded, Charset.forName("UTF-8"))
        } catch (e: Exception) {
            return null
        }
        return runCatching {
            val map = gson.fromJson(payload, Map::class.java)
            val expNum = (map["exp"] as? Number)?.toLong()
            expNum
        }.getOrNull()
    }
}
