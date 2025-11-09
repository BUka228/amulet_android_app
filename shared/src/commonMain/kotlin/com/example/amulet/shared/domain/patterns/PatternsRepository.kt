package com.example.amulet.shared.domain.patterns

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternDraft
import com.example.amulet.shared.domain.patterns.model.PatternFilter
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternUpdate
import com.example.amulet.shared.domain.patterns.model.PublishMetadata
import com.example.amulet.shared.domain.patterns.model.SyncResult
import com.example.amulet.shared.domain.user.model.UserId
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для работы с паттернами.
 * Предоставляет методы для чтения, создания, обновления и удаления паттернов.
 */
interface PatternsRepository {
    // Потоки данных (только из локальной БД)
    fun getPatternsStream(
        filter: PatternFilter
    ): Flow<List<Pattern>>
    
    fun getPatternById(id: PatternId): Flow<Pattern?>
    
    fun getMyPatternsStream(): Flow<List<Pattern>>
    
    // Синхронизация (ручная, по запросу пользователя)
    suspend fun syncWithCloud(): AppResult<SyncResult>
    
    // Команды
    suspend fun createPattern(
        draft: PatternDraft
    ): AppResult<Pattern>
    
    suspend fun updatePattern(
        id: PatternId,
        version: Int,
        updates: PatternUpdate
    ): AppResult<Pattern>
    
    suspend fun deletePattern(
        id: PatternId
    ): AppResult<Unit>
    
    suspend fun publishPattern(
        id: PatternId,
        metadata: PublishMetadata
    ): AppResult<Pattern>
    
    suspend fun sharePattern(
        id: PatternId,
        userIds: List<UserId>
    ): AppResult<Unit>
    
    // Теги
    suspend fun addTag(
        patternId: PatternId,
        tag: String
    ): AppResult<Unit>
    
    suspend fun removeTag(
        patternId: PatternId,
        tag: String
    ): AppResult<Unit>
}
