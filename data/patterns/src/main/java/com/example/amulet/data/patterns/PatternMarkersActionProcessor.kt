package com.example.amulet.data.patterns

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.OutboxActionType
import com.example.amulet.core.network.dto.pattern.PatternMarkersDto
import com.example.amulet.core.sync.processing.ActionProcessor
import com.example.amulet.data.patterns.datasource.RemotePatternDataSource
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
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

        Logger.d(
            "Начало обработки Outbox-действия PATTERN_MARKERS_UPDATE id=${action.id} target=${action.targetEntityId}",
            TAG
        )

        val payload = runCatching { json.parseToJsonElement(action.payloadJson).jsonObject }
            .getOrElse {
                Logger.e(
                    "Некорректный payload для PATTERN_MARKERS_UPDATE id=${action.id}",
                    throwable = it,
                    tag = TAG
                )
                return Err(AppError.Validation(mapOf("payload" to "Invalid JSON")))
            }

        val patternId = payload["patternId"]?.jsonPrimitive?.contentOrNull
            ?: return Err(AppError.Validation(mapOf("patternId" to "Missing patternId")))

        val markersArray: JsonArray? = payload["markersMs"]?.jsonArray
        val markersMs: List<Int> = markersArray?.mapNotNull { it.jsonPrimitive.intOrNull } ?: emptyList()

        Logger.d(
            "Обработка маркеров для паттерна $patternId: count=${markersMs.size}",
            TAG
        )

        val result = if (markersMs.isEmpty()) {
            Logger.d(
                "markersMs пустой — отправляем DELETE маркеров для patternId=$patternId",
                TAG
            )
            remoteDataSource.deletePatternMarkers(patternId)
        } else {
            val dto = PatternMarkersDto(patternId = patternId, markersMs = markersMs)
            Logger.d(
                "Отправка ${markersMs.size} маркеров на сервер (PATTERN_MARKERS_UPDATE) для patternId=$patternId",
                TAG
            )
            remoteDataSource.upsertPatternMarkers(dto)
        }

        return result.fold(
            success = {
                Logger.d(
                    "Outbox PATTERN_MARKERS_UPDATE успешно обработан для patternId=$patternId",
                    TAG
                )
                Ok(Unit)
            },
            failure = { error ->
                Logger.e(
                    "Ошибка обработки PATTERN_MARKERS_UPDATE для patternId=$patternId: $error",
                    throwable = Exception(error.toString()),
                    tag = TAG
                )
                Err(error)
            }
        )
    }

    private companion object {
        private const val TAG = "PatternMarkersActionProcessor"
    }
}
