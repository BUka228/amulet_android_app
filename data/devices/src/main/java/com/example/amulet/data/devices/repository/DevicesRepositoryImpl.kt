package com.example.amulet.data.devices.repository

import com.example.amulet.data.devices.datasource.ble.DevicesBleDataSource
import com.example.amulet.data.devices.datasource.local.DevicesLocalDataSource
import com.example.amulet.data.devices.datasource.remote.DevicesRemoteDataSource
import com.example.amulet.data.devices.mapper.BleMapper
import com.example.amulet.data.devices.mapper.DeviceMapper
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.auth.UserSessionContext
import com.example.amulet.shared.core.auth.UserSessionProvider
import com.example.amulet.shared.domain.devices.model.ConnectionStatus
import com.example.amulet.shared.domain.devices.model.Device
import com.example.amulet.shared.domain.devices.model.DeviceConnectionProgress
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.model.DeviceLiveStatus
import com.example.amulet.shared.domain.devices.model.PairingDeviceFound
import com.example.amulet.shared.domain.devices.model.PairingProgress
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapBoth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация репозитория для управления устройствами.
 */
@Singleton
class DevicesRepositoryImpl @Inject constructor(
    private val remoteDataSource: DevicesRemoteDataSource,
    private val localDataSource: DevicesLocalDataSource,
    private val bleDataSource: DevicesBleDataSource,
    private val deviceMapper: DeviceMapper,
    private val bleMapper: BleMapper,
    private val userSessionProvider: UserSessionProvider
) : DevicesRepository {
    
    override fun observeDevices(): Flow<List<Device>> {
        val userId = getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        
        return localDataSource.observeDevicesByOwner(userId)
            .map { entities -> entities.map { deviceMapper.toDomain(it) } }
    }
    
    override suspend fun getDevice(deviceId: DeviceId): AppResult<Device> {
        // Сначала пытаемся получить из локальной БД
        val localDevice = localDataSource.getDeviceById(deviceId.value)
        if (localDevice != null) {
            return Ok(deviceMapper.toDomain(localDevice))
        }
        
        // Если нет в БД - загружаем с сервера
        return remoteDataSource.fetchDevice(deviceId.value).andThen { dto ->
            val entity = deviceMapper.toEntity(dto)
            localDataSource.upsertDevice(entity)
            Ok(deviceMapper.toDomain(dto))
        }
    }
    
    override suspend fun claimDevice(
        serial: String,
        claimToken: String,
        name: String?
    ): AppResult<Device> {
        return remoteDataSource.claimDevice(serial, claimToken, name).andThen { dto ->
            // Сохраняем в локальную БД
            val entity = deviceMapper.toEntity(dto)
            localDataSource.upsertDevice(entity)
            Ok(deviceMapper.toDomain(dto))
        }
    }
    
    override suspend fun unclaimDevice(deviceId: DeviceId): AppResult<Unit> {
        return remoteDataSource.unclaimDevice(deviceId.value).andThen {
            // Удаляем из локальной БД
            localDataSource.deleteDeviceById(deviceId.value)
            Ok(Unit)
        }
    }
    
    override suspend fun updateDeviceSettings(
        deviceId: DeviceId,
        name: String?,
        brightness: Double?,
        haptics: Double?,
        gestures: Map<String, String>?
    ): AppResult<Device> {
        return remoteDataSource.updateDevice(
            deviceId = deviceId.value,
            name = name,
            brightness = brightness,
            haptics = haptics,
            gestures = gestures
        ).andThen { dto ->
            // Обновляем в локальной БД
            val entity = deviceMapper.toEntity(dto)
            localDataSource.upsertDevice(entity)
            Ok(deviceMapper.toDomain(dto))
        }
    }
    
    override suspend fun syncDevices(): AppResult<Unit> {
        return remoteDataSource.fetchDevices().andThen { dtoList ->
            // Сохраняем в локальную БД
            val entities = dtoList.map { deviceMapper.toEntity(it) }
            localDataSource.upsertDevices(entities)
            Ok(Unit)
        }
    }
    
    // ========== Паринг и подключение ==========
    
    override fun scanForPairing(
        serialNumberFilter: String?,
        timeoutMs: Long
    ): Flow<PairingDeviceFound> {
        return bleDataSource.scanForDevices(timeoutMs, serialNumberFilter).map { scanned ->
            PairingDeviceFound(
                serialNumber = scanned.serialNumber ?: "Unknown",
                signalStrength = bleMapper.mapRssiToSignalStrength(scanned.rssi),
                deviceName = scanned.name
            )
        }
    }
    
    override fun pairAndClaimDevice(
        serialNumber: String,
        claimToken: String,
        deviceName: String?
    ): Flow<PairingProgress> = kotlinx.coroutines.flow.flow {
        try {
            emit(PairingProgress.SearchingDevice)
            
            val foundDevice = findDeviceBySerial(serialNumber, 30_000L)
            if (foundDevice == null) {
                emit(PairingProgress.Failed(AppError.BleError.DeviceNotFound))
                return@flow
            }
            
            emit(PairingProgress.DeviceFound(foundDevice.signalStrength))
            emit(PairingProgress.ConnectingBle)
            
            bleDataSource.connect(foundDevice.address, autoReconnect = false).mapBoth(
                success = {
                    emit(PairingProgress.ClaimingOnServer)
                    
                    remoteDataSource.claimDevice(serialNumber, claimToken, deviceName).mapBoth(
                        success = { dto ->
                            emit(PairingProgress.ConfiguringDevice)
                            
                            val entity = deviceMapper.toEntity(dto)
                            localDataSource.upsertDevice(entity)
                            
                            val device = deviceMapper.toDomain(dto)
                            emit(PairingProgress.Completed(device))
                        },
                        failure = { error ->
                            bleDataSource.disconnect()
                            emit(PairingProgress.Failed(error))
                        }
                    )
                },
                failure = { error ->
                    emit(PairingProgress.Failed(error))
                }
            )
        } catch (e: Exception) {
            emit(PairingProgress.Failed(AppError.Unknown(e.message ?: "Pairing failed")))
        }
    }
    
    override fun connectToDevice(
        serialNumber: String,
        timeoutMs: Long
    ): Flow<DeviceConnectionProgress> = kotlinx.coroutines.flow.flow {
        try {
            emit(DeviceConnectionProgress.Scanning)
            
            val foundDevice = findDeviceBySerial(serialNumber, timeoutMs)
            if (foundDevice == null) {
                emit(DeviceConnectionProgress.Failed(AppError.BleError.DeviceNotFound))
                return@flow
            }
            
            emit(DeviceConnectionProgress.Found(foundDevice.signalStrength))
            emit(DeviceConnectionProgress.Connecting)
            
            bleDataSource.connect(foundDevice.address, autoReconnect = true).mapBoth(
                success = {
                    emit(DeviceConnectionProgress.Connected)
                },
                failure = { error ->
                    emit(DeviceConnectionProgress.Failed(error))
                }
            )
        } catch (e: Exception) {
            emit(DeviceConnectionProgress.Failed(AppError.Unknown(e.message ?: "Connection failed")))
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
    
    /**
     * Получить ID текущего пользователя из сессии.
     * Поддерживает как авторизованных пользователей, так и гостей.
     */
    private fun getCurrentUserId(): String? {
        return when (val context = userSessionProvider.currentContext) {
            is UserSessionContext.LoggedIn -> context.userId.value
            is UserSessionContext.Guest -> context.sessionId // Гость использует sessionId как userId
            else -> null
        }
    }
    
    
    /**
     * Поиск устройства по серийному номеру через BLE сканирование.
     * Возвращает информацию о найденном устройстве или null.
     */
    private suspend fun findDeviceBySerial(
        serialNumber: String,
        timeoutMs: Long
    ): FoundDeviceInfo? {
        var foundDevice: FoundDeviceInfo? = null
        
        try {
            bleDataSource.scanForDevices(timeoutMs, serialNumber)
                .collect { scanned ->
                    if (scanned.serialNumber == serialNumber) {
                        foundDevice = FoundDeviceInfo(
                            address = scanned.address,
                            signalStrength = bleMapper.mapRssiToSignalStrength(scanned.rssi)
                        )
                        throw DeviceFoundException()
                    }
                }
        } catch (e: DeviceFoundException) {
            // Устройство найдено - это нормальный флоу
        }
        
        return foundDevice
    }
    
    /**
     * Внутренняя информация о найденном устройстве.
     */
    private data class FoundDeviceInfo(
        val address: String,
        val signalStrength: com.example.amulet.shared.domain.devices.model.SignalStrength
    )
    
    /**
     * Исключение для прерывания Flow сканирования когда устройство найдено.
     */
    private class DeviceFoundException : Exception()
}
