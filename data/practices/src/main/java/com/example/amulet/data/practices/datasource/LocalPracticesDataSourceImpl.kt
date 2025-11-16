package com.example.amulet.data.practices.datasource

import com.example.amulet.core.database.TransactionRunner
import com.example.amulet.core.database.dao.PracticeDao
import com.example.amulet.core.database.dao.PracticeExtrasDao
import com.example.amulet.core.database.entity.PracticeCategoryEntity
import com.example.amulet.core.database.entity.PracticeEntity
import com.example.amulet.core.database.entity.PracticeFavoriteEntity
import com.example.amulet.core.database.entity.PracticeSessionEntity
import com.example.amulet.core.database.entity.UserPreferencesEntity
import com.example.amulet.data.practices.mapper.toEntity
import com.example.amulet.data.practices.seed.PracticeSeed
import com.example.amulet.shared.core.logging.Logger
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

    override suspend fun seedPresets(presets: List<PracticeSeed>) {
        if (presets.isEmpty()) return
        Logger.d("Сидирование предустановленных практик: ${presets.size}", "LocalPracticesDataSourceImpl")
        tx.runInTransaction {
            // 1) Подготовим уникальные категории
            val categories = presets.mapNotNull { it.category }.distinct()
            if (categories.isNotEmpty()) {
                val categoryEntities = categories.mapIndexed { index, category ->
                    PracticeCategoryEntity(
                        id = category.lowercase().replace(" ", "_"),
                        title = category,
                        order = index
                    )
                }
                Logger.d("Создание категорий: ${categoryEntities.size}", "LocalPracticesDataSourceImpl")
                extrasDao.upsertCategories(categoryEntities)
            }

            // 2) Вставим практики
            val practiceEntities = presets.map { it.toEntity() }
            Logger.d("Создание практик: ${practiceEntities.size}", "LocalPracticesDataSourceImpl")
            practiceDao.upsertPractices(practiceEntities)
        }
    }
}
