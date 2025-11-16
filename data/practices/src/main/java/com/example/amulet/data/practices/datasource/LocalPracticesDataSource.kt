package com.example.amulet.data.practices.datasource

import com.example.amulet.core.database.entity.PracticeCategoryEntity
import com.example.amulet.core.database.entity.PracticeEntity
import com.example.amulet.core.database.entity.PracticeFavoriteEntity
import com.example.amulet.core.database.entity.PracticeSessionEntity
import com.example.amulet.core.database.entity.UserPreferencesEntity
import com.example.amulet.data.practices.seed.PracticeSeed
import kotlinx.coroutines.flow.Flow

interface LocalPracticesDataSource {
    fun observePractices(): Flow<List<PracticeEntity>>
    fun observePracticeById(id: String): Flow<PracticeEntity?>
    fun observeCategories(): Flow<List<PracticeCategoryEntity>>
    fun observeFavoriteIds(userId: String): Flow<List<String>>
    fun observeSessionsForUser(userId: String): Flow<List<PracticeSessionEntity>>
    suspend fun getSessionById(sessionId: String): PracticeSessionEntity?
    fun observePreferences(userId: String): Flow<UserPreferencesEntity?>
    suspend fun upsertPractices(items: List<PracticeEntity>)
    suspend fun upsertCategories(items: List<PracticeCategoryEntity>)
    suspend fun setFavorite(userId: String, practiceId: String, favorite: Boolean)
    suspend fun upsertSession(entity: PracticeSessionEntity)
    suspend fun upsertPreferences(entity: UserPreferencesEntity)
    suspend fun seedPresets(presets: List<PracticeSeed>)
}
