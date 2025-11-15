package com.example.amulet.data.practices.datasource

import com.example.amulet.core.database.TransactionRunner
import com.example.amulet.core.database.dao.PracticeDao
import com.example.amulet.core.database.dao.PracticeExtrasDao
import com.example.amulet.core.database.entity.PracticeCategoryEntity
import com.example.amulet.core.database.entity.PracticeEntity
import com.example.amulet.core.database.entity.PracticeFavoriteEntity
import com.example.amulet.core.database.entity.PracticeSessionEntity
import com.example.amulet.core.database.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalPracticesDataSourceImpl @Inject constructor(
    private val practiceDao: PracticeDao,
    private val extrasDao: PracticeExtrasDao,
    private val tx: TransactionRunner
) : LocalPracticesDataSource {

    override fun observePractices(): Flow<List<PracticeEntity>> = practiceDao.observePractices()

    override fun observePracticeById(id: String): Flow<PracticeEntity?> = practiceDao.observePracticeById(id)

    override fun observeCategories(): Flow<List<PracticeCategoryEntity>> = extrasDao.observeCategories()

    override fun observeFavoriteIds(userId: String): Flow<List<String>> = extrasDao.observeFavoriteIds(userId)

    override fun observeSessionsForUser(userId: String): Flow<List<PracticeSessionEntity>> =
        practiceDao.observeSessionsForUser(userId).map { relations -> relations.map { it.session } }

    override suspend fun getSessionById(sessionId: String): PracticeSessionEntity? =
        practiceDao.getSessionById(sessionId)

    override fun observePreferences(userId: String): Flow<UserPreferencesEntity?> = extrasDao.observePreferences(userId)

    override suspend fun upsertPractices(items: List<PracticeEntity>) {
        practiceDao.upsertPractices(items)
    }

    override suspend fun upsertCategories(items: List<PracticeCategoryEntity>) {
        extrasDao.upsertCategories(items)
    }

    override suspend fun setFavorite(userId: String, practiceId: String, favorite: Boolean) {
        if (favorite) extrasDao.upsertFavorite(PracticeFavoriteEntity(userId, practiceId))
        else extrasDao.removeFavorite(userId, practiceId)
    }

    override suspend fun upsertSession(entity: PracticeSessionEntity) {
        practiceDao.upsertSession(entity)
    }

    override suspend fun upsertPreferences(entity: UserPreferencesEntity) {
        extrasDao.upsertPreferences(entity)
    }
}
