package com.example.amulet.shared.domain.devices.model

import com.example.amulet.shared.core.AppError

/**
 * Информация о найденном устройстве при сканировании BLE.
 */
data class PairingDeviceFound(
    val bleAddress: String,
    val signalStrength: SignalStrength,
    val deviceName: String? = null,
    val hardwareVersion: Int? = null
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
