package com.example.amulet.data.hugs

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.OutboxActionStatus
import com.example.amulet.core.database.entity.OutboxActionType
import com.example.amulet.core.database.entity.PairEmotionEntity
import com.example.amulet.core.database.entity.PairEntity
import com.example.amulet.core.database.entity.PairMemberEntity
import com.example.amulet.core.database.entity.PairQuickReplyEntity
import com.example.amulet.core.database.relation.PairWithMemberSettings
import com.example.amulet.core.network.dto.pair.PairEmotionDto
import com.example.amulet.core.network.dto.pair.PairMemberSettingsDto
import com.example.amulet.core.network.dto.pair.PairQuickReplyDto
import com.example.amulet.core.sync.scheduler.OutboxScheduler
import com.example.amulet.data.hugs.datasource.local.PairsLocalDataSource
import com.example.amulet.data.hugs.datasource.remote.PairsRemoteDataSource
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.hugs.PairsRepository
import com.example.amulet.shared.domain.hugs.model.Pair
import com.example.amulet.shared.domain.hugs.model.PairEmotion
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.hugs.model.PairInvite
import com.example.amulet.shared.domain.hugs.model.PairMemberSettings
import com.example.amulet.shared.domain.hugs.model.PairQuickReply
import com.example.amulet.shared.domain.user.model.UserId
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

@Singleton
class PairsRepositoryImpl @Inject constructor(
    private val localDataSource: PairsLocalDataSource,
    private val remoteDataSource: PairsRemoteDataSource,
    private val outboxScheduler: OutboxScheduler,
    private val json: Json,
) : PairsRepository {
    override suspend fun invitePair(method: String, target: String?): AppResult<PairInvite> {
        Logger.d("PairsRepositoryImpl.invitePair(method=$method, target=$target)", "PairsRepositoryImpl")
        val result = remoteDataSource.invitePair(method, target).map { dto ->
            // На доменном уровне по-прежнему используем inviteId как код приглашения,
            // а URL формируем на клиенте. Новый ответ даёт pairId и status.
            PairInvite(inviteId = dto.pairId, url = null)
        }
        result.component1()?.let { invite ->
            Logger.d(
                "PairsRepositoryImpl.invitePair: success inviteId=${invite.inviteId} url=${invite.url}",
                "PairsRepositoryImpl"
            )
        }
        result.component2()?.let { error ->
            Logger.e(
                "PairsRepositoryImpl.invitePair: failure error=$error",
                throwable = Exception(error.toString()),
                tag = "PairsRepositoryImpl"
            )
        }
        return result
    }

    override suspend fun acceptPair(inviteId: String): AppResult<Unit> {
        Logger.d("PairsRepositoryImpl.acceptPair(inviteId=$inviteId)", "PairsRepositoryImpl")
        val result = remoteDataSource.acceptPair(inviteId).map { Unit }
        result.component1()?.let {
            Logger.d("PairsRepositoryImpl.acceptPair: success inviteId=$inviteId", "PairsRepositoryImpl")
        }
        result.component2()?.let { error ->
            Logger.e(
                "PairsRepositoryImpl.acceptPair: failure error=$error",
                throwable = Exception(error.toString()),
                tag = "PairsRepositoryImpl"
            )
        }
        return result
    }

    override suspend fun syncPairs(): AppResult<Unit> {
        Logger.d("PairsRepositoryImpl.syncPairs: start", "PairsRepositoryImpl")
        val remoteResult = remoteDataSource.getPairs()

        return remoteResult.fold(
            success = { response ->
                val pairs = response.pairs

                val pairEntities = pairs.map { dto ->
                    PairEntity(
                        id = dto.id,
                        status = dto.status ?: "active",
                        blockedBy = dto.blockedBy,
                        blockedAt = dto.blockedAt?.value,
                        createdAt = dto.createdAt?.value ?: System.currentTimeMillis(),
                    )
                }

                val memberEntities = pairs.flatMap { dto ->
                    val joinedAt = dto.createdAt?.value ?: System.currentTimeMillis()
                    dto.memberIds.map { userId ->
                        PairMemberEntity(
                            pairId = dto.id,
                            userId = userId,
                            joinedAt = joinedAt,
                            muted = false,
                            quietHoursStartMinutes = null,
                            quietHoursEndMinutes = null,
                            maxHugsPerHour = null,
                        )
                    }
                }

                localDataSource.replaceAllPairs(pairEntities, memberEntities)

                Logger.d(
                    "PairsRepositoryImpl.syncPairs: success, pairsCount=${pairs.size}",
                    "PairsRepositoryImpl"
                )

                Ok(Unit)
            },
            failure = { error ->
                Logger.e(
                    "PairsRepositoryImpl.syncPairs: failure error=$error",
                    throwable = Exception(error.toString()),
                    tag = "PairsRepositoryImpl"
                )
                Err(error)
            }
        )
    }

    override fun observePairs(): Flow<List<Pair>> =
        localDataSource.observeAllWithSettings()
            .map { list -> list.map(PairWithMemberSettings::toDomain) }

    override fun observePair(pairId: PairId): Flow<Pair?> =
        localDataSource.observePairWithSettings(pairId.value)
            .map { it?.toDomain() }

    override fun observePairEmotions(pairId: PairId): Flow<List<PairEmotion>> =
        localDataSource.observeEmotions(pairId.value)
            .map { list -> list.map(PairEmotionEntity::toDomain) }

    override suspend fun updatePairEmotions(
        pairId: PairId,
        emotions: List<PairEmotion>
    ): AppResult<Unit> {
        val dtos = emotions.map {
            PairEmotionDto(
                id = it.id,
                pairId = it.pairId.value,
                name = it.name,
                colorHex = it.colorHex,
                patternId = it.patternId?.value,
                order = it.order
            )
        }

        val remoteResult = remoteDataSource.updatePairEmotions(pairId.value, dtos)

        return remoteResult.fold(
            success = { response ->
                val entities = response.emotions.map { dto ->
                    PairEmotionEntity(
                        id = dto.id,
                        pairId = dto.pairId,
                        name = dto.name,
                        colorHex = dto.colorHex,
                        patternId = dto.patternId,
                        order = dto.order
                    )
                }
                localDataSource.upsertEmotions(entities)
                Ok(Unit)
            },
            failure = { error ->
                Ok(Unit)
            }
        )
    }

    override suspend fun updateMemberSettings(
        pairId: PairId,
        userId: UserId,
        settings: PairMemberSettings
    ): AppResult<Unit> {
        val nowMillis = System.currentTimeMillis()

        val payloadObject = buildJsonObject {
            put("pairId", pairId.value)
            put("userId", userId.value)
            put("muted", settings.muted)
            val quietStart = settings.quietHoursStartMinutes
            val quietEnd = settings.quietHoursEndMinutes
            val maxPerHour = settings.maxHugsPerHour
            if (quietStart != null) {
                put("quietHoursStartMinutes", quietStart)
            } else {
                put("quietHoursStartMinutes", JsonNull)
            }
            if (quietEnd != null) {
                put("quietHoursEndMinutes", quietEnd)
            } else {
                put("quietHoursEndMinutes", JsonNull)
            }
            if (maxPerHour != null) {
                put("maxHugsPerHour", maxPerHour)
            } else {
                put("maxHugsPerHour", JsonNull)
            }
        }

        val payload = json.encodeToString(payloadObject)

        localDataSource.withPairTransaction {
            localDataSource.updateMemberSettings(
                pairId = pairId.value,
                userId = userId.value,
                muted = settings.muted,
                quietStart = settings.quietHoursStartMinutes,
                quietEnd = settings.quietHoursEndMinutes,
                maxHugsPerHour = settings.maxHugsPerHour
            )

            val action = OutboxActionEntity(
                id = UUID.randomUUID().toString(),
                type = OutboxActionType.PAIR_SETTINGS_UPDATE,
                payloadJson = payload,
                status = OutboxActionStatus.PENDING,
                retryCount = 0,
                lastError = null,
                idempotencyKey = "pair_settings_${pairId.value}_${userId.value}",
                createdAt = nowMillis,
                updatedAt = nowMillis,
                availableAt = nowMillis,
                priority = 1,
                targetEntityId = pairId.value
            )

            localDataSource.enqueueOutboxAction(action)
        }

        outboxScheduler.scheduleSync()

        return Ok(Unit)
    }

    override suspend fun blockPair(pairId: PairId): AppResult<Unit> =
        remoteDataSource.blockPair(pairId.value).map { Unit }

    override suspend fun unblockPair(pairId: PairId): AppResult<Unit> =
        remoteDataSource.unblockPair(pairId.value).map { Unit }

    override suspend fun deletePair(pairId: PairId): AppResult<Unit> {
        val remoteResult = remoteDataSource.deletePair(pairId.value)
        return remoteResult.fold(
            success = {
                localDataSource.deletePair(pairId.value)
                Ok(Unit)
            },
            failure = { error -> Err(error) }
        )
    }

    override fun observeQuickReplies(
        pairId: PairId,
        userId: UserId
    ): Flow<List<PairQuickReply>> =
        localDataSource.observeQuickReplies(pairId.value, userId.value)
            .map { list -> list.map(PairQuickReplyEntity::toDomain) }

    override suspend fun updateQuickReplies(
        pairId: PairId,
        userId: UserId,
        replies: List<PairQuickReply>
    ): AppResult<Unit> {
        val dtos = replies.map {
            PairQuickReplyDto(
                pairId = it.pairId.value,
                userId = it.userId.value,
                gestureType = it.gestureType.name,
                emotionId = it.emotionId
            )
        }

        val remoteResult = remoteDataSource.updateQuickReplies(pairId.value, dtos)

        return remoteResult.fold(
            success = { response ->
                val entities = response.replies.map { dto ->
                    PairQuickReplyEntity(
                        pairId = dto.pairId,
                        userId = dto.userId,
                        gestureType = dto.gestureType,
                        emotionId = dto.emotionId
                    )
                }
                localDataSource.upsertQuickReplies(entities)
                Ok(Unit)
            },
            failure = { error ->
                Ok(Unit)
            }
        )
    }
}
