package com.example.amulet.data.patterns

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.OutboxActionType
import com.example.amulet.core.network.dto.pattern.PatternDto
import com.example.amulet.core.network.dto.pattern.PatternSpecDto
import com.example.amulet.core.sync.processing.ActionProcessor
import com.example.amulet.data.patterns.datasource.LocalPatternDataSource
import com.example.amulet.data.patterns.datasource.RemotePatternDataSource
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.fold
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PatternSegmentsActionProcessor @Inject constructor(
    private val localDataSource: LocalPatternDataSource,
    private val remoteDataSource: RemotePatternDataSource,
    private val json: Json,
) : ActionProcessor {

    override suspend fun process(action: OutboxActionEntity): AppResult<Unit> {
        if (action.type != OutboxActionType.PATTERN_SEGMENTS_UPDATE) {
            return Err(AppError.Validation(mapOf("type" to "Unsupported action type")))
        }

        val payload = runCatching { json.parseToJsonElement(action.payloadJson).jsonObject }
            .getOrElse { return Err(AppError.Validation(mapOf("payload" to "Invalid JSON"))) }

        val patternId = payload["patternId"]?.jsonPrimitive?.contentOrNull
            ?: return Err(AppError.Validation(mapOf("patternId" to "Missing patternId")))

        val entities = localDataSource.getSegmentsForPattern(patternId)

        val dtos = entities.map { entity ->
            val specDto = json.decodeFromString(PatternSpecDto.serializer(), entity.specJson)
            PatternDto(
                id = entity.id,
                version = entity.version,
                ownerId = entity.ownerId,
                kind = entity.kind,
                spec = specDto,
                public = entity.public,
                reviewStatus = entity.reviewStatus,
                hardwareVersion = entity.hardwareVersion,
                title = entity.title,
                description = entity.description,
                tags = null,
                usageCount = entity.usageCount,
                sharedWith = null,
                createdAt = null,
                updatedAt = null,
                parentPatternId = entity.parentPatternId,
                segmentIndex = entity.segmentIndex,
                segmentStartMs = entity.segmentStartMs,
                segmentEndMs = entity.segmentEndMs,
            )
        }

        val result = remoteDataSource.upsertPatternSegments(patternId, dtos)

        return result.fold(
            success = { Ok(Unit) },
            failure = { error -> Err(error) }
        )
    }
}
