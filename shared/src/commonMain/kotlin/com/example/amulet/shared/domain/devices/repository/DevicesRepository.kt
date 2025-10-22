package com.example.amulet.shared.domain.devices.repository

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.model.Device
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
    suspend fun getDevice(deviceId: String): AppResult<Device>
    
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
     * Отвязать устройство от аккаунта.
     *
     * @param deviceId ID устройства
     */
    suspend fun unclaimDevice(deviceId: String): AppResult<Unit>
    
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

/**
 * Информация о найденном устройстве при сканировании для паринга.
 */
data class PairingDeviceFound(
    val serialNumber: String,
    val signalStrength: SignalStrength,
    val deviceName: String? = null
)

/**
 * Сила сигнала BLE (абстракция над RSSI).
 */
enum class SignalStrength {
    EXCELLENT, // > -60 dBm
    GOOD,      // -60..-70 dBm
    FAIR,      // -70..-80 dBm
    WEAK       // < -80 dBm
}

/**
 * Прогресс паринга нового устройства.
 */
sealed interface PairingProgress {
    /** Поиск устройства по BLE */
    data object SearchingDevice : PairingProgress
    
    /** Устройство найдено, начинаем подключение */
    data class DeviceFound(val signalStrength: SignalStrength) : PairingProgress
    
    /** Подключение по BLE */
    data object ConnectingBle : PairingProgress
    
    /** Привязка устройства на сервере (API claim) */
    data object ClaimingOnServer : PairingProgress
    
    /** Настройка устройства (чтение характеристик, установка начальных параметров) */
    data object ConfiguringDevice : PairingProgress
    
    /** Паринг завершен успешно */
    data class Completed(val device: Device) : PairingProgress
    
    /** Ошибка паринга */
    data class Failed(val error: AppError) : PairingProgress
}

/**
 * Прогресс подключения к уже привязанному устройству.
 */
sealed interface DeviceConnectionProgress {
    /** Поиск устройства по BLE */
    data object Scanning : DeviceConnectionProgress
    
    /** Устройство найдено */
    data class Found(val signalStrength: SignalStrength) : DeviceConnectionProgress
    
    /** Подключение */
    data object Connecting : DeviceConnectionProgress
    
    /** Подключено и готово к работе */
    data object Connected : DeviceConnectionProgress
    
    /** Ошибка подключения */
    data class Failed(val error: AppError) : DeviceConnectionProgress
}

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
 * Живой статус подключенного устройства (через BLE).
 */
data class DeviceLiveStatus(
    val serialNumber: String,
    val firmwareVersion: String,
    val hardwareVersion: Int,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val isOnline: Boolean
)
