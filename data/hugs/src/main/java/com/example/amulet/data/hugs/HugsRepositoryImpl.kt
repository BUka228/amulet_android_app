package com.example.amulet.data.hugs

import com.example.amulet.core.database.entity.HugEntity
import com.example.amulet.data.hugs.datasource.local.HugsLocalDataSource
import com.example.amulet.data.hugs.datasource.remote.HugsRemoteDataSource
import com.example.amulet.core.network.dto.hug.HugEmotionDto
import com.example.amulet.core.network.dto.hug.HugSendRequestDto
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.hugs.HugsRepository
import com.example.amulet.shared.domain.hugs.PairsRepository
import com.example.amulet.shared.domain.hugs.model.Hug
import com.example.amulet.shared.domain.hugs.model.HugId
import com.example.amulet.shared.domain.hugs.model.HugStatus
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.hugs.model.PairQuickReply
import com.example.amulet.shared.domain.user.model.UserId
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.fold
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class HugsRepositoryImpl @Inject constructor(
    private val localDataSource: HugsLocalDataSource,
    private val remoteDataSource: HugsRemoteDataSource,
    private val pairsRepository: PairsRepository,
) : HugsRepository {

    override suspend fun sendHug(
        pairId: PairId?,
        fromUserId: UserId,
        toUserId: UserId?,
        quickReply: PairQuickReply?,
        payload: Map<String, Any?>?
    ): AppResult<Unit> {
        // Для отправки через quick reply нам нужен привязанный PairEmotion.
        val emotion = quickReply?.let { reply ->
            if (pairId == null) {
                return Err(AppError.Validation(mapOf("pairId" to "pairId is required for quick reply")))
            }

            val emotions = pairsRepository.observePairEmotions(pairId).first()
            val boundEmotion = emotions.firstOrNull { it.id == reply.emotionId }
                ?: return Err(AppError.Validation(mapOf("emotionId" to "Unknown emotion for quick reply")))

            HugEmotionDto(
                color = boundEmotion.colorHex,
                patternId = boundEmotion.patternId?.value
            )
        } ?: return Err(AppError.Validation(mapOf("emotion" to "Hug emotion must not be null: use quick reply or explicit emotion support")))

        val request = HugSendRequestDto(
            toUserId = toUserId?.value,
            pairId = pairId?.value,
            emotion = requireNotNull(emotion) {
                "Hug emotion must not be null: use quick reply or explicit emotion support"
            },
            payload = null,
            inReplyToHugId = null
        )

        val remoteResult = remoteDataSource.sendHug(request)

        return remoteResult.fold(
            success = { hugId ->
                val entity = HugEntity(
                    id = hugId,
                    fromUserId = fromUserId.value,
                    toUserId = toUserId?.value,
                    pairId = pairId?.value,
                    emotionColor = emotion.color,
                    emotionPatternId = emotion.patternId,
                    payloadJson = null,
                    inReplyToHugId = null,
                    deliveredAt = null,
                    status = HugStatus.SENT.name,
                    createdAt = System.currentTimeMillis()
                )
                localDataSource.upsert(entity)
                Ok(Unit)
            },
            failure = { error ->
                Ok(Unit)
            }
        )
    }

    override fun observeHugsForPair(pairId: PairId): Flow<List<Hug>> =
        localDataSource.observeByPairId(pairId.value)
            .map { list -> list.mapNotNull { it.toDomainOrNull() } }

    override fun observeHugsForUser(userId: UserId): Flow<List<Hug>> =
        localDataSource.observeByUserId(userId.value)
            .map { list -> list.mapNotNull { it.toDomainOrNull() } }

    override suspend fun updateHugStatus(hugId: HugId, status: HugStatus): AppResult<Unit> {
        val remoteResult = remoteDataSource.updateHugStatus(hugId.value, status.name)
        return remoteResult.fold(
            success = { dto ->
                localDataSource.updateStatus(dto.id, dto.status ?: status.name)
                Ok(Unit)
            },
            failure = { error ->
                // Best-effort: обновляем локально, но возвращаем ошибку наружу.
                localDataSource.updateStatus(hugId.value, status.name)
                Err(error)
            }
        )
    }

    override suspend fun getHugById(hugId: HugId): AppResult<Hug> {
        // Сначала пробуем взять из локальной БД.
        val local = localDataSource.observeById(hugId.value).firstOrNull()?.toDomainOrNull()
        if (local != null) return Ok(local)

        // Если локально нет — пробуем загрузить с сервера.
        val remote = remoteDataSource.getHug(hugId.value)
        return remote.fold(
            success = { dto ->
                val entity = HugEntity(
                    id = dto.id,
                    fromUserId = dto.fromUserId,
                    toUserId = dto.toUserId,
                    pairId = dto.pairId,
                    emotionColor = dto.emotion?.color,
                    emotionPatternId = dto.emotion?.patternId,
                    payloadJson = dto.payload?.toString(),
                    inReplyToHugId = dto.inReplyToHugId,
                    deliveredAt = dto.deliveredAt?.value,
                    status = dto.status ?: HugStatus.SENT.name,
                    createdAt = dto.createdAt?.value ?: System.currentTimeMillis()
                )
                localDataSource.upsert(entity)
                entity.toDomainOrNull()?.let { Ok(it) } ?: Err(AppError.Unknown)
            },
            failure = { error -> Err(error) }
        )
    }

    override suspend fun syncHugs(
        direction: String,
        cursor: String?,
        limit: Int?
    ): AppResult<Unit> {
        val remote = remoteDataSource.getHugs(direction = direction, cursor = cursor, limit = limit)
        return remote.fold(
            success = { listResponse ->
                if (listResponse.items.isEmpty()) {
                    return Ok(Unit)
                }

                val mergedEntities = mutableListOf<HugEntity>()

                for (dto in listResponse.items) {
                    val remoteStatus = dto.status ?: HugStatus.SENT.name
                    val remoteDeliveredAt = dto.deliveredAt?.value
                    val remoteCreatedAt = dto.createdAt?.value ?: System.currentTimeMillis()

                    val local = localDataSource.observeById(dto.id).firstOrNull()

                    if (local == null) {
                        // Нет локальной записи — просто создаём по данным сервера.
                        mergedEntities += HugEntity(
                            id = dto.id,
                            fromUserId = dto.fromUserId,
                            toUserId = dto.toUserId,
                            pairId = dto.pairId,
                            emotionColor = dto.emotion?.color,
                            emotionPatternId = dto.emotion?.patternId,
                            payloadJson = dto.payload?.toString(),
                            inReplyToHugId = dto.inReplyToHugId,
                            deliveredAt = remoteDeliveredAt,
                            status = remoteStatus,
                            createdAt = remoteCreatedAt
                        )
                        continue
                    }

                    // Есть локальная запись — мерджим статусы и deliveredAt.
                    val localStatus = runCatching { HugStatus.valueOf(local.status) }.getOrElse { HugStatus.SENT }
                    val remoteStatusEnum = runCatching { HugStatus.valueOf(remoteStatus) }.getOrElse { HugStatus.SENT }

                    val mergedStatusEnum = maxOf(localStatus, remoteStatusEnum, compareBy { statusOrder(it) })
                    val mergedStatus = mergedStatusEnum.name

                    val localDeliveredAt = local.deliveredAt
                    val mergedDeliveredAt = when {
                        localDeliveredAt == null -> remoteDeliveredAt
                        remoteDeliveredAt == null -> localDeliveredAt
                        else -> maxOf(localDeliveredAt, remoteDeliveredAt)
                    }

                    // Если локальный статус "опережает" серверный — пытаемся подтолкнуть сервер best-effort.
                    if (localStatus.ordinal > remoteStatusEnum.ordinal) {
                        // Игнорируем результат: важнее сохранить локальную правду.
                        runCatching {
                            remoteDataSource.updateHugStatus(local.id, localStatus.name)
                        }
                    }

                    mergedEntities += local.copy(
                        emotionColor = dto.emotion?.color ?: local.emotionColor,
                        emotionPatternId = dto.emotion?.patternId ?: local.emotionPatternId,
                        payloadJson = dto.payload?.toString() ?: local.payloadJson,
                        inReplyToHugId = dto.inReplyToHugId ?: local.inReplyToHugId,
                        deliveredAt = mergedDeliveredAt,
                        status = mergedStatus,
                        createdAt = remoteCreatedAt
                    )
                }

                if (mergedEntities.isNotEmpty()) {
                    localDataSource.upsert(mergedEntities)
                }

                Ok(Unit)
            },
            failure = { error -> Err(error) }
        )
    }

    private fun statusOrder(status: HugStatus): Int = when (status) {
        HugStatus.SENT -> 0
        HugStatus.EXPIRED -> 1
        HugStatus.DELIVERED -> 2
        HugStatus.READ -> 3
    }
}
