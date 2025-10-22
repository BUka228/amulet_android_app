package com.example.amulet.data.devices.datasource.remote

import com.example.amulet.core.network.NetworkExceptionMapper
import com.example.amulet.core.network.dto.device.DeviceClaimRequestDto
import com.example.amulet.core.network.dto.device.DeviceDto
import com.example.amulet.core.network.dto.device.DeviceSettingsDto
import com.example.amulet.core.network.dto.device.DeviceUpdateRequestDto
import com.example.amulet.core.network.safeApiCall
import com.example.amulet.core.network.service.DevicesApiService
import com.example.amulet.shared.core.AppResult
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

/**
 * Реализация источника удаленных данных для устройств.
 */
class DevicesRemoteDataSourceImpl @Inject constructor(
    private val apiService: DevicesApiService,
    private val exceptionMapper: NetworkExceptionMapper
) : DevicesRemoteDataSource {
    
    override suspend fun claimDevice(
        serial: String,
        claimToken: String,
        name: String?
    ): AppResult<DeviceDto> = safeApiCall(exceptionMapper) {
        val request = DeviceClaimRequestDto(
            serial = serial,
            claimToken = claimToken,
            name = name
        )
        apiService.claimDevice(request).device
    }
    
    override suspend fun unclaimDevice(deviceId: String): AppResult<Unit> = safeApiCall(exceptionMapper) {
        apiService.unclaimDevice(deviceId)
        Unit
    }
    
    override suspend fun fetchDevices(): AppResult<List<DeviceDto>> = safeApiCall(exceptionMapper) {
        apiService.getDevices().devices
    }
    
    override suspend fun fetchDevice(deviceId: String): AppResult<DeviceDto> = safeApiCall(exceptionMapper) {
        apiService.getDevice(deviceId).device
    }
    
    override suspend fun updateDevice(
        deviceId: String,
        name: String?,
        brightness: Double?,
        haptics: Double?,
        gestures: Map<String, String>?
    ): AppResult<DeviceDto> = safeApiCall(exceptionMapper) {
        val settings = if (brightness != null || haptics != null || gestures != null) {
            DeviceSettingsDto(
                brightness = brightness,
                haptics = haptics,
                gestures = gestures?.let { map ->
                    JsonObject(map.mapValues { JsonPrimitive(it.value) })
                }
            )
        } else null
        
        val request = DeviceUpdateRequestDto(
            name = name,
            settings = settings
        )
        
        apiService.updateDevice(deviceId, request).device
    }
}
