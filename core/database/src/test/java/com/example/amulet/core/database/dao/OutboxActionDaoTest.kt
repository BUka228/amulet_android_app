package com.example.amulet.core.database.dao

import com.example.amulet.core.database.BaseDatabaseTest
import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.OutboxActionStatus
import com.example.amulet.core.database.entity.OutboxActionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OutboxActionDaoTest : BaseDatabaseTest() {

    private lateinit var outboxActionDao: OutboxActionDao

    @Before
    fun prepare() {
        outboxActionDao = database.outboxActionDao()
    }

    @Test
    fun `findDue returns pending actions ordered by priority`() = runTest {
        val actions = listOf(
            newAction(id = "1", priority = 10, availableAt = 20L),
            newAction(id = "2", priority = 100, availableAt = 10L),
            newAction(id = "3", priority = 50, availableAt = 5L, status = OutboxActionStatus.FAILED)
        )
        outboxActionDao.upsert(actions)

        val due = outboxActionDao.findDue(now = 20L, limit = 10)
        assertEquals(listOf("2", "3", "1"), due.map(OutboxActionEntity::id))
    }

    @Test
    fun `observeForEntity excludes completed`() = runTest {
        val entityId = "pattern-1"
        val pending = newAction(id = "pending", targetEntityId = entityId)
        val completed = newAction(id = "completed", targetEntityId = entityId, status = OutboxActionStatus.COMPLETED)
        outboxActionDao.upsert(listOf(pending, completed))

        val observed = outboxActionDao.observeForEntity(entityId, OutboxActionStatus.COMPLETED).first()
        assertEquals(listOf(pending), observed)
    }

    @Test
    fun `cleanup removes completed and failed actions`() = runTest {
        val completed = newAction(id = "completed", status = OutboxActionStatus.COMPLETED, createdAt = 0L, updatedAt = 0L)
        val failed = newAction(id = "failed", status = OutboxActionStatus.FAILED, createdAt = 100L, updatedAt = 0L)
        outboxActionDao.upsert(listOf(completed, failed))

        val removedCompleted = outboxActionDao.cleanupCompleted(cutoffTime = 50L)
        val removedFailed = outboxActionDao.cleanupFailed(cutoffTime = 10L)

        assertEquals(1, removedCompleted)
        assertEquals(1, removedFailed)
        val remaining = outboxActionDao.findDue(now = Long.MAX_VALUE, limit = 10)
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun `updateStatus updates retry count and last error`() = runTest {
        val action = newAction(id = "action")
        outboxActionDao.upsert(action)

        outboxActionDao.updateStatus(
            id = action.id,
            status = OutboxActionStatus.FAILED,
            retryCount = 1,
            lastError = "timeout",
            updatedAt = 200L,
            availableAt = 500L
        )

        val updated = outboxActionDao.getById(action.id)
        requireNotNull(updated)
        assertEquals(OutboxActionStatus.FAILED, updated.status)
        assertEquals(1, updated.retryCount)
        assertEquals("timeout", updated.lastError)
        assertEquals(200L, updated.updatedAt)
        assertEquals(500L, updated.availableAt)
    }

    private fun newAction(
        id: String,
        priority: Int = 50,
        availableAt: Long = 0L,
        status: OutboxActionStatus = OutboxActionStatus.PENDING,
        createdAt: Long = 0L,
        updatedAt: Long = 0L,
        targetEntityId: String? = null
    ) = OutboxActionEntity(
        id = id,
        type = OutboxActionType.PATTERN_CREATE,
        payloadJson = "{}",
        status = status,
        retryCount = 0,
        lastError = null,
        idempotencyKey = null,
        createdAt = createdAt,
        updatedAt = updatedAt,
        availableAt = availableAt,
        priority = priority,
        targetEntityId = targetEntityId
    )
}
