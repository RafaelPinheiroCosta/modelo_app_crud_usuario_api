package com.rafaelcosta.modelo_app_crud_usuario_api.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rafaelcosta.modelo_app_crud_usuario_api.data.worker.UsuarioSyncWorker

object NetworkMonitor {

    private var isRegistered = false

    fun register(context: Context) {
        if (isRegistered) return
        isRegistered = true

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.i("NetworkMonitor", "Rede disponível — forçando sincronização imediata")

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val work = OneTimeWorkRequestBuilder<UsuarioSyncWorker>()
                    .setConstraints(constraints)
                    .addTag("sync_imediata")
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "usuario_sync_imediata",
                    ExistingWorkPolicy.REPLACE,
                    work
                )
            }

            override fun onLost(network: Network) {
                Log.w("NetworkMonitor", "Rede perdida")
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)
    }
}
