package com.example.amulet.core.ble

import com.example.amulet.core.ble.model.AnimationPlan
import com.example.amulet.core.ble.model.AmuletCommand
import com.example.amulet.core.ble.model.BleResult
import com.example.amulet.core.ble.model.ConnectionState
import com.example.amulet.core.ble.model.DeviceReadyState
import com.example.amulet.core.ble.model.DeviceStatus
import com.example.amulet.core.ble.model.FirmwareInfo
import com.example.amulet.core.ble.model.OtaProgress
import com.example.amulet.core.ble.model.UploadProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Менеджер для управления BLE подключением к амулету.
 * 
 * Реализует протокол из docs/20_DATA_LAYER/03_BLE_PROTOCOL.md:
 * - GATT профиль с Nordic UART Service и Amulet Device Service
 * - Flow Control для надежной передачи данных
 * - OTA обновления через BLE и Wi-Fi
 * - Управление анимациями и командами
 */
interface AmuletBleManager {
    
    /**
     * Текущее состояние подключения.
     */
    val connectionState: StateFlow<ConnectionState>
    
    /**
     * Уровень батареи устройства (0-100).
     */
    val batteryLevel: Flow<Int>
    
    /**
     * Статус устройства (серийный номер, версия прошивки и т.д.).
     */
    val deviceStatus: Flow<DeviceStatus?>
    
    /**
     * Состояние готовности устройства для Flow Control.
     */
    val deviceReadyState: StateFlow<DeviceReadyState>
    
    /**
     * Подключиться к устройству по MAC адресу или Device ID.
     * 
     * @param deviceAddress MAC адрес устройства (например, "00:11:22:AA:BB:CC")
     * @param autoReconnect Автоматически переподключаться при потере связи
     */
    suspend fun connect(deviceAddress: String, autoReconnect: Boolean = true)
    
    /**
     * Отключиться от устройства.
     */
    suspend fun disconnect()
    
    /**
     * Отправить команду на устройство.
     * 
     * @param command Команда для выполнения
     * @return Результат выполнения
     */
    suspend fun sendCommand(command: AmuletCommand): BleResult
    
    /**
     * Загрузить план анимации на устройство.
     * Использует механизм BEGIN_PLAN -> ADD_COMMAND -> COMMIT_PLAN с Flow Control.
     * 
     * @param plan План анимации с набором команд
     * @return Flow с прогрессом загрузки
     */
    fun uploadAnimation(plan: AnimationPlan): Flow<UploadProgress>
    
    /**
     * Запустить OTA обновление прошивки через BLE.
     * Использует команды START_OTA -> OTA_CHUNK -> OTA_COMMIT с Flow Control.
     * 
     * @param firmwareInfo Информация о прошивке
     * @return Flow с прогрессом обновления
     */
    fun startOtaUpdate(firmwareInfo: FirmwareInfo): Flow<OtaProgress>
    
    /**
     * Запустить OTA обновление через Wi-Fi.
     * Требует предварительной настройки Wi-Fi через SetWifiCred команду.
     * 
     * @param firmwareInfo Информация о прошивке
     * @return Flow с прогрессом обновления
     */
    fun startWifiOtaUpdate(firmwareInfo: FirmwareInfo): Flow<OtaProgress>
    
    /**
     * Получить версию протокола, поддерживаемую устройством.
     * 
     * @return Версия протокола (например, "v1.0" или "v2.0")
     */
    suspend fun getProtocolVersion(): String?
    
    /**
     * Подписаться на уведомления от устройства.
     * 
     * @return Flow с уведомлениями (NOTIFY:TYPE:DATA)
     */
    fun observeNotifications(): Flow<String>
}
