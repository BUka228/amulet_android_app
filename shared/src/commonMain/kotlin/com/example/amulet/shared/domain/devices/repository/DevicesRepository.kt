package com.example.amulet.shared.domain.devices.repository

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.model.ConnectionStatus
import com.example.amulet.shared.domain.devices.model.Device
import com.example.amulet.shared.domain.devices.model.DeviceConnectionProgress
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.model.DeviceLiveStatus
import com.example.amulet.shared.domain.devices.model.PairingDeviceFound
import com.example.amulet.shared.domain.devices.model.PairingProgress
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для управления устройствами амулета.
 * Инкапсулирует работу с API, локальной БД и BLE подключениями.
 * 
 * Все BLE-специфичные детали (MAC адреса, сканирование, GATT) скрыты внутри.
 * UseCase'ы работают только с доменными концепциями (серийный номер, статус и т.д.).
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
    suspend fun getDevice(deviceId: DeviceId): AppResult<Device>
    
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
        deviceId: DeviceId,
        name: String? = null,
        brightness: Double? = null,
        haptics: Double? = null,
        gestures: Map<String, String>? = null
    ): AppResult<Device>
    
    /**
     * Отвязать устройство от аккаунта.
     *
     * @param deviceId ID устройства
     */
    suspend fun unclaimDevice(deviceId: DeviceId): AppResult<Unit>
    
    /**
     * Синхронизировать устройства с сервером.
     * Загружает актуальный список устройств и обновляет локальную БД.
     */
    suspend fun syncDevices(): AppResult<Unit>
    
    // ========== Паринг и подключение ==========
    
    /**
     * Сканировать устройства для паринга.
     * Используется на экране паринга для показа найденных устройств.
     *
     * @param serialNumberFilter Фильтр по серийному номеру (из QR/NFC)
     * @param timeoutMs Таймаут сканирования
     * @return Flow с найденными устройствами
     */
    fun scanForPairing(
        serialNumberFilter: String? = null,
        timeoutMs: Long = 10_000L
    ): Flow<PairingDeviceFound>
    
    /**
     * Подключиться и привязать новое устройство (полный паринг флоу).
     * Инкапсулирует: сканирование BLE -> подключение -> claim на сервере -> конфигурация.
     *
     * @param serialNumber Серийный номер устройства (из QR/NFC)
     * @param claimToken Токен для привязки (из QR/NFC)
     * @param deviceName Опциональное имя устройства
     * @return Flow с прогрессом паринга
     */
    fun pairAndClaimDevice(
        serialNumber: String,
        claimToken: String,
        deviceName: String? = null
    ): Flow<PairingProgress>
    
    /**
     * Подключиться к уже привязанному устройству по серийному номеру.
     * Инкапсулирует: сканирование BLE -> подключение -> GATT discovery.
     *
     * @param serialNumber Серийный номер устройства
     * @param timeoutMs Таймаут поиска устройства
     * @return Flow с прогрессом подключения
     */
    fun connectToDevice(
        serialNumber: String,
        timeoutMs: Long = 30_000L
    ): Flow<DeviceConnectionProgress>
    
    /**
     * Отключиться от текущего подключенного устройства.
     */
    suspend fun disconnectFromDevice(): AppResult<Unit>
    
    /**
     * Наблюдать за состоянием BLE подключения.
     */
    fun observeConnectionState(): Flow<ConnectionStatus>
    
    /**
     * Наблюдать за статусом подключенного устройства (батарея, прошивка и т.д.).
     */
    fun observeConnectedDeviceStatus(): Flow<DeviceLiveStatus?>
}
