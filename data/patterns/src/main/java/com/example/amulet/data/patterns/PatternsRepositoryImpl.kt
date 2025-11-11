package com.example.amulet.data.patterns

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.OutboxActionStatus
import com.example.amulet.core.database.entity.OutboxActionType
import com.example.amulet.core.sync.scheduler.OutboxScheduler
import com.example.amulet.data.patterns.datasource.LocalPatternDataSource
import com.example.amulet.data.patterns.datasource.RemotePatternDataSource
import com.example.amulet.data.patterns.mapper.toDomain
import com.example.amulet.data.patterns.mapper.toEntity
import com.example.amulet.data.patterns.mapper.toTagEntities
import com.example.amulet.data.patterns.mapper.toTagNames
import com.example.amulet.data.patterns.mapper.toUserIds
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.auth.UserSessionContext
import com.example.amulet.shared.core.auth.UserSessionProvider
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternDraft
import com.example.amulet.shared.domain.patterns.model.PatternFilter
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.model.PatternUpdate
import com.example.amulet.shared.domain.patterns.model.PublishMetadata
import com.example.amulet.shared.domain.patterns.model.SyncResult
import com.example.amulet.shared.domain.user.model.UserId
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация репозитория паттернов.
 * Использует datasource паттерн для разделения источников данных.
 * Команды отправляются через Outbox для гарантированной доставки.
 */
@Singleton
class PatternsRepositoryImpl @Inject constructor(
    private val localDataSource: LocalPatternDataSource,
    private val remoteDataSource: RemotePatternDataSource,
    private val sessionProvider: UserSessionProvider,
    private val outboxScheduler: OutboxScheduler,
    private val json: Json
) : PatternsRepository {
    
    private val currentUserId: String
        get() {
            val context = sessionProvider.currentContext
            return when (context) {
                is UserSessionContext.LoggedIn -> context.userId.value
                else -> throw IllegalStateException("User not authenticated")
            }
        }
    
    override fun getPatternsStream(filter: PatternFilter): Flow<List<Pattern>> {
        val baseFlow = when {
            filter.publicOnly -> localDataSource.observePublic()
            else -> localDataSource.observeByOwner(currentUserId)
        }
        
        return baseFlow.map { entities ->
            entities.map { entity ->
                val tags = localDataSource.getTagsForPattern(entity.id).toTagNames()
                val sharedWith = localDataSource.getSharesForPattern(entity.id).toUserIds()
                entity.toDomain(tags, sharedWith)
            }.filter { pattern ->
                // Применяем фильтры
                (filter.kind == null || pattern.kind == filter.kind) &&
                (filter.hardwareVersion == null || pattern.hardwareVersion == filter.hardwareVersion) &&
                (filter.tags.isEmpty() || filter.tags.any { it in pattern.tags }) &&
                (filter.query == null || pattern.title.contains(filter.query ?: "", ignoreCase = true))
            }
        }
    }
    
    override fun getPatternById(id: PatternId): Flow<Pattern?> {
        return localDataSource.observeById(id.value).map { entity ->
            entity?.let {
                val tags = localDataSource.getTagsForPattern(it.id).toTagNames()
                val sharedWith = localDataSource.getSharesForPattern(it.id).toUserIds()
                it.toDomain(tags, sharedWith)
            }
        }
    }
    
    override fun getMyPatternsStream(): Flow<List<Pattern>> {
        return localDataSource.observeByOwner(currentUserId).map { entities ->
            entities.map { entity ->
                val tags = localDataSource.getTagsForPattern(entity.id).toTagNames()
                val sharedWith = localDataSource.getSharesForPattern(entity.id).toUserIds()
                entity.toDomain(tags, sharedWith)
            }
        }
    }
    
    override suspend fun syncWithCloud(): AppResult<SyncResult> {
        return try {
            // Получаем все паттерны с сервера
            val remotePatterns = remoteDataSource.getOwnPatterns().getOrElse { error ->
                return Err(error)
            }
            
            // Сохраняем в локальную БД
            val entities = remotePatterns.map { it.toEntity() }
            localDataSource.upsertPatterns(entities)
            
            // Обрабатываем теги и шеринг для каждого паттерна
            remotePatterns.forEach { dto ->
                val tags = dto.tags?.toTagEntities() ?: emptyList()
                val tagIds = tags.map { it.id }
                val sharedWith = dto.sharedWith ?: emptyList()
                
                localDataSource.upsertPatternWithRelations(
                    pattern = dto.toEntity(),
                    tags = tags,
                    tagIds = tagIds,
                    sharedUserIds = sharedWith
                )
            }
            
            Ok(SyncResult(
                patternsAdded = remotePatterns.size,
                patternsUpdated = 0,
                patternsDeleted = 0
            ))
        } catch (_: Exception) {
            Err(AppError.Unknown)
        }
    }
    
    override suspend fun createPattern(draft: PatternDraft): AppResult<Pattern> {
        return try {
            val patternId = UUID.randomUUID().toString()
            val nowMillis = System.currentTimeMillis()
            
            // Создаем локальную сущность
            val pattern = Pattern(
                id = PatternId(patternId),
                version = 1,
                ownerId = UserId(currentUserId),
                kind = draft.kind,
                spec = draft.spec,
                public = false,
                reviewStatus = null,
                hardwareVersion = draft.hardwareVersion,
                title = draft.title,
                description = draft.description,
                tags = emptyList(),
                usageCount = null,
                sharedWith = emptyList(),
                createdAt = nowMillis,
                updatedAt = nowMillis
            )
            
            // Транзакционно: сохраняем в БД и добавляем в Outbox
            localDataSource.withPatternTransaction {
                localDataSource.upsertPattern(pattern.toEntity())
                
                val payload = json.encodeToString(
                    mapOf(
                        "kind" to draft.kind.name.lowercase(),
                        "spec" to json.encodeToString(PatternSpec.serializer(), draft.spec),
                        "title" to draft.title,
                        "description" to draft.description,
                        "hardwareVersion" to draft.hardwareVersion
                    )
                )
                
                localDataSource.enqueueOutboxAction(
                    OutboxActionEntity(
                        id = UUID.randomUUID().toString(),
                        type = OutboxActionType.PATTERN_CREATE,
                        payloadJson = payload,
                        status = OutboxActionStatus.PENDING,
                        retryCount = 0,
                        lastError = null,
                        idempotencyKey = "pattern_create_$patternId",
                        createdAt = nowMillis,
                        updatedAt = nowMillis,
                        availableAt = nowMillis,
                        priority = 1,
                        targetEntityId = patternId
                    )
                )
            }
            
            // Планируем синхронизацию
            //outboxScheduler.scheduleSync()
            
            Ok(pattern)
        } catch (_: Exception) {
            Err(AppError.DatabaseError)
        }
    }
    
    override suspend fun updatePattern(
        id: PatternId,
        version: Int,
        updates: PatternUpdate
    ): AppResult<Pattern> {
        return try {
            val entity = localDataSource.observeById(id.value).first()
                ?: return Err(AppError.NotFound)
            
            val tags = localDataSource.getTagsForPattern(id.value).toTagNames()
            val sharedWith = localDataSource.getSharesForPattern(id.value).toUserIds()
            val currentPattern = entity.toDomain(tags, sharedWith)
            
            // Применяем обновления
            val updatedPattern = currentPattern.copy(
                title = updates.title ?: currentPattern.title,
                description = updates.description ?: currentPattern.description,
                spec = updates.spec ?: currentPattern.spec,
                tags = updates.tags ?: currentPattern.tags,
                version = version + 1,
                updatedAt = System.currentTimeMillis()
            )
            
            val nowMillis = System.currentTimeMillis()
            
            // Транзакционно: обновляем в БД и добавляем в Outbox
            localDataSource.withPatternTransaction {
                localDataSource.upsertPattern(updatedPattern.toEntity())
                
                val payload = json.encodeToString(
                    mapOf(
                        "version" to version,
                        "title" to updates.title,
                        "description" to updates.description,
                        "spec" to updates.spec?.let { json.encodeToString(PatternSpec.serializer(), it) },
                        "tags" to updates.tags
                    )
                )
                
                localDataSource.enqueueOutboxAction(
                    OutboxActionEntity(
                        id = UUID.randomUUID().toString(),
                        type = OutboxActionType.PATTERN_UPDATE,
                        payloadJson = payload,
                        status = OutboxActionStatus.PENDING,
                        retryCount = 0,
                        lastError = null,
                        idempotencyKey = "pattern_update_${id.value}_$version",
                        createdAt = nowMillis,
                        updatedAt = nowMillis,
                        availableAt = nowMillis,
                        priority = 1,
                        targetEntityId = id.value
                    )
                )
            }
            
            //outboxScheduler.scheduleSync()
            
            Ok(updatedPattern)
        } catch (_: Exception) {
            Err(AppError.DatabaseError)
        }
    }
    
    override suspend fun deletePattern(id: PatternId): AppResult<Unit> {
        return try {
            val nowMillis = System.currentTimeMillis()
            
            // Транзакционно: удаляем из БД и добавляем в Outbox
            localDataSource.withPatternTransaction {
                localDataSource.deletePattern(id.value)
                
                localDataSource.enqueueOutboxAction(
                    OutboxActionEntity(
                        id = UUID.randomUUID().toString(),
                        type = OutboxActionType.PATTERN_DELETE,
                        payloadJson = json.encodeToString(mapOf("patternId" to id.value)),
                        status = OutboxActionStatus.PENDING,
                        retryCount = 0,
                        lastError = null,
                        idempotencyKey = "pattern_delete_${id.value}",
                        createdAt = nowMillis,
                        updatedAt = nowMillis,
                        availableAt = nowMillis,
                        priority = 1,
                        targetEntityId = id.value
                    )
                )
            }
            
            //outboxScheduler.scheduleSync()
            
            Ok(Unit)
        } catch (_: Exception) {
            Err(AppError.DatabaseError)
        }
    }
    
    override suspend fun publishPattern(
        id: PatternId,
        metadata: PublishMetadata
    ): AppResult<Pattern> {
        return try {
            val entity = localDataSource.observeById(id.value).first()
                ?: return Err(AppError.NotFound)
            
            val tags = localDataSource.getTagsForPattern(id.value).toTagNames()
            val sharedWith = localDataSource.getSharesForPattern(id.value).toUserIds()
            val currentPattern = entity.toDomain(tags, sharedWith)
            
            val updatedPattern = currentPattern.copy(
                public = true,
                title = metadata.title,
                description = metadata.description,
                tags = metadata.tags,
                updatedAt = System.currentTimeMillis()
            )
            
            val nowMillis = System.currentTimeMillis()
            
            localDataSource.withPatternTransaction {
                localDataSource.upsertPattern(updatedPattern.toEntity())
                
                val payload = json.encodeToString(
                    mapOf(
                        "version" to currentPattern.version,
                        "public" to true,
                        "title" to metadata.title,
                        "description" to metadata.description,
                        "tags" to metadata.tags
                    )
                )
                
                localDataSource.enqueueOutboxAction(
                    OutboxActionEntity(
                        id = UUID.randomUUID().toString(),
                        type = OutboxActionType.PATTERN_UPDATE,
                        payloadJson = payload,
                        status = OutboxActionStatus.PENDING,
                        retryCount = 0,
                        lastError = null,
                        idempotencyKey = "pattern_publish_${id.value}",
                        createdAt = nowMillis,
                        updatedAt = nowMillis,
                        availableAt = nowMillis,
                        priority = 1,
                        targetEntityId = id.value
                    )
                )
            }
            
            //outboxScheduler.scheduleSync()
            
            Ok(updatedPattern)
        } catch (_: Exception) {
            Err(AppError.DatabaseError)
        }
    }
    
    override suspend fun sharePattern(id: PatternId, userIds: List<UserId>): AppResult<Unit> {
        return try {
            val nowMillis = System.currentTimeMillis()
            
            userIds.forEach { userId ->
                localDataSource.withPatternTransaction {
                    val payload = json.encodeToString(
                        mapOf(
                            "patternId" to id.value,
                            "toUserId" to userId.value
                        )
                    )
                    
                    localDataSource.enqueueOutboxAction(
                        OutboxActionEntity(
                            id = UUID.randomUUID().toString(),
                            type = OutboxActionType.PATTERN_SHARE,
                            payloadJson = payload,
                            status = OutboxActionStatus.PENDING,
                            retryCount = 0,
                            lastError = null,
                            idempotencyKey = "pattern_share_${id.value}_${userId.value}",
                            createdAt = nowMillis,
                            updatedAt = nowMillis,
                            availableAt = nowMillis,
                            priority = 1,
                            targetEntityId = id.value
                        )
                    )
                }
            }
            
            //outboxScheduler.scheduleSync()
            
            Ok(Unit)
        } catch (_: Exception) {
            Err(AppError.DatabaseError)
        }
    }
    
    override suspend fun addTag(patternId: PatternId, tag: String): AppResult<Unit> {
        return try {
            val entity = localDataSource.observeById(patternId.value).first()
                ?: return Err(AppError.NotFound)
            
            val currentTags = localDataSource.getTagsForPattern(patternId.value).toTagNames()
            if (tag in currentTags) {
                return Ok(Unit) // Тег уже есть
            }
            
            val updatedTags = currentTags + tag
            val tagEntities = updatedTags.toTagEntities()
            
            localDataSource.upsertPatternWithRelations(
                pattern = entity,
                tags = tagEntities,
                tagIds = tagEntities.map { it.id },
                sharedUserIds = localDataSource.getSharesForPattern(patternId.value).toUserIds()
            )
            
            Ok(Unit)
        } catch (_: Exception) {
            Err(AppError.DatabaseError)
        }
    }
    
    override suspend fun removeTag(patternId: PatternId, tag: String): AppResult<Unit> {
        return try {
            val entity = localDataSource.observeById(patternId.value).first()
                ?: return Err(AppError.NotFound)
            
            val currentTags = localDataSource.getTagsForPattern(patternId.value).toTagNames()
            val updatedTags = currentTags.filter { it != tag }
            val tagEntities = updatedTags.toTagEntities()
            
            localDataSource.upsertPatternWithRelations(
                pattern = entity,
                tags = tagEntities,
                tagIds = tagEntities.map { it.id },
                sharedUserIds = localDataSource.getSharesForPattern(patternId.value).toUserIds()
            )
            
            Ok(Unit)
        } catch (_: Exception) {
            Err(AppError.DatabaseError)
        }
    }
}
