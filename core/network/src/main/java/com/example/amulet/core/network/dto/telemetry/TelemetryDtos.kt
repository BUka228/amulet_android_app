package com.example.amulet.core.network.dto.telemetry

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class TelemetryEventDto(
    val type: String,
    val timestamp: Long,
    val params: JsonObject? = null,
    val sessionId: String? = null,
    val practiceId: String? = null
)

@Serializable
data class TelemetryEventsRequestDto(
    val events: List<TelemetryEventDto>
)

@Serializable
data class TelemetryAcceptedResponseDto(
    val success: Boolean,
    val data: TelemetryAcceptedDataDto? = null
)

@Serializable
data class TelemetryAcceptedDataDto(
    val accepted: Int? = null
)
