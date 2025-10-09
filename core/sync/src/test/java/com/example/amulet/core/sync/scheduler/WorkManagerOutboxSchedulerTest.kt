package com.example.amulet.core.sync.scheduler

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.amulet.core.sync.worker.OutboxWorker
import io.mockk.CapturingSlot
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class WorkManagerOutboxSchedulerTest {

    private val workManager = mockk<WorkManager>(relaxed = true)
    private val scheduler = WorkManagerOutboxScheduler(workManager)

    @Test
    fun `enqueues unique work with keep policy`() {
        val requestSlot: CapturingSlot<OneTimeWorkRequest> = slot()

        scheduler.scheduleSync(expedited = false)

        verify {
            workManager.enqueueUniqueWork(
                OutboxWorker.UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                capture(requestSlot)
            )
        }
        val request = requestSlot.captured
        assertEquals(OutboxWorker::class.qualifiedName, request.workSpec.workerClassName)
        assertFalse(request.workSpec.expedited)
    }

    @Test
    fun `expedited flag builds expedited request`() {
        val requestSlot: CapturingSlot<OneTimeWorkRequest> = slot()

        scheduler.scheduleSync(expedited = true)

        verify {
            workManager.enqueueUniqueWork(
                OutboxWorker.UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                capture(requestSlot)
            )
        }
        val request = requestSlot.captured
        // OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST still marks workSpec as expedited
        assertEquals(true, request.workSpec.expedited)
    }
}
