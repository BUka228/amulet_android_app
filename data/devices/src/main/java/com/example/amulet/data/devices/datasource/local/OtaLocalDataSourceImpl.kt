package com.example.amulet.data.devices.datasource.local

import com.example.amulet.core.database.dao.FirmwareInfoDao
import com.example.amulet.core.database.entity.FirmwareInfoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Реализация локального источника данных для OTA (кэширование прошивок).
 */
class OtaLocalDataSourceImpl @Inject constructor(
    private val firmwareInfoDao: FirmwareInfoDao
) : OtaLocalDataSource {
    
    override fun observeFirmwareByHardware(hardwareVersion: Int): Flow<List<FirmwareInfoEntity>> {
        return firmwareInfoDao.observeByHardwareVersion(hardwareVersion)
    }
    
    override suspend fun getFirmwareById(id: String): FirmwareInfoEntity? {
        return firmwareInfoDao.observeById(id).firstOrNull()
    }
    
    override suspend fun upsertFirmware(firmware: FirmwareInfoEntity) {
        firmwareInfoDao.upsert(firmware)
    }
    
    override suspend fun cleanupOldFirmware(cutoffTimestamp: Long): Int {
        return firmwareInfoDao.cleanup(cutoffTimestamp)
    }
    
    override suspend fun clearAll() {
        firmwareInfoDao.clear()
    }
}
