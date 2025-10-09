package com.example.amulet.core.sync.scheduler

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.example.amulet.core.sync.worker.OutboxWorker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerOutboxScheduler @Inject constructor(
    private val workManager: WorkManager
) : OutboxScheduler {

    override fun scheduleSync(expedited: Boolean) {
        val request = OutboxWorker.createWorkRequest(expedited = expedited)
        workManager.enqueueUniqueWork(
            OutboxWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}
