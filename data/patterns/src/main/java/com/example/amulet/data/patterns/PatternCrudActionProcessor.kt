package com.example.amulet.data.patterns

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.OutboxActionType
import com.example.amulet.core.network.dto.pattern.PatternSpecDto
import com.example.amulet.core.sync.processing.ActionProcessor
import com.example.amulet.data.patterns.datasource.RemotePatternDataSource
import com.example.amulet.data.patterns.mapper.toDto
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.fold
import javax.inject.Inject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * ActionProcessor для отправки/обновления/удаления/шаринга паттерна на бэкенд.
 * Используется для Outbox-действий PATTERN_CREATE, PATTERN_UPDATE, PATTERN_DELETE, PATTERN_SHARE.
 */
class PatternCrudActionProcessor @Inject constructor(
    private val remoteDataSource: RemotePatternDataSource,
    private val json: Json,
) : ActionProcessor {

    override suspend fun process(action: OutboxActionEntity): AppResult<Unit> {
        return when (action.type) {
            OutboxActionType.PATTERN_CREATE -> handleCreate(action)
            OutboxActionType.PATTERN_UPDATE -> handleUpdate(action)
            OutboxActionType.PATTERN_DELETE -> handleDelete(action)
            OutboxActionType.PATTERN_SHARE -> handleShare(action)
            else -> Err(AppError.Validation(mapOf("type" to "Unsupported action type")))
        }
    }

    private suspend fun handleCreate(action: OutboxActionEntity): AppResult<Unit> {
        Logger.d(
            "Начало обработки Outbox-действия PATTERN_CREATE id=${action.id} target=${action.targetEntityId}",
            TAG
        )

        val payload = parsePayload(action) ?: return Err(AppError.Validation(mapOf("payload" to "Invalid JSON")))

        val kind = payload["kind"]?.jsonPrimitive?.contentOrNull
            ?: return Err(AppError.Validation(mapOf("kind" to "Missing kind")))

        val specElement = payload["spec"]?.takeIfNotNull()
            ?: return Err(AppError.Validation(mapOf("spec" to "Missing spec")))

        val title = payload["title"]?.takeIfNotNull()?.jsonPrimitive?.contentOrNull
        val description = payload["description"]?.takeIfNotNull()?.jsonPrimitive?.contentOrNull

        val hardwareVersion = payload["hardwareVersion"]?.jsonPrimitive?.intOrNull
            ?: return Err(AppError.Validation(mapOf("hardwareVersion" to "Missing hardwareVersion")))

        val specDomain = runCatching {
            when (specElement) {
                is JsonPrimitive -> json.decodeFromString<PatternSpec>(specElement.content)
                else -> json.decodeFromJsonElement<PatternSpec>(specElement)
            }
        }.getOrElse {
            Logger.e(
                "Ошибка декодирования PatternSpec в PATTERN_CREATE id=${action.id}",
                throwable = it,
                tag = TAG
            )
            return Err(AppError.Validation(mapOf("spec" to "Invalid PatternSpec JSON")))
        }

        val specDto = specDomain.toDto()
        val specDtoJson = json.encodeToString(PatternSpecDto.serializer(), specDto)

        Logger.d(
            "Отправка PATTERN_CREATE на сервер kind=$kind title=$title hw=$hardwareVersion",
            TAG
        )

        val result = remoteDataSource.createPattern(
            id = action.targetEntityId,
            kind = kind,
            specJson = specDtoJson,
            title = title,
            description = description,
            tags = null,
            public = null,
            hardwareVersion = hardwareVersion
        )

        return result.fold(
            success = {
                Logger.d(
                    "Outbox PATTERN_CREATE успешно обработан для локального patternId=${action.targetEntityId}",
                    TAG
                )
                Ok(Unit)
            },
            failure = { error ->
                Logger.e(
                    "Ошибка обработки PATTERN_CREATE для локального patternId=${action.targetEntityId}: $error",
                    throwable = Exception(error.toString()),
                    tag = TAG
                )
                Err(error)
            }
        )
    }

    private suspend fun handleUpdate(action: OutboxActionEntity): AppResult<Unit> {
        val patternId = action.targetEntityId
            ?: return Err(AppError.Validation(mapOf("targetEntityId" to "Missing patternId")))

        Logger.d(
            "Начало обработки Outbox-действия PATTERN_UPDATE id=${action.id} target=$patternId",
            TAG
        )

        val payload = parsePayload(action) ?: return Err(AppError.Validation(mapOf("payload" to "Invalid JSON")))

        val version = payload["version"]?.jsonPrimitive?.intOrNull
            ?: return Err(AppError.Validation(mapOf("version" to "Missing version")))

        val title = payload["title"]?.takeIfNotNull()?.jsonPrimitive?.contentOrNull
        val description = payload["description"]?.takeIfNotNull()?.jsonPrimitive?.contentOrNull
        val public = payload["public"]?.takeIfNotNull()?.jsonPrimitive?.booleanOrNull

        val tags: List<String>? = when (val tagsElement = payload["tags"]) {
            null, is JsonNull -> null
            else -> (tagsElement as? JsonArray)?.mapNotNull { it.jsonPrimitive.contentOrNull }
        }

        val specElement = payload["spec"]?.takeIfNotNull()
        val specDtoJson: String? = if (specElement != null) {
            val specDomain = runCatching {
                when (specElement) {
                    is JsonPrimitive -> json.decodeFromString<PatternSpec>(specElement.content)
                    else -> json.decodeFromJsonElement<PatternSpec>(specElement)
                }
            }.getOrElse {
                Logger.e(
                    "Ошибка декодирования PatternSpec в PATTERN_UPDATE id=${action.id} patternId=$patternId",
                    throwable = it,
                    tag = TAG
                )
                return Err(AppError.Validation(mapOf("spec" to "Invalid PatternSpec JSON")))
            }
            val specDto = specDomain.toDto()
            json.encodeToString(PatternSpecDto.serializer(), specDto)
        } else {
            null
        }

        Logger.d(
            "Отправка PATTERN_UPDATE на сервер patternId=$patternId version=$version hasSpec=${specDtoJson != null}",
            TAG
        )

        val result = remoteDataSource.updatePattern(
            patternId = patternId,
            version = version,
            kind = null,
            specJson = specDtoJson,
            title = title,
            description = description,
            tags = tags,
            public = public
        )

        return result.fold(
            success = {
                Logger.d(
                    "Outbox PATTERN_UPDATE успешно обработан для patternId=$patternId",
                    TAG
                )
                Ok(Unit)
            },
            failure = { error ->
                Logger.e(
                    "Ошибка обработки PATTERN_UPDATE для patternId=$patternId: $error",
                    throwable = Exception(error.toString()),
                    tag = TAG
                )
                Err(error)
            }
        )
    }

    private suspend fun handleDelete(action: OutboxActionEntity): AppResult<Unit> {
        val patternId = action.targetEntityId
            ?: return Err(AppError.Validation(mapOf("targetEntityId" to "Missing patternId")))

        Logger.d(
            "Начало обработки Outbox-действия PATTERN_DELETE id=${action.id} target=$patternId",
            TAG
        )

        val result = remoteDataSource.deletePattern(patternId)

        return result.fold(
            success = {
                Logger.d(
                    "Outbox PATTERN_DELETE успешно обработан для patternId=$patternId",
                    TAG
                )
                Ok(Unit)
            },
            failure = { error ->
                // Идемпотентность: если паттерн уже удалён на сервере, считаем операцию успешной.
                if (error is AppError.NotFound) {
                    Logger.d(
                        "PATTERN_DELETE: паттерн уже отсутствует на сервере patternId=$patternId, считаем успехом",
                        TAG
                    )
                    Ok(Unit)
                } else {
                    Logger.e(
                        "Ошибка обработки PATTERN_DELETE для patternId=$patternId: $error",
                        throwable = Exception(error.toString()),
                        tag = TAG
                    )
                    Err(error)
                }
            }
        )
    }

    private suspend fun handleShare(action: OutboxActionEntity): AppResult<Unit> {
        Logger.d(
            "Начало обработки Outbox-действия PATTERN_SHARE id=${action.id} target=${action.targetEntityId}",
            TAG
        )

        val payload = parsePayload(action) ?: return Err(AppError.Validation(mapOf("payload" to "Invalid JSON")))

        val patternId = payload["patternId"]?.jsonPrimitive?.contentOrNull
            ?: action.targetEntityId
            ?: return Err(AppError.Validation(mapOf("patternId" to "Missing patternId")))

        val toUserId = payload["toUserId"]?.jsonPrimitive?.contentOrNull
            ?: return Err(AppError.Validation(mapOf("toUserId" to "Missing toUserId")))

        Logger.d(
            "Отправка PATTERN_SHARE на сервер patternId=$patternId toUserId=$toUserId",
            TAG
        )

        val result = remoteDataSource.sharePattern(
            patternId = patternId,
            toUserId = toUserId,
            pairId = null
        )

        return result.fold(
            success = {
                Logger.d(
                    "Outbox PATTERN_SHARE успешно обработан для patternId=$patternId toUserId=$toUserId",
                    TAG
                )
                Ok(Unit)
            },
            failure = { error ->
                Logger.e(
                    "Ошибка обработки PATTERN_SHARE для patternId=$patternId toUserId=$toUserId: $error",
                    throwable = Exception(error.toString()),
                    tag = TAG
                )
                Err(error)
            }
        )
    }

    private fun parsePayload(action: OutboxActionEntity): Map<String, JsonElement>? {
        return runCatching { json.parseToJsonElement(action.payloadJson).jsonObject }
            .getOrElse {
                Logger.e(
                    "Некорректный payload для действия ${action.type} id=${action.id}",
                    throwable = it,
                    tag = TAG
                )
                null
            }
    }

    private fun JsonElement.takeIfNotNull(): JsonElement? = if (this is JsonNull) null else this

    private companion object {
        private const val TAG = "PatternCrudActionProcessor"
    }
}
