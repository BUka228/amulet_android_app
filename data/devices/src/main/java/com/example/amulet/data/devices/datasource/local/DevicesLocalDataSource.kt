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
     *
     * @param ownerId ID владельца
     * @return Реактивный поток устройств
     */
    fun observeDevicesByOwner(ownerId: String): Flow<List<DeviceEntity>>
    
    /**
     * Получить устройство по ID.
     *
     * @param deviceId ID устройства
     */
    suspend fun getDeviceById(deviceId: String): DeviceEntity?
    
    /**
     * Сохранить или обновить устройство.
     *
     * @param device Устройство для сохранения
     */
    suspend fun upsertDevice(device: DeviceEntity)
    
    /**
     * Сохранить или обновить несколько устройств.
     *
     * @param devices Список устройств
     */
    suspend fun upsertDevices(devices: List<DeviceEntity>)
    
    /**
     * Удалить устройство по ID.
     *
     * @param deviceId ID устройства
     */
    suspend fun deleteDeviceById(deviceId: String)
    
    /**
     * Удалить все устройства.
     */
    suspend fun clearAll()
}
