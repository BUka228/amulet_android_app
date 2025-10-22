package com.example.amulet.shared.domain.devices.model

import com.example.amulet.shared.core.AppError

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
