package com.example.amulet.core.telemetry.logging

import com.example.amulet.core.network.NetworkExceptionMapper
import com.example.amulet.core.network.dto.telemetry.TelemetryEventDto
import com.example.amulet.core.network.dto.telemetry.TelemetryEventsRequestDto
import com.example.amulet.core.network.safeApiCall
import com.example.amulet.core.network.service.TelemetryApiService
import io.github.aakira.napier.LogLevel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Singleton
class NetworkTelemetryReporter @Inject constructor(
    private val telemetryApiService: TelemetryApiService,
    private val exceptionMapper: NetworkExceptionMapper
) : TelemetryReporter {

    override suspend fun reportLog(level: LogLevel, tag: String?, message: String?, throwable: Throwable?) {
        val params = buildParams(level, tag, message, throwable)
        val event = TelemetryEventDto(
            type = TELEMETRY_LOG_TYPE,
            timestamp = System.currentTimeMillis(),
            params = params
        )

        safeApiCall(exceptionMapper) {
            telemetryApiService.sendTelemetry(TelemetryEventsRequestDto(listOf(event)))
        }
    }

    private fun buildParams(
        level: LogLevel,
        tag: String?,
        message: String?,
        throwable: Throwable?
    ): JsonObject = buildJsonObject {
        put("level", level.name)
        tag?.let { put("tag", it) }
        message?.let { put("message", it) }
        throwable?.let { put("throwable", it.stackTraceToString()) }
    }

    private companion object {
        private const val TELEMETRY_LOG_TYPE = "app_log"
    }
}
