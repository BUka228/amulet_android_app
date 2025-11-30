package com.example.amulet.data.hugs

import com.example.amulet.core.database.entity.PairEmotionEntity
import com.example.amulet.core.database.entity.PairQuickReplyEntity
import com.example.amulet.core.database.relation.PairWithMemberSettings
import com.example.amulet.data.hugs.datasource.local.PairsLocalDataSource
import com.example.amulet.data.hugs.datasource.remote.PairsRemoteDataSource
import com.example.amulet.core.network.dto.pair.PairEmotionDto
import com.example.amulet.core.network.dto.pair.PairMemberSettingsDto
import com.example.amulet.core.network.dto.pair.PairQuickReplyDto
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.PairsRepository
import com.example.amulet.shared.domain.hugs.model.Pair
import com.example.amulet.shared.domain.hugs.model.PairEmotion
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.hugs.model.PairMemberSettings
import com.example.amulet.shared.domain.hugs.model.PairQuickReply
import com.example.amulet.shared.domain.user.model.UserId
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.fold
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PairsRepositoryImpl @Inject constructor(
    private val localDataSource: PairsLocalDataSource,
    private val remoteDataSource: PairsRemoteDataSource
) : PairsRepository {

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
        val dto = PairMemberSettingsDto(
            muted = settings.muted,
            quietHoursStartMinutes = settings.quietHoursStartMinutes,
            quietHoursEndMinutes = settings.quietHoursEndMinutes,
            maxHugsPerHour = settings.maxHugsPerHour
        )

        val remoteResult = remoteDataSource.updateMemberSettings(
            pairId = pairId.value,
            userId = userId.value,
            settings = dto
        )

        return remoteResult.fold(
            success = {
                localDataSource.updateMemberSettings(
                    pairId = pairId.value,
                    userId = userId.value,
                    muted = settings.muted,
                    quietStart = settings.quietHoursStartMinutes,
                    quietEnd = settings.quietHoursEndMinutes,
                    maxHugsPerHour = settings.maxHugsPerHour
                )
                Ok(Unit)
            },
            failure = { error ->
                Ok(Unit)
            }
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
                emotionId = requireNotNull(it.emotionId) { "emotionId must not be null when updating quick replies" }
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
