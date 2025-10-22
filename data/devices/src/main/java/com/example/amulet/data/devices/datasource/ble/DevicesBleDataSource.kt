package com.example.amulet.data.devices.datasource.ble

import com.example.amulet.core.ble.model.ConnectionState
import com.example.amulet.core.ble.model.DeviceStatus
import com.example.amulet.core.ble.scanner.ScannedDevice
import com.example.amulet.shared.core.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * Источник данных для работы с устройствами через BLE.
 * Инкапсулирует работу с AmuletBleManager и BleScanner.
 */
interface DevicesBleDataSource {
    
    /**
     * Сканировать BLE устройства амулета.
     *
     * @param timeoutMs Таймаут сканирования (0 = бесконечно)
     * @param serialNumberFilter Фильтр по серийному номеру (для паринга конкретного устройства)
     * @return Flow с найденными устройствами
     */
    fun scanForDevices(
        timeoutMs: Long = 10_000L,
        serialNumberFilter: String? = null
    ): Flow<ScannedDevice>
    
    /**
     * Подключиться к устройству по MAC адресу.
     *
     * @param deviceAddress MAC адрес устройства
     * @param autoReconnect Автоматически переподключаться при потере связи
     */
    suspend fun connect(
        deviceAddress: String,
        autoReconnect: Boolean = true
    ): AppResult<Unit>
    
    /**
     * Отключиться от устройства.
     */
    suspend fun disconnect(): AppResult<Unit>
    
    /**
     * Наблюдать за состоянием подключения.
     */
    fun observeConnectionState(): Flow<ConnectionState>
    
    /**
     * Наблюдать за уровнем батареи устройства.
     */
    fun observeBatteryLevel(): Flow<Int>
    
    /**
     * Наблюдать за статусом устройства (серийный номер, версия прошивки и т.д.).
     */
    fun observeDeviceStatus(): Flow<DeviceStatus?>
    
    /**
     * Получить версию протокола, поддерживаемую устройством.
     *
     * @return Версия протокола (например, "v1.0" или "v2.0")
     */
    suspend fun getProtocolVersion(): AppResult<String?>
}
