package com.example.amulet.core.network.service

import com.example.amulet.core.network.dto.telemetry.TelemetryAcceptedResponseDto
import com.example.amulet.core.network.dto.telemetry.TelemetryEventsRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface TelemetryApiService {

    @POST("telemetry/events")
    suspend fun sendTelemetry(@Body request: TelemetryEventsRequestDto): TelemetryAcceptedResponseDto
}
