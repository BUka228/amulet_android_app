package com.example.amulet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.OutboxActionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxActionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(action: OutboxActionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(actions: List<OutboxActionEntity>)

    @Query(
        "SELECT * FROM outbox_actions " +
            "WHERE status IN (:statuses) AND availableAt <= :now " +
            "ORDER BY priority DESC, availableAt ASC, createdAt ASC " +
            "LIMIT :limit"
    )
    suspend fun findDueInternal(
        statuses: List<OutboxActionStatus>,
        now: Long,
        limit: Int
    ): List<OutboxActionEntity>

    suspend fun findDue(now: Long, limit: Int): List<OutboxActionEntity> =
        findDueInternal(listOf(OutboxActionStatus.PENDING, OutboxActionStatus.FAILED), now, limit)

    @Query("SELECT * FROM outbox_actions WHERE id = :id")
    suspend fun getById(id: String): OutboxActionEntity?

    @Query("SELECT * FROM outbox_actions WHERE targetEntityId = :entityId AND status != :completed ORDER BY createdAt ASC")
    fun observeForEntity(
        entityId: String,
        completed: OutboxActionStatus = OutboxActionStatus.COMPLETED
    ): Flow<List<OutboxActionEntity>>

    @Query(
        "UPDATE outbox_actions SET " +
            "status = :status, " +
            "retryCount = :retryCount, " +
            "lastError = :lastError, " +
            "updatedAt = :updatedAt, " +
            "availableAt = :availableAt " +
            "WHERE id = :id"
    )
    suspend fun updateStatus(
        id: String,
        status: OutboxActionStatus,
        retryCount: Int,
        lastError: String?,
        updatedAt: Long,
        availableAt: Long
    )

    @Query("DELETE FROM outbox_actions WHERE status = :status AND createdAt < :cutoffTime")
    suspend fun deleteByStatusBefore(status: OutboxActionStatus, cutoffTime: Long): Int

    @Query("DELETE FROM outbox_actions WHERE status = :status AND updatedAt < :cutoffTime")
    suspend fun deleteUpdatedBefore(status: OutboxActionStatus, cutoffTime: Long): Int

    @Query("DELETE FROM outbox_actions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM outbox_actions")
    suspend fun clear()

    @Transaction
    suspend fun cleanupCompleted(cutoffTime: Long): Int =
        deleteByStatusBefore(OutboxActionStatus.COMPLETED, cutoffTime)

    @Transaction
    suspend fun cleanupFailed(cutoffTime: Long): Int =
        deleteUpdatedBefore(OutboxActionStatus.FAILED, cutoffTime)
}
