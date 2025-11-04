package com.rafaelcosta.modelo_app_crud_usuario_api

import android.app.Application
import android.widget.Toast
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.rafaelcosta.modelo_app_crud_usuario_api.data.network.NetworkMonitor
import com.rafaelcosta.modelo_app_crud_usuario_api.data.worker.UsuarioSyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()

        try {
            WorkManager.initialize(this, workManagerConfiguration)
            android.util.Log.i("App", "WorkManager inicializado manualmente com HiltWorkerFactory")
        } catch (e: IllegalStateException) {
            android.util.Log.i("App", "WorkManager já inicializado: ${e.message}")
        }

        NetworkMonitor.register(this)

        agendarSincronizacaoPeriodica()

    }

    private fun agendarSincronizacaoPeriodica() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<UsuarioSyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "usuario_sync_periodica",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )

        Toast.makeText(this, "Sincronização agendada (a cada 15 min).", Toast.LENGTH_SHORT).show()
    }
}
