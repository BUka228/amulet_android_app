package com.example.amulet.data.devices.repository

import com.example.amulet.data.devices.datasource.ble.DevicesBleDataSource
import com.example.amulet.data.devices.datasource.local.DevicesLocalDataSource
import com.example.amulet.data.devices.mapper.BleMapper
import com.example.amulet.data.devices.mapper.toDevice
import com.example.amulet.data.devices.mapper.toDeviceEntity
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.auth.UserSessionProvider
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.example.amulet.shared.domain.devices.model.ConnectionStatus
import com.example.amulet.shared.domain.devices.model.Device
import com.example.amulet.shared.domain.devices.model.DeviceConnectionProgress
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.model.DeviceLiveStatus
import com.example.amulet.shared.domain.devices.model.DeviceSettings
import com.example.amulet.shared.domain.devices.model.PairingDeviceFound
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация репозитория устройств.
 * Работает только локально: БД + BLE.
 * Устройства привязаны к текущему пользователю.
 */
@Singleton
class DevicesRepositoryImpl @Inject constructor(
    private val localDataSource: DevicesLocalDataSource,
    private val bleDataSource: DevicesBleDataSource,
    private val sessionProvider: UserSessionProvider,
    private val bleMapper: BleMapper
) : DevicesRepository {
    
    private val currentUserId: String
        get() {
            val context = sessionProvider.currentContext
            return when (context) {
                is com.example.amulet.shared.core.auth.UserSessionContext.LoggedIn -> context.userId.value
                is com.example.amulet.shared.core.auth.UserSessionContext.Guest -> context.sessionId
                else -> throw IllegalStateException("User not authenticated")
            }
        }
    
    override fun observeDevices(): Flow<List<Device>> {
        return localDataSource.observeDevicesByOwner(currentUserId)
            .map { entities -> entities.map { it.toDevice() } }
    }
    
    override suspend fun getDevice(deviceId: DeviceId): AppResult<Device> {
        return try {
            val entity = localDataSource.getDeviceById(deviceId.value)
                ?: return Err(AppError.NotFound)
            Ok(entity.toDevice())
        } catch (e: Exception) {
            Err(AppError.DatabaseError)
        }
    }
    
    override suspend fun addDevice(
        bleAddress: String,
        name: String,
        hardwareVersion: Int
    ): AppResult<Device> {
        return try {
            // Проверяем, не добавлено ли уже для текущего пользователя
            val existing = localDataSource.getDeviceByBleAddress(bleAddress, currentUserId)
            if (existing != null) {
                return Err(AppError.Validation(mapOf("bleAddress" to "Device already added")))
            }
            
            val device = Device(
                id = DeviceId(UUID.randomUUID().toString()),
                ownerId = currentUserId,
                bleAddress = bleAddress,
                hardwareVersion = hardwareVersion,
                firmwareVersion = "unknown",
                name = name,
                batteryLevel = null,
                status = com.example.amulet.shared.domain.devices.model.DeviceStatus.OFFLINE,
                addedAt = System.currentTimeMillis(),
                lastConnectedAt = null,
                settings = DeviceSettings()
            )
            
            localDataSource.upsertDevice(device.toDeviceEntity())
            Ok(device)
        } catch (e: Exception) {
            Err(AppError.DatabaseError)
        }
    }
    
    override suspend fun removeDevice(deviceId: DeviceId): AppResult<Unit> {
        return try {
            localDataSource.deleteDeviceById(deviceId.value)
            Ok(Unit)
        } catch (e: Exception) {
            Err(AppError.DatabaseError)
        }
    }
    
    override suspend fun updateDeviceSettings(
        deviceId: DeviceId,
        name: String?,
        brightness: Double?,
        haptics: Double?,
        gestures: Map<String, String>?
    ): AppResult<Device> {
        return try {
            val entity = localDataSource.getDeviceById(deviceId.value)
                ?: return Err(AppError.NotFound)
            
            val device = entity.toDevice()
            val updatedSettings = device.settings.copy(
                brightness = brightness ?: device.settings.brightness,
                haptics = haptics ?: device.settings.haptics,
                gestures = gestures ?: device.settings.gestures
            )
            
            val updatedDevice = device.copy(
                name = name ?: device.name,
                settings = updatedSettings
            )
            
            localDataSource.upsertDevice(updatedDevice.toDeviceEntity())
            Ok(updatedDevice)
        } catch (e: Exception) {
            Err(AppError.DatabaseError)
        }
    }
    
    override fun scanForDevices(timeoutMs: Long): Flow<PairingDeviceFound> {
        return bleDataSource.scanForDevices(timeoutMs).map { scannedDevice ->
            PairingDeviceFound(
                bleAddress = scannedDevice.address,
                signalStrength = bleMapper.mapRssiToSignalStrength(scannedDevice.rssi),
                deviceName = scannedDevice.name,
                hardwareVersion = null // Получим при подключении
            )
        }
    }
    
    override fun connectToDevice(bleAddress: String): Flow<DeviceConnectionProgress> {
        // Используем observeConnectionState для отслеживания прогресса подключения
        return kotlinx.coroutines.flow.flow {
            // Запускаем подключение
            bleDataSource.connect(bleAddress)
                .onFailure { error ->
                    emit(DeviceConnectionProgress.Failed(error))
                    return@flow
                }
            
            // Наблюдаем за состоянием подключения
            bleDataSource.observeConnectionState().collect { state ->
                when (state) {
                    com.example.amulet.core.ble.model.ConnectionState.Connecting -> 
                        emit(DeviceConnectionProgress.Connecting)
                    
                    com.example.amulet.core.ble.model.ConnectionState.Connected,
                    com.example.amulet.core.ble.model.ConnectionState.ServicesDiscovered -> 
                        emit(DeviceConnectionProgress.Connected)
                    
                    is com.example.amulet.core.ble.model.ConnectionState.Failed -> 
                        emit(DeviceConnectionProgress.Failed(AppError.BleError.ConnectionFailed))
                    
                    else -> { /* Игнорируем другие состояния */ }
                }
            }
        }
    }
    
    override suspend fun disconnectFromDevice(): AppResult<Unit> {
        return bleDataSource.disconnect()
    }
    
    override fun observeConnectionState(): Flow<ConnectionStatus> {
        return bleDataSource.observeConnectionState().map { state ->
            bleMapper.mapConnectionState(state)
        }
    }
    
    override fun observeConnectedDeviceStatus(): Flow<DeviceLiveStatus?> {
        return bleDataSource.observeDeviceStatus().map { status ->
            status?.let { bleMapper.mapDeviceStatus(it) }
        }
    }
}
