package com.example.amulet.data.practices.datasource

import com.example.amulet.core.database.entity.CollectionEntity
import com.example.amulet.core.database.entity.CollectionItemEntity
import com.example.amulet.core.database.entity.PlanEntity
import com.example.amulet.core.database.entity.PracticeCategoryEntity
import com.example.amulet.core.database.entity.PracticeEntity
import com.example.amulet.core.database.entity.PracticeFavoriteEntity
import com.example.amulet.core.database.entity.PracticeScheduleEntity
import com.example.amulet.core.database.entity.PracticeSessionEntity
import com.example.amulet.core.database.entity.PracticeTagEntity
import com.example.amulet.core.database.entity.UserBadgeEntity
import com.example.amulet.core.database.entity.UserMoodEntryEntity
import com.example.amulet.core.database.entity.UserPracticeStatsEntity
import com.example.amulet.core.database.entity.UserPreferencesEntity
import com.example.amulet.core.database.relation.PracticeWithFavorites
import com.example.amulet.data.practices.seed.PracticeSeed
import kotlinx.coroutines.flow.Flow

interface LocalPracticesDataSource {
    fun observePractices(): Flow<List<PracticeEntity>>
    fun observePracticeById(id: String): Flow<PracticeEntity?>
    fun observePracticeByIdWithFavorites(id: String): Flow<PracticeWithFavorites?>
    fun observeCategories(): Flow<List<PracticeCategoryEntity>>
    fun observeFavoriteIds(userId: String): Flow<List<String>>
    fun observeSessionsForUser(userId: String): Flow<List<PracticeSessionEntity>>
    suspend fun getSessionById(sessionId: String): PracticeSessionEntity?
    fun observePreferences(userId: String): Flow<UserPreferencesEntity?>
    fun observeSchedules(userId: String): Flow<List<PracticeScheduleEntity>>
    fun observeScheduleByPracticeId(userId: String, practiceId: String): Flow<PracticeScheduleEntity?>
    fun observeSchedulesByPlan(planId: String): Flow<List<PracticeScheduleEntity>>
    suspend fun upsertPractices(items: List<PracticeEntity>)
    suspend fun upsertCategories(items: List<PracticeCategoryEntity>)
    suspend fun setFavorite(userId: String, practiceId: String, favorite: Boolean)
    suspend fun upsertSession(entity: PracticeSessionEntity)
    suspend fun upsertPreferences(entity: UserPreferencesEntity)
    suspend fun upsertSchedule(entity: PracticeScheduleEntity)
    suspend fun deleteSchedule(scheduleId: String)
    suspend fun seedPresets(presets: List<PracticeSeed>)

    // Plans
    fun observePlans(userId: String): Flow<List<PlanEntity>>
    fun observePlanById(planId: String): Flow<PlanEntity?>
    suspend fun upsertPlan(entity: PlanEntity)
    suspend fun upsertPlans(entities: List<PlanEntity>)
    suspend fun deletePlan(planId: String)

    // Statistics
    fun observeUserPracticeStats(userId: String): Flow<UserPracticeStatsEntity?>
    suspend fun upsertUserPracticeStats(entity: UserPracticeStatsEntity)

    // Badges
    fun observeBadges(userId: String): Flow<List<UserBadgeEntity>>
    suspend fun upsertBadges(entities: List<UserBadgeEntity>)
    suspend fun deleteBadge(badgeId: String)

    // Mood history
    fun observeMoodEntries(userId: String): Flow<List<UserMoodEntryEntity>>
    suspend fun upsertMoodEntry(entity: UserMoodEntryEntity)

    // Tags
    fun observePracticeTags(): Flow<List<PracticeTagEntity>>

    // Collections
    fun observeCollections(): Flow<List<CollectionEntity>>
    fun observeCollectionItems(collectionId: String): Flow<List<CollectionItemEntity>>
}
