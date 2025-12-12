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
import com.example.amulet.shared.core.logging.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@HiltWorker
class OutboxWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val runner: OutboxWorkerRunner
) : CoroutineWorker(appContext, workerParameters) {

    // Вторичный конструктор для случая, когда используется дефолтный WorkerFactory
    constructor(appContext: Context, workerParameters: WorkerParameters) : this(
        appContext,
        workerParameters,
        EntryPointAccessors.fromApplication(
            appContext,
            OutboxWorkerEntryPoint::class.java
        ).outboxWorkerRunner()
    )

    override suspend fun doWork(): Result {
        Logger.d("OutboxWorker doWork started", TAG)
        val result = runner.run()
        Logger.d("OutboxWorker doWork finished with result=$result", TAG)
        return result
    }

    companion object {
        private const val TAG = "OutboxWorker"

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

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface OutboxWorkerEntryPoint {
        fun outboxWorkerRunner(): OutboxWorkerRunner
    }
}
