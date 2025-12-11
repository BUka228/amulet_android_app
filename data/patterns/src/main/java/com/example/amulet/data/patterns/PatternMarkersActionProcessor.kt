package com.example.amulet.data.patterns

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.OutboxActionType
import com.example.amulet.core.network.dto.pattern.PatternMarkersDto
import com.example.amulet.core.sync.processing.ActionProcessor
import com.example.amulet.data.patterns.datasource.RemotePatternDataSource
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.fold
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PatternMarkersActionProcessor @Inject constructor(
    private val remoteDataSource: RemotePatternDataSource,
    private val json: Json,
) : ActionProcessor {

    override suspend fun process(action: OutboxActionEntity): AppResult<Unit> {
        if (action.type != OutboxActionType.PATTERN_MARKERS_UPDATE) {
            return Err(AppError.Validation(mapOf("type" to "Unsupported action type")))
        }

        val payload = runCatching { json.parseToJsonElement(action.payloadJson).jsonObject }
            .getOrElse { return Err(AppError.Validation(mapOf("payload" to "Invalid JSON"))) }

        val patternId = payload["patternId"]?.jsonPrimitive?.contentOrNull
            ?: return Err(AppError.Validation(mapOf("patternId" to "Missing patternId")))

        val markersArray: JsonArray? = payload["markersMs"]?.jsonArray
        val markersMs: List<Int> = markersArray?.mapNotNull { it.jsonPrimitive.intOrNull } ?: emptyList()

        val result = if (markersMs.isEmpty()) {
            remoteDataSource.deletePatternMarkers(patternId)
        } else {
            val dto = PatternMarkersDto(patternId = patternId, markersMs = markersMs)
            remoteDataSource.upsertPatternMarkers(dto)
        }

        return result.fold(
            success = { Ok(Unit) },
            failure = { error -> Err(error) }
        )
    }
}
