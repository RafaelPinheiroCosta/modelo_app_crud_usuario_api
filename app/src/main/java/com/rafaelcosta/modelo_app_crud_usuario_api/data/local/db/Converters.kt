package com.rafaelcosta.modelo_app_crud_usuario_api.data.local.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String>? =
        value?.takeIf { it.isNotBlank() }?.split("|")

    @TypeConverter
    fun listToString(list: List<String>?): String? =
        list?.joinToString("|")
}
