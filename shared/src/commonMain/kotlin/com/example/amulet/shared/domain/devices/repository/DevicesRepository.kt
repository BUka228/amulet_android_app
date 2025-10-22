package com.example.amulet.shared.domain.devices.repository

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.model.Device
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для управления устройствами амулета.
 * Инкапсулирует работу с API, локальной БД и BLE подключениями.
 */
interface DevicesRepository {
    
    /**
     * Наблюдать за списком устройств текущего пользователя.
     * Источник истины - локальная БД.
     */
    fun observeDevices(): Flow<List<Device>>
    
    /**
     * Получить устройство по ID.
     *
     * @param deviceId ID устройства
     */
    suspend fun getDevice(deviceId: String): AppResult<Device>
    
    /**
     * Привязать устройство к аккаунту пользователя.
     *
     * @param serial Серийный номер устройства
     * @param claimToken Токен для привязки (из QR/NFC)
     * @param name Опциональное имя устройства
     */
    suspend fun claimDevice(
        serial: String,
        claimToken: String,
        name: String? = null
    ): AppResult<Device>
    
    /**
     * Отвязать устройство от аккаунта.
     *
     * @param deviceId ID устройства
     */
    suspend fun unclaimDevice(deviceId: String): AppResult<Unit>
    
    /**
     * Обновить настройки устройства.
     *
     * @param deviceId ID устройства
     * @param name Новое имя устройства
     * @param brightness Яркость LED (0.0-1.0)
     * @param haptics Интенсивность вибрации (0.0-1.0)
     * @param gestures Пользовательские жесты
     */
    suspend fun updateDeviceSettings(
        deviceId: String,
        name: String? = null,
        brightness: Double? = null,
        haptics: Double? = null,
        gestures: Map<String, String>? = null
    ): AppResult<Device>
    
    /**
     * Синхронизировать устройства с сервером.
     * Загружает актуальный список устройств и обновляет локальную БД.
     */
    suspend fun syncDevices(): AppResult<Unit>
    
    /**
     * Сканировать BLE устройства амулета.
     *
     * @param timeoutMs Таймаут сканирования
     * @param serialNumberFilter Фильтр по серийному номеру (для паринга конкретного устройства)
     */
    fun scanForDevices(
        timeoutMs: Long = 10_000L,
        serialNumberFilter: String? = null
    ): Flow<ScannedDeviceInfo>
    
    /**
     * Подключиться к устройству по MAC адресу.
     *
     * @param deviceAddress MAC адрес устройства
     */
    suspend fun connectToDevice(deviceAddress: String): AppResult<Unit>
    
    /**
     * Отключиться от устройства.
     */
    suspend fun disconnectFromDevice(): AppResult<Unit>
    
    /**
     * Наблюдать за состоянием BLE подключения.
     */
    fun observeConnectionState(): Flow<ConnectionStatus>
    
    /**
     * Наблюдать за статусом устройства (батарея, прошивка и т.д.).
     */
    fun observeDeviceStatus(): Flow<DeviceLiveStatus?>
}

/**
 * Информация о найденном устройстве при сканировании.
 */
data class ScannedDeviceInfo(
    val name: String,
    val address: String,
    val rssi: Int,
    val serialNumber: String?
)

/**
 * Статус BLE подключения.
 */
enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    FAILED
}

/**
 * Живой статус устройства (через BLE).
 */
data class DeviceLiveStatus(
    val serialNumber: String,
    val firmwareVersion: String,
    val hardwareVersion: Int,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val isOnline: Boolean
)
