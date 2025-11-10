package com.example.amulet.data.patterns.datasource

import com.example.amulet.core.database.TransactionRunner
import com.example.amulet.core.database.dao.OutboxActionDao
import com.example.amulet.core.database.dao.PatternDao
import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.PatternEntity
import com.example.amulet.core.database.entity.PatternShareEntity
import com.example.amulet.core.database.entity.TagEntity
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
    
    override suspend fun getSharesForPattern(patternId: String): List<PatternShareEntity> {
        return patternDao.getSharesForPattern(patternId)
    }
    
    override suspend fun upsertPattern(pattern: PatternEntity) {
        patternDao.upsertPattern(pattern)
    }
    
    override suspend fun upsertPatterns(patterns: List<PatternEntity>) {
        patternDao.upsertPatterns(patterns)
    }
    
    override suspend fun upsertPatternWithRelations(
        pattern: PatternEntity,
        tags: List<TagEntity>,
        tagIds: List<String>,
        sharedUserIds: List<String>
    ) {
        patternDao.upsertPatternWithRelations(pattern, tags, tagIds, sharedUserIds)
    }
    
    override suspend fun deletePattern(patternId: String) {
        patternDao.deletePattern(patternId)
    }
    
    override suspend fun clearAll() {
        patternDao.clear()
    }
    
    override suspend fun enqueueOutboxAction(action: OutboxActionEntity) {
        outboxDao.upsert(action)
    }
    
    override suspend fun <R> withPatternTransaction(block: suspend () -> R): R {
        return transactionRunner.runInTransaction(block)
    }
}
