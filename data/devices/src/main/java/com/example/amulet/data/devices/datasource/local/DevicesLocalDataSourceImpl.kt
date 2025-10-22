package com.example.amulet.data.devices.datasource.local

import com.example.amulet.core.database.dao.DeviceDao
import com.example.amulet.core.database.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Реализация локального источника данных для устройств.
 */
class DevicesLocalDataSourceImpl @Inject constructor(
    private val deviceDao: DeviceDao
) : DevicesLocalDataSource {
    
    override fun observeDevicesByOwner(ownerId: String): Flow<List<DeviceEntity>> {
        return deviceDao.observeByOwner(ownerId)
    }
    
    override suspend fun getDeviceById(deviceId: String): DeviceEntity? {
        return deviceDao.observeWithOwner(deviceId).firstOrNull()?.device
    }
    
    override suspend fun upsertDevice(device: DeviceEntity) {
        deviceDao.upsert(device)
    }
    
    override suspend fun upsertDevices(devices: List<DeviceEntity>) {
        deviceDao.upsert(devices)
    }
    
    override suspend fun deleteDeviceById(deviceId: String) {
        deviceDao.deleteById(deviceId)
    }
    
    override suspend fun clearAll() {
        deviceDao.clear()
    }
}
