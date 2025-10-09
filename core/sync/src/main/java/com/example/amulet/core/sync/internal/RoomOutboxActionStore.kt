package com.example.amulet.core.sync.internal

import androidx.room.withTransaction
import com.example.amulet.core.database.AmuletDatabase
import com.example.amulet.core.database.dao.OutboxActionDao
import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.OutboxActionStatus
import javax.inject.Inject

class RoomOutboxActionStore @Inject constructor(
    private val database: AmuletDatabase,
    private val outboxActionDao: OutboxActionDao
) : OutboxActionStore {

    override suspend fun takeNextAction(now: Long): OutboxActionEntity? =
        database.withTransaction {
            val next = outboxActionDao.findDue(now, limit = 1).firstOrNull() ?: return@withTransaction null
            outboxActionDao.updateStatus(
                id = next.id,
                status = OutboxActionStatus.IN_FLIGHT,
                retryCount = next.retryCount,
                lastError = null,
                updatedAt = now,
                availableAt = now
            )
            next.copy(
                status = OutboxActionStatus.IN_FLIGHT,
                lastError = null,
                updatedAt = now,
                availableAt = now
            )
        }

    override suspend fun markCompleted(action: OutboxActionEntity, completedAt: Long) {
        outboxActionDao.updateStatus(
            id = action.id,
            status = OutboxActionStatus.COMPLETED,
            retryCount = action.retryCount,
            lastError = null,
            updatedAt = completedAt,
            availableAt = completedAt
        )
    }

    override suspend fun markForRetry(action: OutboxActionEntity, delayMillis: Long, errorMessage: String?, updatedAt: Long) {
        val newRetryCount = action.retryCount + 1
        val nextAvailableAt = updatedAt + delayMillis
        outboxActionDao.updateStatus(
            id = action.id,
            status = OutboxActionStatus.PENDING,
            retryCount = newRetryCount,
            lastError = errorMessage,
            updatedAt = updatedAt,
            availableAt = nextAvailableAt
        )
    }

    override suspend fun markFailed(action: OutboxActionEntity, errorMessage: String?, updatedAt: Long) {
        val newRetryCount = action.retryCount + 1
        outboxActionDao.updateStatus(
            id = action.id,
            status = OutboxActionStatus.FAILED,
            retryCount = newRetryCount,
            lastError = errorMessage,
            updatedAt = updatedAt,
            availableAt = Long.MAX_VALUE
        )
    }

    override suspend fun resetStuck(cutoffTimestamp: Long, now: Long): Int =
        outboxActionDao.resetStuckActions(
            inFlightStatus = OutboxActionStatus.IN_FLIGHT,
            pendingStatus = OutboxActionStatus.PENDING,
            stuckBefore = cutoffTimestamp,
            now = now
        )
}
