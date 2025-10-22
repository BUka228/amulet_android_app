package com.example.amulet.data.devices.datasource.local

import com.example.amulet.core.database.entity.FirmwareInfoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Локальный источник данных для OTA (кэширование информации о прошивках).
 * Инкапсулирует работу с FirmwareInfoDao и Room базой данных.
 */
interface OtaLocalDataSource {
    
    /**
     * Наблюдать за информацией о прошивках для конкретной версии оборудования.
     *
     * @param hardwareVersion Версия оборудования (100 или 200)
     * @return Реактивный поток прошивок
     */
    fun observeFirmwareByHardware(hardwareVersion: Int): Flow<List<FirmwareInfoEntity>>
    
    /**
     * Получить информацию о прошивке по ID.
     *
     * @param id ID прошивки
     */
    suspend fun getFirmwareById(id: String): FirmwareInfoEntity?
    
    /**
     * Сохранить или обновить информацию о прошивке.
     *
     * @param firmware Информация о прошивке
     */
    suspend fun upsertFirmware(firmware: FirmwareInfoEntity)
    
    /**
     * Очистить устаревшую информацию о прошивках.
     *
     * @param cutoffTimestamp Удалить записи старше этого времени
     * @return Количество удаленных записей
     */
    suspend fun cleanupOldFirmware(cutoffTimestamp: Long): Int
    
    /**
     * Удалить все кэшированные данные о прошивках.
     */
    suspend fun clearAll()
}
