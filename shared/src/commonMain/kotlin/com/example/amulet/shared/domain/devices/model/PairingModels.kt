package com.example.amulet.shared.domain.devices.model

/**
 * Информация о найденном устройстве при сканировании BLE.
 */
data class ScannedAmulet(
    val bleAddress: String,
    val signalStrength: SignalStrength,
    val deviceName: String
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
