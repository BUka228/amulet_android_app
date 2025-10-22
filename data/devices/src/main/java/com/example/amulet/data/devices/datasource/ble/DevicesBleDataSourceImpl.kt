package com.example.amulet.data.devices.datasource.ble

import com.example.amulet.core.ble.AmuletBleManager
import com.example.amulet.core.ble.model.AmuletCommand
import com.example.amulet.core.ble.model.BleResult
import com.example.amulet.core.ble.model.ConnectionState
import com.example.amulet.core.ble.model.DeviceStatus
import com.example.amulet.core.ble.scanner.BleScanner
import com.example.amulet.core.ble.scanner.ScannedDevice
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Реализация источника данных для работы с устройствами через BLE.
 */
class DevicesBleDataSourceImpl @Inject constructor(
    private val bleManager: AmuletBleManager,
    private val bleScanner: BleScanner
) : DevicesBleDataSource {
    
    override fun scanForDevices(
        timeoutMs: Long,
        serialNumberFilter: String?
    ): Flow<ScannedDevice> {
        return bleScanner.scanForAmulets(timeoutMs, serialNumberFilter)
    }
    
    override suspend fun connect(
        deviceAddress: String,
        autoReconnect: Boolean
    ): AppResult<Unit> {
        return try {
            bleManager.connect(deviceAddress, autoReconnect)
            Ok(Unit)
        } catch (e: Exception) {
            Err(mapBleException(e))
        }
    }
    
    override suspend fun disconnect(): AppResult<Unit> {
        return try {
            bleManager.disconnect()
            Ok(Unit)
        } catch (e: Exception) {
            Err(mapBleException(e))
        }
    }
    
    override fun observeConnectionState(): Flow<ConnectionState> {
        return bleManager.connectionState
    }
    
    override fun observeBatteryLevel(): Flow<Int> {
        return bleManager.batteryLevel
    }
    
    override fun observeDeviceStatus(): Flow<DeviceStatus?> {
        return bleManager.deviceStatus
    }
    
    override suspend fun getProtocolVersion(): AppResult<String?> {
        return try {
            val version = bleManager.getProtocolVersion()
            Ok(version)
        } catch (e: Exception) {
            Err(mapBleException(e))
        }
    }
    
    /**
     * Маппинг BLE исключений в типизированные ошибки AppError.
     */
    private fun mapBleException(e: Exception): AppError {
        return when {
            e.message?.contains("not found", ignoreCase = true) == true ->
                AppError.BleError.DeviceNotFound
            
            e.message?.contains("connection", ignoreCase = true) == true ->
                AppError.BleError.ConnectionFailed
            
            e.message?.contains("disconnected", ignoreCase = true) == true ->
                AppError.BleError.DeviceDisconnected
            
            e.message?.contains("timeout", ignoreCase = true) == true ->
                AppError.BleError.CommandTimeout(e.message ?: "Unknown command")
            
            else -> AppError.Unknown
        }
    }
}
