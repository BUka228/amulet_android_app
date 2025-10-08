package com.example.amulet.core.network.service

import com.example.amulet.core.network.dto.device.DeviceClaimRequestDto
import com.example.amulet.core.network.dto.device.DeviceResponseDto
import com.example.amulet.core.network.dto.device.DeviceUpdateRequestDto
import com.example.amulet.core.network.dto.device.DeviceUnclaimResponseDto
import com.example.amulet.core.network.dto.device.DevicesResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Path

interface DevicesApiService {

    @POST("devices.claim")
    suspend fun claimDevice(@Body request: DeviceClaimRequestDto): DeviceResponseDto

    @GET("devices")
    suspend fun getDevices(): DevicesResponseDto

    @GET("devices/{id}")
    suspend fun getDevice(@Path("id") deviceId: String): DeviceResponseDto

    @PATCH("devices/{id}")
    suspend fun updateDevice(
        @Path("id") deviceId: String,
        @Body request: DeviceUpdateRequestDto
    ): DeviceResponseDto

    @POST("devices/{id}/unclaim")
    suspend fun unclaimDevice(@Path("id") deviceId: String): DeviceUnclaimResponseDto
}
