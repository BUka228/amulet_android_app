package com.example.amulet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.amulet.core.database.entity.PrivacyJobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrivacyJobDao {

    @Query("SELECT * FROM privacy_jobs WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeByUser(userId: String): Flow<List<PrivacyJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(job: PrivacyJobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(jobs: List<PrivacyJobEntity>)

    @Query("DELETE FROM privacy_jobs WHERE id = :jobId")
    suspend fun delete(jobId: String)

    @Query("DELETE FROM privacy_jobs WHERE expiresAt IS NOT NULL AND expiresAt < :cutoff")
    suspend fun cleanupExpired(cutoff: Long): Int

    @Query("DELETE FROM privacy_jobs")
    suspend fun clear()
}
