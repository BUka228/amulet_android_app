package com.example.amulet.shared.domain.devices.repository

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.model.FirmwareUpdate
import com.example.amulet.shared.domain.devices.model.OtaUpdateProgress
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для управления OTA обновлениями прошивки.
 * Инкапсулирует работу с API, кэшированием и BLE обновлениями.
 */
interface OtaRepository {
    
    /**
     * Проверить доступность обновления прошивки для устройства.
     *
     * @param deviceId ID устройства
     * @return Информация об обновлении (null если обновление не требуется)
     */
    suspend fun checkFirmwareUpdate(deviceId: DeviceId): AppResult<FirmwareUpdate?>
    
    /**
     * Запустить OTA обновление через BLE.
     *
     * @param deviceId ID устройства
     * @param firmwareUpdate Информация о прошивке для установки
     * @return Flow с прогрессом обновления
     */
    fun startBleOtaUpdate(
        deviceId: DeviceId,
        firmwareUpdate: FirmwareUpdate
    ): Flow<OtaUpdateProgress>
    
    /**
     * Запустить OTA обновление через Wi-Fi.
     * Устройство должно быть подключено через BLE для настройки Wi-Fi.
     *
     * @param deviceId ID устройства
     * @param ssid SSID Wi-Fi сети
     * @param password Пароль Wi-Fi сети
     * @param firmwareUpdate Информация о прошивке для установки
     * @return Flow с прогрессом обновления
     */
    fun startWifiOtaUpdate(
        deviceId: DeviceId,
        ssid: String,
        password: String,
        firmwareUpdate: FirmwareUpdate
    ): Flow<OtaUpdateProgress>
    
    /**
     * Отправить отчет об успешной/неудачной установке прошивки.
     *
     * @param deviceId ID устройства
     * @param fromVersion Исходная версия
     * @param toVersion Целевая версия
     * @param success Успешность установки
     * @param errorMessage Сообщение об ошибке (если success = false)
     */
    suspend fun reportFirmwareInstall(
        deviceId: DeviceId,
        fromVersion: String,
        toVersion: String,
        success: Boolean,
        errorMessage: String? = null
    ): AppResult<Unit>
    
    /**
     * Отменить текущее OTA обновление.
     */
    suspend fun cancelOtaUpdate(): AppResult<Unit>
}
