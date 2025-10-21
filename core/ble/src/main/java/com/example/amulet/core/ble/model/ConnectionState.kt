package com.example.amulet.core.ble.model

/**
 * Состояние BLE подключения к устройству.
 */
sealed interface ConnectionState {
    /** Устройство отключено */
    data object Disconnected : ConnectionState
    
    /** Идет процесс подключения */
    data object Connecting : ConnectionState
    
    /** Устройство подключено */
    data object Connected : ConnectionState
    
    /** GATT сервисы обнаружены, готов к работе */
    data object ServicesDiscovered : ConnectionState
    
    /** Попытка переподключения */
    data class Reconnecting(val attempt: Int) : ConnectionState
    
    /** Подключение не удалось */
    data class Failed(val cause: Throwable?) : ConnectionState
}
