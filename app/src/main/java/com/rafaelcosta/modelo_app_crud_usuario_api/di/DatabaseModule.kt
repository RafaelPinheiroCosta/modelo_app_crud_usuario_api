package com.rafaelcosta.modelo_app_crud_usuario_api.di

import android.content.Context
import androidx.room.Room
import com.rafaelcosta.modelo_app_crud_usuario_api.data.local.dao.UsuarioDao
import com.rafaelcosta.modelo_app_crud_usuario_api.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "usuarios.db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideUsuarioDao(db: AppDatabase): UsuarioDao = db.usuarioDao()
}
