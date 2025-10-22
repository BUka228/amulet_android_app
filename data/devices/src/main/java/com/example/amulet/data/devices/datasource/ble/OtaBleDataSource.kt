package com.example.amulet.data.devices.datasource.ble

import com.example.amulet.core.ble.model.FirmwareInfo
import com.example.amulet.core.ble.model.OtaProgress
import com.example.amulet.shared.core.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * Источник данных для OTA обновлений через BLE.
 * Инкапсулирует работу с AmuletBleManager для OTA операций.
 */
interface OtaBleDataSource {
    
    /**
     * Запустить OTA обновление прошивки через BLE.
     * Использует команды START_OTA -> OTA_CHUNK -> OTA_COMMIT с Flow Control.
     *
     * @param firmwareInfo Информация о прошивке
     * @return Flow с прогрессом обновления
     */
    fun startBleOtaUpdate(firmwareInfo: FirmwareInfo): Flow<OtaProgress>
    
    /**
     * Запустить OTA обновление через Wi-Fi.
     * Требует предварительной настройки Wi-Fi через SetWifiCred команду.
     *
     * @param ssid SSID Wi-Fi сети
     * @param password Пароль Wi-Fi сети
     * @param firmwareInfo Информация о прошивке
     * @return Flow с прогрессом обновления
     */
    fun startWifiOtaUpdate(
        ssid: String,
        password: String,
        firmwareInfo: FirmwareInfo
    ): Flow<OtaProgress>
}
