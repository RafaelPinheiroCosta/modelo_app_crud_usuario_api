package com.rafaelcosta.modelo_app_crud_usuario_api.data.worker

import android.content.Context

object UsuarioSyncScheduler {
    fun enqueueNow(context: Context) {
        val req = androidx.work.OneTimeWorkRequestBuilder<UsuarioSyncWorker>()
            .setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()
            )
            .build()
        androidx.work.WorkManager.getInstance(context).enqueue(req)
    }
}
