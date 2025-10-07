package com.example.amulet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.amulet.core.database.entity.TelemetryEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: TelemetryEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(events: List<TelemetryEventEntity>)

    @Query("SELECT * FROM telemetry_events WHERE sentAt IS NULL ORDER BY createdAt ASC LIMIT :limit")
    suspend fun findPending(limit: Int): List<TelemetryEventEntity>

    @Query("SELECT * FROM telemetry_events WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeByUser(userId: String): Flow<List<TelemetryEventEntity>>

    @Query("UPDATE telemetry_events SET sentAt = :sentAt WHERE id IN (:ids)")
    suspend fun markSent(ids: List<String>, sentAt: Long)

    @Query("DELETE FROM telemetry_events WHERE sentAt IS NOT NULL AND createdAt < :cutoffTime")
    suspend fun cleanupSent(cutoffTime: Long): Int

    @Query("DELETE FROM telemetry_events")
    suspend fun clear()
}
