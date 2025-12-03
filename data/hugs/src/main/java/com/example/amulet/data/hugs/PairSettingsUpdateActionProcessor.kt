package com.example.amulet.data.hugs

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.network.dto.pair.PairMemberSettingsDto
import com.example.amulet.core.sync.processing.ActionProcessor
import com.example.amulet.data.hugs.datasource.remote.PairsRemoteDataSource
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.fold
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PairSettingsUpdateActionProcessor @Inject constructor(
    private val remoteDataSource: PairsRemoteDataSource,
    private val json: Json,
) : ActionProcessor {

    override suspend fun process(action: OutboxActionEntity): AppResult<Unit> {
        val payload = runCatching { json.parseToJsonElement(action.payloadJson).jsonObject }
            .getOrElse { return Err(AppError.Validation(mapOf("payload" to "Invalid JSON"))) }

        val pairId = payload["pairId"]?.jsonPrimitive?.contentOrNull
            ?: return Err(AppError.Validation(mapOf("pairId" to "Missing pairId")))

        val userId = payload["userId"]?.jsonPrimitive?.contentOrNull
            ?: return Err(AppError.Validation(mapOf("userId" to "Missing userId")))

        val muted = payload["muted"]?.jsonPrimitive?.booleanOrNull ?: false
        val quietStart = payload["quietHoursStartMinutes"]?.jsonPrimitive?.intOrNull
        val quietEnd = payload["quietHoursEndMinutes"]?.jsonPrimitive?.intOrNull
        val maxPerHour = payload["maxHugsPerHour"]?.jsonPrimitive?.intOrNull

        val settingsDto = PairMemberSettingsDto(
            muted = muted,
            quietHoursStartMinutes = quietStart,
            quietHoursEndMinutes = quietEnd,
            maxHugsPerHour = maxPerHour,
        )

        val result = remoteDataSource.updateMemberSettings(
            pairId = pairId,
            userId = userId,
            settings = settingsDto,
        )

        return result.fold(
            success = { Ok(Unit) },
            failure = { error -> Err(error) }
        )
    }
}

