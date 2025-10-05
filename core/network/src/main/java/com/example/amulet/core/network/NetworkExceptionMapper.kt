package com.example.amulet.core.network

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.ExceptionMapper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkExceptionMapper(
    private val json: Json
) : ExceptionMapper {

    /**
     * Преобразует произвольное исключение сетевого/IO уровня в доменную ошибку `AppError`.
     * Обрабатывает HTTP, таймауты, отсутствие сети и прочие IO‑исключения.
     */
    override fun mapToAppError(throwable: Throwable): AppError = when (throwable) {
        is HttpException -> mapHttpException(throwable)
        is SocketTimeoutException -> AppError.Timeout
        is UnknownHostException, is ConnectException -> AppError.Network
        is IOException -> AppError.Network
        else -> AppError.Unknown
    }

    /**
     * Маппит `HttpException` в соответствующий `AppError` по HTTP‑коду и телу ошибки.
     * Для 4xx/5xx кодов пытается разобрать полезную нагрузку для уточнения причины.
     */
    private fun mapHttpException(exception: HttpException): AppError {
        val code = exception.code()
        val payload = parseErrorPayload(exception)

        return when (code) {
            401 -> AppError.Unauthorized
            403 -> AppError.Forbidden
            404 -> AppError.NotFound
            409 -> parseVersionConflict(payload)
            412 -> AppError.PreconditionFailed(parsePreconditionReason(payload))
            422 -> parseValidation(payload)
            429 -> AppError.RateLimited
            in 400..499 -> parseValidation(payload)
            in 500..599 -> AppError.Server(code, exception.message())
            else -> AppError.Unknown
        }
    }


    /**
     * Разбирает конфликт версий (409) и извлекает версию сервера, если она присутствует.
     * Возвращает `AppError.VersionConflict`, иначе общий `AppError.Conflict`.
     */
    private fun parseVersionConflict(payload: JsonElement?): AppError {
        val serverVersion = when (payload) {
            is JsonObject -> {
                payload.intField("serverVersion")
                    ?: payload.intField("currentVersion")
                    ?: payload.jsonObject("details")?.let { details ->
                        details.intField("serverVersion") ?: details.intField("currentVersion")
                    }
            }
            else -> null
        }
        return serverVersion?.let { AppError.VersionConflict(it) } ?: AppError.Conflict
    }

    /**
     * Для ответа с кодом 412 извлекает текстовую причину (reason) из корня или `details`.
     */
    private fun parsePreconditionReason(payload: JsonElement?): String? {
        if (payload !is JsonObject) return null
        return payload.stringField("reason")
            ?: payload.jsonObject("details")?.stringField("reason")
    }

    /**
     * Оборачивает разобранные ошибки в `AppError.Validation`.
     */
    private fun parseValidation(payload: JsonElement?): AppError =
        AppError.Validation(parseValidationErrors(payload))

    /**
     * Разбирает поле `errors` (объект или массив) в карту `поле -> сообщение`.
     * Если структура иная или пустая — возвращает пустую карту.
     */
    private fun parseValidationErrors(payload: JsonElement?): Map<String, String> {
        val root = payload as? JsonObject ?: return emptyMap()
        val errorsElement = root["errors"] ?: payload
        return when (errorsElement) {
            is JsonObject -> errorsElement.entries.associate { (key, value) ->
                key to extractMessage(value)
            }
            is JsonArray -> errorsElement
                .mapIndexed { index, element -> index.toString() to extractMessage(element) }
                .toMap()
            else -> emptyMap()
        }
    }

    /**
     * Унифицирует извлечение текстового сообщения из JSON‑элемента:
     * примитив, массив (конкатенация), объект (ключ:значение).
     */
    private fun extractMessage(element: JsonElement): String = when (element) {
        is JsonPrimitive -> element.content
        is JsonArray -> element.joinToString(",") { child -> extractMessage(child) }
        is JsonObject -> element.entries.joinToString(",") { (key, value) -> "$key:${extractMessage(value)}" }
        else -> element.toString()
    }

    /**
     * Извлекает и парсит тело ошибки из `HttpException` как `JsonElement`.
     * Возвращает `null`, если тело пустое или невалидный JSON.
     */
    private fun parseErrorPayload(exception: HttpException): JsonElement? {
        val errorBody = exception.response()?.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: return null
        return runCatching { json.parseToJsonElement(errorBody) }.getOrNull()
    }

    /**
     * Безопасно извлекает целочисленное поле из `JsonObject`.
     */
    private fun JsonObject.intField(key: String): Int? =
        (this[key] as? JsonPrimitive)?.intOrNull

    /**
     * Безопасно извлекает строковое поле из `JsonObject`.
     */
    private fun JsonObject.stringField(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull

    /**
     * Возвращает дочерний объект по ключу, если он действительно `JsonObject`.
     */
    private fun JsonObject.jsonObject(key: String): JsonObject? =
        this[key] as? JsonObject
}

/**
 * Преобразует содержимое примитива в `Int`, возвращая `null` при неудаче.
 */
private val JsonPrimitive.intOrNull: Int?
    get() = content.toIntOrNull()

/**
 * Возвращает строковое содержимое примитива или `null`, если оно пустое и не строкового типа.
 */
private val JsonPrimitive.contentOrNull: String?
    get() = if (isString || content.isNotEmpty()) content else null
