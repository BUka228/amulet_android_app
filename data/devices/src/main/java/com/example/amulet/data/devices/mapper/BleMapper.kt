package com.example.amulet.data.devices.mapper

import com.example.amulet.core.ble.model.ConnectionState
import com.example.amulet.core.ble.model.DeviceStatus as BleDeviceStatus
import com.example.amulet.core.ble.scanner.ScannedDevice
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.devices.model.BleConnectionState
import com.example.amulet.shared.domain.devices.model.DeviceLiveStatus
import com.example.amulet.shared.domain.devices.model.ScannedAmulet
import com.example.amulet.shared.domain.devices.model.SignalStrength
import javax.inject.Inject

/**
 * Маппер для преобразования BLE моделей в доменные.
 */
class BleMapper @Inject constructor() {
    
    /**
     * Маппинг состояния BLE подключения из core:ble в domain.
     */
    fun mapConnectionState(state: ConnectionState): BleConnectionState {
        return when (state) {
            ConnectionState.Disconnected -> BleConnectionState.Disconnected
            ConnectionState.Connecting -> BleConnectionState.Connecting
            ConnectionState.Connected, ConnectionState.ServicesDiscovered -> BleConnectionState.Connected
            is ConnectionState.Reconnecting -> BleConnectionState.Reconnecting(state.attempt)
            is ConnectionState.Failed -> BleConnectionState.Failed(
                state.cause?.let { AppError.BleError.ConnectionFailed } ?: AppError.BleError.ConnectionFailed
            )
        }
    }
    
    /**
     * Маппинг статуса BLE устройства из core:ble в domain.
     */
    fun mapDeviceStatus(status: BleDeviceStatus): DeviceLiveStatus {
        return DeviceLiveStatus(
            firmwareVersion = status.firmwareVersion,
            hardwareVersion = status.hardwareVersion,
            batteryLevel = status.batteryLevel,
            isCharging = status.isCharging,
            isOnline = status.isOnline
        )
    }
    
    /**
     * Маппинг найденного устройства из core:ble в domain.
     */
    fun mapScannedDevice(device: ScannedDevice): ScannedAmulet {
        return ScannedAmulet(
            bleAddress = device.address,
            signalStrength = mapRssiToSignalStrength(device.rssi),
            deviceName = device.name
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
