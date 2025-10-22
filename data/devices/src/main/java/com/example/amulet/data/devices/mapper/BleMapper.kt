package com.example.amulet.data.devices.mapper

import com.example.amulet.core.ble.model.ConnectionState
import com.example.amulet.core.ble.model.DeviceStatus as BleDeviceStatus
import com.example.amulet.shared.domain.devices.model.ConnectionStatus
import com.example.amulet.shared.domain.devices.model.DeviceLiveStatus
import com.example.amulet.shared.domain.devices.model.SignalStrength
import javax.inject.Inject

/**
 * Маппер для преобразования BLE моделей в доменные.
 */
class BleMapper @Inject constructor() {
    
    /**
     * Маппинг состояния BLE подключения.
     */
    fun mapConnectionState(state: ConnectionState): ConnectionStatus {
        return when (state) {
            ConnectionState.Disconnected -> ConnectionStatus.DISCONNECTED
            ConnectionState.Connecting -> ConnectionStatus.CONNECTING
            ConnectionState.Connected, ConnectionState.ServicesDiscovered -> ConnectionStatus.CONNECTED
            is ConnectionState.Reconnecting -> ConnectionStatus.RECONNECTING
            is ConnectionState.Failed -> ConnectionStatus.FAILED
        }
    }
    
    /**
     * Маппинг статуса BLE устройства.
     */
    fun mapDeviceStatus(status: BleDeviceStatus): DeviceLiveStatus {
        return DeviceLiveStatus(
            serialNumber = status.serialNumber,
            firmwareVersion = status.firmwareVersion,
            hardwareVersion = status.hardwareVersion,
            batteryLevel = status.batteryLevel,
            isCharging = status.isCharging,
            isOnline = status.isOnline
        )
    }
    
    /**
     * Маппинг RSSI в доменную силу сигнала.
     */
    fun mapRssiToSignalStrength(rssi: Int): SignalStrength {
        return when {
            rssi > -60 -> SignalStrength.EXCELLENT
            rssi > -70 -> SignalStrength.GOOD
            rssi > -80 -> SignalStrength.FAIR
            else -> SignalStrength.WEAK
        }
    }
}
