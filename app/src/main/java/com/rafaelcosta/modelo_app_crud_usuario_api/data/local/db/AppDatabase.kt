package com.rafaelcosta.modelo_app_crud_usuario_api.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rafaelcosta.modelo_app_crud_usuario_api.data.local.dao.UsuarioDao
import com.rafaelcosta.modelo_app_crud_usuario_api.data.local.entity.UsuarioEntity


@Database(entities = [UsuarioEntity::class], version = 5)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
}
