package com.example.amulet.core.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class OutboxWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val runner: OutboxWorkerRunner
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result = runner.run()

    companion object {
        const val UNIQUE_WORK_NAME = "outbox_sync"

        fun createWorkRequest(expedited: Boolean = false): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val builder = OneTimeWorkRequestBuilder<OutboxWorker>()
                .setConstraints(constraints)

            if (expedited) {
                builder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }

            return builder.build()
        }
    }
}
