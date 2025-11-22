package com.example.amulet.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.amulet.core.database.entity.PracticeEntity
import com.example.amulet.core.database.entity.PracticeSessionEntity
import com.example.amulet.core.database.relation.PracticeSessionWithDetails
import com.example.amulet.core.database.relation.PracticeWithFavorites
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPractice(practice: PracticeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPractices(practices: List<PracticeEntity>)

    @Query("SELECT * FROM practices")
    fun observePractices(): Flow<List<PracticeEntity>>

    @Query("SELECT * FROM practices WHERE id = :id")
    fun observePracticeById(id: String): Flow<PracticeEntity?>

    @Transaction
    @Query("SELECT * FROM practices WHERE id = :practiceId")
    fun observePracticeWithFavorites(practiceId: String): Flow<PracticeWithFavorites?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: PracticeSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSessions(sessions: List<PracticeSessionEntity>)

    @Transaction
    @Query("SELECT * FROM practice_sessions WHERE id = :sessionId")
    fun observeSession(sessionId: String): Flow<PracticeSessionWithDetails?>

    @Query("SELECT * FROM practice_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): PracticeSessionEntity?

    @Transaction
    @Query("SELECT * FROM practice_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun observeSessionsForUser(userId: String): Flow<List<PracticeSessionWithDetails>>

    @Transaction
    @Query("SELECT * FROM practice_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun pagingSessionsForUser(userId: String): PagingSource<Int, PracticeSessionWithDetails>

    @Transaction
    @Query("SELECT * FROM practice_sessions WHERE status = :status ORDER BY startedAt DESC")
    fun pagingByStatus(status: String): PagingSource<Int, PracticeSessionWithDetails>

    @Query("DELETE FROM practices")
    suspend fun clearPractices()

    @Query("DELETE FROM practice_sessions")
    suspend fun clearSessions()

    @Query("DELETE FROM practice_sessions WHERE status = 'COMPLETED' AND completedAt IS NOT NULL AND completedAt < :cutoff")
    suspend fun cleanupCompletedSessions(cutoff: Long): Int
    
    // Filtering by level
    @Query("SELECT * FROM practices WHERE level = :level")
    fun observePracticesByLevel(level: String): Flow<List<PracticeEntity>>
    
    // Filtering by goal
    @Query("SELECT * FROM practices WHERE goal = :goal")
    fun observePracticesByGoal(goal: String): Flow<List<PracticeEntity>>
    
    // Filtering by type and goal
    @Query("SELECT * FROM practices WHERE type = :type AND goal = :goal")
    fun observePracticesByTypeAndGoal(type: String, goal: String): Flow<List<PracticeEntity>>
    
    // Search by title
    @Query("SELECT * FROM practices WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchPractices(query: String): Flow<List<PracticeEntity>>
    
    // Get practice with usage count
    @Query("UPDATE practices SET usageCount = usageCount + 1 WHERE id = :practiceId")
    suspend fun incrementUsageCount(practiceId: String)
}

