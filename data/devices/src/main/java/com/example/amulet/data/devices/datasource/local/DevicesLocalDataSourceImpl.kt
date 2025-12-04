package com.example.amulet.data.devices.datasource.local

import com.example.amulet.core.database.dao.DeviceDao
import com.example.amulet.core.database.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow
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
        return deviceDao.getById(deviceId)
    }
    
    override suspend fun getDeviceByBleAddress(bleAddress: String, ownerId: String): DeviceEntity? {
        return deviceDao.getByBleAddress(bleAddress, ownerId)
    }
    
    override suspend fun getLastConnectedDeviceByOwner(ownerId: String): DeviceEntity? {
        return deviceDao.getLastConnectedForOwner(ownerId)
    }
    
    override suspend fun upsertDevice(device: DeviceEntity) {
        deviceDao.upsert(device)
    }
    
    override suspend fun deleteDeviceById(deviceId: String) {
        deviceDao.deleteById(deviceId)
    }
    
    override suspend fun clearAll() {
        deviceDao.clear()
    }
}
