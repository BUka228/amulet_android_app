package com.example.amulet.data.devices.datasource.local

import com.example.amulet.core.database.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Локальный источник данных для устройств.
 * Инкапсулирует работу с DeviceDao и Room базой данных.
 */
interface DevicesLocalDataSource {
    
    /**
     * Наблюдать за устройствами владельца.
     */
    fun observeDevicesByOwner(ownerId: String): Flow<List<DeviceEntity>>
    
    /**
     * Получить устройство по ID.
     */
    suspend fun getDeviceById(deviceId: String): DeviceEntity?
    
    /**
     * Получить устройство по BLE адресу для конкретного владельца.
     */
    suspend fun getDeviceByBleAddress(bleAddress: String, ownerId: String): DeviceEntity?
    
    /**
     * Сохранить или обновить устройство.
     */
    suspend fun upsertDevice(device: DeviceEntity)
    
    /**
     * Удалить устройство по ID.
     */
    suspend fun deleteDeviceById(deviceId: String)
    
    /**
     * Удалить все устройства.
     */
    suspend fun clearAll()
}
