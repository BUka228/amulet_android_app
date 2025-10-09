package com.example.amulet.core.sync.internal

import com.example.amulet.core.database.entity.OutboxActionEntity

interface OutboxActionStore {
    suspend fun takeNextAction(now: Long): OutboxActionEntity?
    suspend fun markCompleted(action: OutboxActionEntity, completedAt: Long)
    suspend fun markForRetry(action: OutboxActionEntity, delayMillis: Long, errorMessage: String?, updatedAt: Long)
    suspend fun markFailed(action: OutboxActionEntity, errorMessage: String?, updatedAt: Long)
    suspend fun resetStuck(cutoffTimestamp: Long, now: Long): Int
}
