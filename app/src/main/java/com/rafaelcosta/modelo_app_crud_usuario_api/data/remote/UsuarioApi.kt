package com.rafaelcosta.modelo_app_crud_usuario_api.data.remote

import com.rafaelcosta.modelo_app_crud_usuario_api.data.remote.dto.UsuarioDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface UsuarioApi {
    @GET("usuarios")
    suspend fun list(): List<UsuarioDto>

    @GET("usuarios/{id}")
    suspend fun get(@Path("id") id: String): UsuarioDto

    @Multipart
    @POST("usuarios")
    suspend fun create(
        @Part("dados") dadosJson: RequestBody,
        @Part foto: MultipartBody.Part?,
        @Part anexos: List<MultipartBody.Part>?
    ): UsuarioDto

    @Multipart
    @PUT("usuarios/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Part("dados") dadosJson: RequestBody,
        @Part foto: MultipartBody.Part?,
        @Part anexos: List<MultipartBody.Part>?
    ): UsuarioDto

    @DELETE("usuarios/{id}")
    suspend fun delete(@Path("id") id: String)
}
