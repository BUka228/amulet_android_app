package com.example.amulet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.amulet.core.database.entity.PracticeCategoryEntity
import com.example.amulet.core.database.entity.PracticeFavoriteEntity
import com.example.amulet.core.database.entity.PracticeScheduleEntity
import com.example.amulet.core.database.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeExtrasDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategories(items: List<PracticeCategoryEntity>)

    @Query("SELECT * FROM practice_categories ORDER BY `order` ASC")
    fun observeCategories(): Flow<List<PracticeCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavorite(entry: PracticeFavoriteEntity)

    @Query("DELETE FROM practice_favorites WHERE userId = :userId AND practiceId = :practiceId")
    suspend fun removeFavorite(userId: String, practiceId: String)

    @Query("SELECT practiceId FROM practice_favorites WHERE userId = :userId")
    fun observeFavoriteIds(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreferences(prefs: UserPreferencesEntity)

    @Query("SELECT * FROM user_preferences WHERE userId = :userId LIMIT 1")
    fun observePreferences(userId: String): Flow<UserPreferencesEntity?>
    
    // Schedules
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchedule(schedule: PracticeScheduleEntity)
    
    @Query("SELECT * FROM practice_schedules WHERE userId = :userId")
    fun observeSchedules(userId: String): Flow<List<PracticeScheduleEntity>>
    
    @Query("DELETE FROM practice_schedules WHERE id = :scheduleId")
    suspend fun deleteSchedule(scheduleId: String)
    
    @Query("DELETE FROM practice_schedules WHERE userId = :userId")
    suspend fun clearSchedules(userId: String)
}
