package com.rafaelcosta.modelo_app_crud_usuario_api.di

import android.content.Context
import com.google.gson.Gson
import com.rafaelcosta.modelo_app_crud_usuario_api.data.local.TokenStore
import com.rafaelcosta.modelo_app_crud_usuario_api.data.remote.AuthApiService
import com.rafaelcosta.modelo_app_crud_usuario_api.data.remote.RefreshRequest
import com.rafaelcosta.modelo_app_crud_usuario_api.data.remote.UsuarioApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("baseUrl")
    fun provideBaseUrl(): String = "http://10.83.131.30:8080/"

    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenStore: TokenStore): Interceptor = Interceptor { chain ->
        val original = chain.request()
        val token = tokenStore.getAccessTokenSync()
        val newRequest = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else original
        chain.proceed(newRequest)
    }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenStore: TokenStore,
        @Named("auth") authRetrofit: Retrofit
    ): Authenticator = Authenticator { _, response ->
        if (response.request.url.encodedPath.contains("/auth/refresh") || responseCount(response) >= 2)
            return@Authenticator null

        val refresh = tokenStore.getRefreshTokenSync() ?: return@Authenticator null
        val service = authRetrofit.create(AuthApiService::class.java)

        android.util.Log.d("Auth", "Tentando refresh do token...")

        val refreshed = try {
            val resp = service.refreshCall(RefreshRequest(refresh)).execute()
            if (resp.isSuccessful) resp.body() else null
        } catch (_: Exception) { null }

        val newAccess = refreshed?.accessToken ?: return@Authenticator null

        kotlinx.coroutines.runBlocking {
            tokenStore.saveAccessToken(newAccess)
            refreshed.refreshToken?.let { tokenStore.saveRefreshToken(it) }
        }

        response.request.newBuilder()
            .header("Authorization", "Bearer $newAccess")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    @Provides
    @Singleton
    @Named("authClient")
    fun provideAuthClient(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("secureClient")
    fun provideSecureClient(
        logging: HttpLoggingInterceptor,
        authInterceptor: Interceptor,
        authenticator: Authenticator
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .authenticator(authenticator)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthRetrofit(
        @Named("baseUrl") baseUrl: String,
        @Named("authClient") client: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("secure")
    fun provideSecureRetrofit(
        @Named("baseUrl") baseUrl: String,
        @Named("secureClient") client: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("authApi")
    fun provideAuthApi(@Named("auth") retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    @Named("secureApi")
    fun provideSecureApi(@Named("secure") retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideUsuarioApi(@Named("secure") retrofit: Retrofit): UsuarioApi =
        retrofit.create(UsuarioApi::class.java)

    @Provides
    @Singleton
    fun provideTokenStore(@ApplicationContext context: Context): TokenStore = TokenStore(context)

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}
