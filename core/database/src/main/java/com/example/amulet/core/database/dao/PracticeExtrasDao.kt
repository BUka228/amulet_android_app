package com.example.amulet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.amulet.core.database.entity.CollectionEntity
import com.example.amulet.core.database.entity.CollectionItemEntity
import com.example.amulet.core.database.entity.PlanEntity
import com.example.amulet.core.database.entity.PracticeCategoryEntity
import com.example.amulet.core.database.entity.PracticeFavoriteEntity
import com.example.amulet.core.database.entity.PracticeScheduleEntity
import com.example.amulet.core.database.entity.PracticeTagCrossRef
import com.example.amulet.core.database.entity.PracticeTagEntity
import com.example.amulet.core.database.entity.UserBadgeEntity
import com.example.amulet.core.database.entity.UserPracticeStatsEntity
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
    
    @Query("SELECT * FROM practice_schedules WHERE userId = :userId AND practiceId = :practiceId LIMIT 1")
    fun observeScheduleByPracticeId(userId: String, practiceId: String): Flow<PracticeScheduleEntity?>
    
    @Query("DELETE FROM practice_schedules WHERE id = :scheduleId")
    suspend fun deleteSchedule(scheduleId: String)
    
    @Query("DELETE FROM practice_schedules WHERE userId = :userId")
    suspend fun clearSchedules(userId: String)

    // Plans
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlan(plan: PlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlans(plans: List<PlanEntity>)

    @Query("SELECT * FROM plans WHERE userId = :userId ORDER BY createdAt DESC")
    fun observePlans(userId: String): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans WHERE id = :planId")
    fun observePlanById(planId: String): Flow<PlanEntity?>

    @Query("DELETE FROM plans WHERE id = :planId")
    suspend fun deletePlan(planId: String)

    // Schedules by plan
    @Query("SELECT * FROM practice_schedules WHERE planId = :planId ORDER BY timeOfDay ASC")
    fun observeSchedulesByPlan(planId: String): Flow<List<PracticeScheduleEntity>>

    // User practice stats
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserPracticeStats(stats: UserPracticeStatsEntity)

    @Query("SELECT * FROM user_practice_stats WHERE userId = :userId")
    fun observeUserPracticeStats(userId: String): Flow<UserPracticeStatsEntity?>

    // Badges
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBadges(badges: List<UserBadgeEntity>)

    @Query("SELECT * FROM user_badges WHERE userId = :userId ORDER BY earnedAt DESC")
    fun observeBadges(userId: String): Flow<List<UserBadgeEntity>>

    @Query("DELETE FROM user_badges WHERE id = :badgeId")
    suspend fun deleteBadge(badgeId: String)

    // Practice tags
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPracticeTags(tags: List<PracticeTagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPracticeTagRefs(refs: List<PracticeTagCrossRef>)

    @Query("SELECT * FROM practice_tags ORDER BY name ASC")
    fun observePracticeTags(): Flow<List<PracticeTagEntity>>

    @Query(
        "SELECT t.* FROM practice_tags t " +
            "INNER JOIN practice_tag_cross_ref r ON t.id = r.tagId " +
            "WHERE r.practiceId = :practiceId"
    )
    fun observeTagsForPractice(practiceId: String): Flow<List<PracticeTagEntity>>

    // Collections
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCollections(collections: List<CollectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCollectionItems(items: List<CollectionItemEntity>)

    @Query("SELECT * FROM collections ORDER BY `order` ASC")
    fun observeCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collection_items WHERE collectionId = :collectionId ORDER BY `order` ASC")
    fun observeCollectionItems(collectionId: String): Flow<List<CollectionItemEntity>>
}
