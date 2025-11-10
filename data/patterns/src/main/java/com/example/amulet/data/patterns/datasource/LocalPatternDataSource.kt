package com.example.amulet.data.patterns.datasource

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.PatternEntity
import com.example.amulet.core.database.entity.PatternShareEntity
import com.example.amulet.core.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

/**
 * Локальный источник данных для паттернов.
 * Инкапсулирует работу с PatternDao, OutboxDao и транзакциями Room.
 */
interface LocalPatternDataSource {
    
    /**
     * Наблюдать за паттерном по ID.
     */
    fun observeById(patternId: String): Flow<PatternEntity?>
    
    /**
     * Наблюдать за паттернами владельца.
     */
    fun observeByOwner(ownerId: String): Flow<List<PatternEntity>>
    
    /**
     * Наблюдать за публичными паттернами.
     */
    fun observePublic(): Flow<List<PatternEntity>>
    
    /**
     * Наблюдать за паттернами, расшаренными с пользователем.
     */
    fun observeSharedWith(userId: String): Flow<List<PatternEntity>>
    
    /**
     * Получить теги для паттерна.
     */
    suspend fun getTagsForPattern(patternId: String): List<TagEntity>
    
    /**
     * Получить список пользователей, с которыми расшарен паттерн.
     */
    suspend fun getSharesForPattern(patternId: String): List<PatternShareEntity>
    
    /**
     * Сохранить или обновить паттерн.
     */
    suspend fun upsertPattern(pattern: PatternEntity)
    
    /**
     * Сохранить или обновить несколько паттернов.
     */
    suspend fun upsertPatterns(patterns: List<PatternEntity>)
    
    /**
     * Сохранить паттерн с тегами и шерингом (транзакционно).
     */
    suspend fun upsertPatternWithRelations(
        pattern: PatternEntity,
        tags: List<TagEntity>,
        tagIds: List<String>,
        sharedUserIds: List<String>
    )
    
    /**
     * Удалить паттерн по ID.
     */
    suspend fun deletePattern(patternId: String)
    
    /**
     * Удалить все паттерны.
     */
    suspend fun clearAll()
    
    /**
     * Добавить действие в Outbox.
     */
    suspend fun enqueueOutboxAction(action: OutboxActionEntity)
    
    /**
     * Выполнить транзакцию с паттерном и Outbox действием.
     */
    suspend fun <R> withPatternTransaction(block: suspend () -> R): R
}
