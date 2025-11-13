package com.example.amulet.data.patterns.datasource

import com.example.amulet.core.database.TransactionRunner
import com.example.amulet.core.database.dao.OutboxActionDao
import com.example.amulet.core.database.dao.PatternDao
import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.PatternEntity
import com.example.amulet.core.database.entity.PatternShareEntity
import com.example.amulet.core.database.entity.TagEntity
import com.example.amulet.shared.core.logging.Logger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Реализация локального источника данных для паттернов.
 */
class LocalPatternDataSourceImpl @Inject constructor(
    private val patternDao: PatternDao,
    private val outboxDao: OutboxActionDao,
    private val transactionRunner: TransactionRunner
) : LocalPatternDataSource {
    
    override fun observeById(patternId: String): Flow<PatternEntity?> {
        return patternDao.observeById(patternId)
    }
    
    override fun observeByOwner(ownerId: String): Flow<List<PatternEntity>> {
        return patternDao.observeByOwner(ownerId)
    }
    
    override fun observePublic(): Flow<List<PatternEntity>> {
        return patternDao.observePublic()
    }
    
    override fun observeSharedWith(userId: String): Flow<List<PatternEntity>> {
        return patternDao.observeSharedWith(userId)
    }
    
    override suspend fun getTagsForPattern(patternId: String): List<TagEntity> {
        return patternDao.getTagsForPattern(patternId)
    }
    
    override suspend fun getAllTags(): List<TagEntity> {
        return patternDao.getAllTags()
    }

    override suspend fun searchTags(query: String): List<TagEntity> {
        return patternDao.searchTags(query)
    }
    
    override suspend fun getTagsByNames(names: List<String>): List<TagEntity> {
        return patternDao.getTagsByNames(names)
    }

    override suspend fun deleteTagsByNames(names: List<String>) {
        patternDao.deleteTagsByNames(names)
    }
    
    override suspend fun insertTags(tags: List<TagEntity>) {
        Logger.d("Вставка/создание тегов: ${tags.size}", "LocalPatternDataSourceImpl")
        patternDao.insertTags(tags)
    }
    
    override suspend fun getSharesForPattern(patternId: String): List<PatternShareEntity> {
        return patternDao.getSharesForPattern(patternId)
    }
    
    override suspend fun upsertPattern(pattern: PatternEntity) {
        Logger.d("Сохранение паттерна в БД: ${pattern.id}", "LocalPatternDataSourceImpl")
        patternDao.upsertPattern(pattern)
    }
    
    override suspend fun upsertPatterns(patterns: List<PatternEntity>) {
        Logger.d("Сохранение паттернов в БД: ${patterns.size}", "LocalPatternDataSourceImpl")
        patternDao.upsertPatterns(patterns)
    }
    
    override suspend fun upsertPatternWithRelations(
        pattern: PatternEntity,
        tags: List<TagEntity>,
        tagIds: List<String>,
        sharedUserIds: List<String>
    ) {
        Logger.d("Сохранение паттерна с_relations в БД: ${pattern.id}", "LocalPatternDataSourceImpl")
        patternDao.upsertPatternWithRelations(pattern, tags, tagIds, sharedUserIds)
    }
    
    override suspend fun deletePattern(patternId: String) {
        Logger.d("Удаление паттерна из БД: $patternId", "LocalPatternDataSourceImpl")
        patternDao.deletePattern(patternId)
    }
    
    override suspend fun clearAll() {
        Logger.d("Очистка всех паттернов из БД", "LocalPatternDataSourceImpl")
        patternDao.clear()
    }
    
    override suspend fun enqueueOutboxAction(action: OutboxActionEntity) {
        Logger.d("Добавление действия в Outbox: ${action.type}, ID: ${action.id}", "LocalPatternDataSourceImpl")
        outboxDao.upsert(action)
    }
    
    override suspend fun <R> withPatternTransaction(block: suspend () -> R): R {
        Logger.d("Начало транзакции", "LocalPatternDataSourceImpl")
        val result = transactionRunner.runInTransaction(block)
        Logger.d("Завершение транзакции", "LocalPatternDataSourceImpl")
        return result
    }
}

