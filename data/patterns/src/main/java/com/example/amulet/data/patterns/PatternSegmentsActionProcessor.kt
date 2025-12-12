package com.example.amulet.data.patterns

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.OutboxActionType
import com.example.amulet.core.network.dto.pattern.PatternDto
import com.example.amulet.core.network.dto.pattern.PatternSpecDto
import com.example.amulet.core.sync.processing.ActionProcessor
import com.example.amulet.data.patterns.datasource.LocalPatternDataSource
import com.example.amulet.data.patterns.datasource.RemotePatternDataSource
import com.example.amulet.data.patterns.mapper.toDomain
import com.example.amulet.data.patterns.mapper.toDtoForSegments
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
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

        Logger.d(
            "Начало обработки Outbox-действия PATTERN_SEGMENTS_UPDATE id=${action.id} target=${action.targetEntityId}",
            TAG
        )

        val payload = runCatching { json.parseToJsonElement(action.payloadJson).jsonObject }
            .getOrElse {
                Logger.e(
                    "Некорректный payload для PATTERN_SEGMENTS_UPDATE id=${action.id}",
                    throwable = it,
                    tag = TAG
                )
                return Err(AppError.Validation(mapOf("payload" to "Invalid JSON")))
            }

        val patternId = payload["patternId"]?.jsonPrimitive?.contentOrNull
            ?: return Err(AppError.Validation(mapOf("patternId" to "Missing patternId")))

        val entities = localDataSource.getSegmentsForPattern(patternId)
        Logger.d(
            "Найдено локальных сегментов для паттерна $patternId: ${entities.size}",
            TAG
        )

        val dtos = entities.map { entity ->
            val domainPattern = try {
                entity.toDomain()
            } catch (e: Exception) {
                Logger.e(
                    "Ошибка декодирования PatternSpec для сегмента id=${entity.id} patternId=$patternId: ${entity.specJson}",
                    throwable = e,
                    tag = TAG
                )
                return Err(
                    AppError.Validation(
                        mapOf("specJson" to "Invalid spec for segment ${entity.id} of pattern $patternId")
                    )
                )
            }

            domainPattern.toDtoForSegments()
        }

        Logger.d(
            "Отправка ${dtos.size} сегментов паттерна $patternId на сервер (PATTERN_SEGMENTS_UPDATE)",
            TAG
        )

        val result = remoteDataSource.upsertPatternSegments(patternId, dtos)

        return result.fold(
            success = {
                Logger.d(
                    "Outbox PATTERN_SEGMENTS_UPDATE успешно обработан для patternId=$patternId",
                    TAG
                )
                Ok(Unit)
            },
            failure = { error ->
                Logger.e(
                    "Ошибка обработки PATTERN_SEGMENTS_UPDATE для patternId=$patternId: $error",
                    throwable = Exception(error.toString()),
                    tag = TAG
                )
                Err(error)
            }
        )
    }

    private companion object {
        private const val TAG = "PatternSegmentsActionProcessor"
    }
}
