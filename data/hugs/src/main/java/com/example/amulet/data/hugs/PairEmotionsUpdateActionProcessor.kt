package com.example.amulet.data.hugs

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.network.dto.pair.PairEmotionDto
import com.example.amulet.core.network.dto.pair.PairEmotionUpdateRequestDto
import com.example.amulet.core.sync.processing.ActionProcessor
import com.example.amulet.data.hugs.datasource.remote.PairsRemoteDataSource
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.fold
import javax.inject.Inject
import kotlinx.serialization.json.Json

class PairEmotionsUpdateActionProcessor @Inject constructor(
    private val remoteDataSource: PairsRemoteDataSource,
    private val json: Json,
) : ActionProcessor {

    override suspend fun process(action: OutboxActionEntity): AppResult<Unit> {
        val request = runCatching {
            json.decodeFromString(PairEmotionUpdateRequestDto.serializer(), action.payloadJson)
        }.getOrElse {
            return Err(AppError.Validation(mapOf("payload" to "Invalid JSON")))
        }

        val emotions: List<PairEmotionDto> = request.emotions
        val pairId = action.targetEntityId
            ?: return Err(AppError.Validation(mapOf("pairId" to "Missing pairId")))

        val result = remoteDataSource.updatePairEmotions(pairId = pairId, emotions = emotions)

        return result.fold(
            success = { Ok(Unit) },
            failure = { error -> Err(error) },
        )
    }
}
