package com.example.amulet.data.devices.datasource.remote

import com.example.amulet.core.network.dto.device.DeviceDto
import com.example.amulet.shared.core.AppResult

/**
 * Источник удаленных данных для устройств.
 * Инкапсулирует работу с DevicesApiService и маппинг сетевых ошибок.
 */
interface DevicesRemoteDataSource {
    
    /**
     * Привязать устройство к аккаунту пользователя.
     *
     * @param serial Серийный номер устройства
     * @param claimToken Токен для привязки (из QR/NFC)
     * @param name Опциональное имя устройства
     * @return Данные привязанного устройства
     */
    suspend fun claimDevice(
        serial: String,
        claimToken: String,
        name: String?
    ): AppResult<DeviceDto>
    
    /**
     * Отвязать устройство от аккаунта.
     *
     * @param deviceId ID устройства
     */
    suspend fun unclaimDevice(deviceId: String): AppResult<Unit>
    
    /**
     * Получить список всех устройств пользователя.
     */
    suspend fun fetchDevices(): AppResult<List<DeviceDto>>
    
    /**
     * Получить детальную информацию об устройстве.
     *
     * @param deviceId ID устройства
     */
    suspend fun fetchDevice(deviceId: String): AppResult<DeviceDto>
    
    /**
     * Обновить настройки устройства.
     *
     * @param deviceId ID устройства
     * @param name Новое имя устройства
     * @param brightness Яркость (0.0-1.0)
     * @param haptics Интенсивность вибрации (0.0-1.0)
     * @param gestures Пользовательские жесты
     */
    suspend fun updateDevice(
        deviceId: String,
        name: String?,
        brightness: Double?,
        haptics: Double?,
        gestures: Map<String, String>?
    ): AppResult<DeviceDto>
}
