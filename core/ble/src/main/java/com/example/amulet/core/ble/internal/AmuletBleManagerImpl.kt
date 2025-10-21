package com.example.amulet.core.ble.internal

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.example.amulet.core.ble.AmuletBleManager
import com.example.amulet.core.ble.model.AnimationPlan
import com.example.amulet.core.ble.model.AmuletCommand
import com.example.amulet.core.ble.model.BleResult
import com.example.amulet.core.ble.model.ConnectionState
import com.example.amulet.core.ble.model.DeviceReadyState
import com.example.amulet.core.ble.model.DeviceStatus
import com.example.amulet.core.ble.model.FirmwareInfo
import com.example.amulet.core.ble.model.OtaProgress
import com.example.amulet.core.ble.model.OtaState
import com.example.amulet.core.ble.model.UploadProgress
import com.example.amulet.core.ble.model.UploadState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Реализация BLE менеджера для управления амулетом.
 * 
 * Использует Android Bluetooth Low Energy API для:
 * - GATT подключения и управления сервисами
 * - Отправки команд через Nordic UART Service
 * - OTA обновлений через BLE и Wi-Fi
 * - Flow Control для надежной передачи данных
 */
@Singleton
class AmuletBleManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val flowControlManager: FlowControlManager,
    private val reconnectPolicy: ReconnectPolicy,
    private val retryPolicy: RetryPolicy
) : AmuletBleManager {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    
    private var bluetoothGatt: BluetoothGatt? = null
    private var currentDeviceAddress: String? = null
    private var autoReconnect: Boolean = false
    
    // State flows
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _batteryLevel = MutableSharedFlow<Int>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val batteryLevel: Flow<Int> = _batteryLevel.asSharedFlow()
    
    private val _deviceStatus = MutableStateFlow<DeviceStatus?>(null)
    override val deviceStatus: Flow<DeviceStatus?> = _deviceStatus.asStateFlow()
    
    override val deviceReadyState: StateFlow<DeviceReadyState> = flowControlManager.readyState
    
    private val _notifications = MutableSharedFlow<String>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    
    // Характеристики GATT
    private var txCharacteristic: BluetoothGattCharacteristic? = null
    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    private var batteryCharacteristic: BluetoothGattCharacteristic? = null
    private var deviceInfoCharacteristic: BluetoothGattCharacteristic? = null
    private var deviceStatusCharacteristic: BluetoothGattCharacteristic? = null
    
    @SuppressLint("MissingPermission")
    override suspend fun connect(deviceAddress: String, autoReconnect: Boolean) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            throw IllegalStateException("Bluetooth adapter is not available or disabled")
        }
        
        this.currentDeviceAddress = deviceAddress
        this.autoReconnect = autoReconnect
        
        _connectionState.value = ConnectionState.Connecting
        
        try {
            val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
            
            suspendCancellableCoroutine<Unit> { continuation ->
                bluetoothGatt = device.connectGatt(
                    context,
                    autoReconnect,
                    createGattCallback(continuation),
                    BluetoothDevice.TRANSPORT_LE
                )
                
                continuation.invokeOnCancellation {
                    bluetoothGatt?.disconnect()
                }
            }
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Failed(e)
            throw e
        }
    }
    
    @SuppressLint("MissingPermission")
    override suspend fun disconnect() {
        autoReconnect = false
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = ConnectionState.Disconnected
        flowControlManager.reset()
    }
    
    override suspend fun sendCommand(command: AmuletCommand): BleResult {
        return retryPolicy.executeWithRetry(
            operation = { sendCommandInternal(command) }
        )
    }
    
    @SuppressLint("MissingPermission")
    private suspend fun sendCommandInternal(command: AmuletCommand): BleResult {
        val gatt = bluetoothGatt ?: throw IllegalStateException("Not connected")
        val characteristic = txCharacteristic ?: throw IllegalStateException("TX characteristic not found")
        
        val commandString = command.toCommandString()
        val commandBytes = commandString.toByteArray(Charsets.UTF_8)
        
        return withTimeout(GattConstants.COMMAND_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                characteristic.value = commandBytes
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                
                val success = gatt.writeCharacteristic(characteristic)
                
                if (success) {
                    // Ждем ответа через callback
                    continuation.resume(BleResult.Success)
                } else {
                    continuation.resume(BleResult.Error("WRITE_FAILED", "Failed to write command"))
                }
            }
        }
    }
    
    override fun uploadAnimation(plan: AnimationPlan): Flow<UploadProgress> = flow {
        emit(UploadProgress(plan.commands.size, 0, UploadState.Preparing))
        
        try {
            // BEGIN_PLAN
            sendCommandInternal(AmuletCommand.Custom("BEGIN_PLAN", listOf(plan.id)))
            flowControlManager.waitForReady()
            
            emit(UploadProgress(plan.commands.size, 0, UploadState.Uploading))
            
            // ADD_COMMAND для каждой команды
            plan.commands.forEachIndexed { index, command ->
                flowControlManager.executeWithFlowControl {
                    val addCommandStr = "ADD_COMMAND:${index + 1}:${command.toCommandString()}"
                    sendCommandInternal(AmuletCommand.Custom(addCommandStr))
                }
                
                emit(UploadProgress(plan.commands.size, index + 1, UploadState.Uploading))
            }
            
            // COMMIT_PLAN
            emit(UploadProgress(plan.commands.size, plan.commands.size, UploadState.Committing))
            sendCommandInternal(AmuletCommand.Custom("COMMIT_PLAN", listOf(plan.id)))
            
            emit(UploadProgress(plan.commands.size, plan.commands.size, UploadState.Completed))
            
        } catch (e: Exception) {
            emit(UploadProgress(plan.commands.size, 0, UploadState.Failed(e)))
        }
    }
    
    override fun startOtaUpdate(firmwareInfo: FirmwareInfo): Flow<OtaProgress> = flow {
        emit(OtaProgress(firmwareInfo.size, 0, OtaState.Preparing))
        
        try {
            // Загрузка файла прошивки
            val firmwareData = downloadFirmware(firmwareInfo.url)
            
            // START_OTA
            val startCommand = "START_OTA:${firmwareInfo.version}:${firmwareInfo.checksum}"
            sendCommandInternal(AmuletCommand.Custom(startCommand))
            flowControlManager.waitForReady()
            
            emit(OtaProgress(firmwareInfo.size, 0, OtaState.Transferring))
            
            // Отправка чанков
            val chunks = firmwareData.chunked(GattConstants.CHUNK_SIZE)
            chunks.forEachIndexed { index, chunk ->
                flowControlManager.executeWithFlowControl(GattConstants.OTA_CHUNK_TIMEOUT_MS) {
                    val chunkCommand = "OTA_CHUNK:${index + 1}:${chunk.size}:${chunk.toBase64()}"
                    sendCommandInternal(AmuletCommand.Custom(chunkCommand))
                }
                
                val sentBytes = (index + 1) * GattConstants.CHUNK_SIZE.toLong()
                    .coerceAtMost(firmwareInfo.size)
                emit(OtaProgress(firmwareInfo.size, sentBytes, OtaState.Transferring))
            }
            
            // OTA_COMMIT
            emit(OtaProgress(firmwareInfo.size, firmwareInfo.size, OtaState.Verifying))
            sendCommandInternal(AmuletCommand.Custom("OTA_COMMIT"))
            
            emit(OtaProgress(firmwareInfo.size, firmwareInfo.size, OtaState.Installing))
            
            // Ждем завершения установки
            flowControlManager.waitForReady(60_000L)
            
            emit(OtaProgress(firmwareInfo.size, firmwareInfo.size, OtaState.Completed))
            
        } catch (e: Exception) {
            emit(OtaProgress(firmwareInfo.size, 0, OtaState.Failed(e)))
        }
    }
    
    override fun startWifiOtaUpdate(firmwareInfo: FirmwareInfo): Flow<OtaProgress> = flow {
        emit(OtaProgress(firmwareInfo.size, 0, OtaState.Preparing))
        
        try {
            // Отправка команды Wi-Fi OTA
            val command = AmuletCommand.WifiOtaStart(
                url = firmwareInfo.url,
                version = firmwareInfo.version,
                checksum = firmwareInfo.checksum
            )
            
            sendCommandInternal(command)
            
            emit(OtaProgress(firmwareInfo.size, 0, OtaState.Transferring))
            
            // Прогресс отслеживается через уведомления NOTIFY:WIFI_OTA:PROGRESS:XX
            // Финальное состояние устанавливается через уведомления
            
        } catch (e: Exception) {
            emit(OtaProgress(firmwareInfo.size, 0, OtaState.Failed(e)))
        }
    }
    
    override suspend fun getProtocolVersion(): String? {
        return try {
            sendCommandInternal(AmuletCommand.GetProtocolVersion)
            // Версия возвращается через уведомление
            null // TODO: Ожидать ответ через notifications flow
        } catch (e: Exception) {
            null
        }
    }
    
    override fun observeNotifications(): Flow<String> = _notifications.asSharedFlow()
    
    @SuppressLint("MissingPermission")
    private fun createGattCallback(
        continuation: CancellableContinuation<Unit>? = null
    ) = object : BluetoothGattCallback() {
        
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.value = ConnectionState.Connected
                    gatt.requestMtu(GattConstants.PREFERRED_MTU)
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = ConnectionState.Disconnected
                    
                    if (autoReconnect && currentDeviceAddress != null) {
                        scope.launch {
                            reconnectPolicy.attemptReconnection(
                                onAttempt = { attempt ->
                                    _connectionState.value = ConnectionState.Reconnecting(attempt)
                                }
                            ) {
                                connect(currentDeviceAddress!!, autoReconnect = true)
                            }
                        }
                    }
                }
            }
        }
        
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Найти и сохранить характеристики
                val uartService = gatt.getService(GattConstants.NORDIC_UART_SERVICE_UUID)
                txCharacteristic = uartService?.getCharacteristic(GattConstants.NORDIC_UART_TX_CHARACTERISTIC_UUID)
                rxCharacteristic = uartService?.getCharacteristic(GattConstants.NORDIC_UART_RX_CHARACTERISTIC_UUID)
                
                val batteryService = gatt.getService(GattConstants.BATTERY_SERVICE_UUID)
                batteryCharacteristic = batteryService?.getCharacteristic(GattConstants.BATTERY_LEVEL_CHARACTERISTIC_UUID)
                
                val amuletService = gatt.getService(GattConstants.AMULET_DEVICE_SERVICE_UUID)
                deviceInfoCharacteristic = amuletService?.getCharacteristic(GattConstants.AMULET_DEVICE_INFO_CHARACTERISTIC_UUID)
                deviceStatusCharacteristic = amuletService?.getCharacteristic(GattConstants.AMULET_DEVICE_STATUS_CHARACTERISTIC_UUID)
                
                // Включить уведомления
                rxCharacteristic?.let { enableNotifications(gatt, it) }
                batteryCharacteristic?.let { enableNotifications(gatt, it) }
                deviceStatusCharacteristic?.let { enableNotifications(gatt, it) }
                
                _connectionState.value = ConnectionState.ServicesDiscovered
                flowControlManager.reset()
                
                continuation?.resume(Unit)
            } else {
                val exception = Exception("Service discovery failed")
                _connectionState.value = ConnectionState.Failed(exception)
                continuation?.resumeWithException(exception)
            }
        }
        
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            when (characteristic.uuid) {
                GattConstants.NORDIC_UART_RX_CHARACTERISTIC_UUID -> {
                    val message = characteristic.value.toString(Charsets.UTF_8)
                    handleNotification(message)
                }
                GattConstants.BATTERY_LEVEL_CHARACTERISTIC_UUID -> {
                    val level = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                    scope.launch {
                        _batteryLevel.emit(level)
                    }
                }
                GattConstants.AMULET_DEVICE_STATUS_CHARACTERISTIC_UUID -> {
                    val statusData = characteristic.value.toString(Charsets.UTF_8)
                    parseDeviceStatus(statusData)
                }
            }
        }
        
        @SuppressLint("MissingPermission")
        private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            gatt.setCharacteristicNotification(characteristic, true)
            
            val descriptor = characteristic.getDescriptor(GattConstants.CLIENT_CHARACTERISTIC_CONFIG_UUID)
            descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }
    }
    
    private fun handleNotification(message: String) {
        scope.launch {
            // Обработка Flow Control состояний
            if (message.startsWith("STATE:")) {
                flowControlManager.handleDeviceState(message)
            }
            
            // Отправка уведомления в общий поток
            _notifications.emit(message)
        }
    }
    
    private fun parseDeviceStatus(statusData: String) {
        // Парсинг формата: "SERIAL:xxx;FIRMWARE:xxx;HARDWARE:xxx;BATTERY:xx"
        try {
            val parts = statusData.split(";").associate {
                val (key, value) = it.split(":")
                key to value
            }
            
            val status = DeviceStatus(
                serialNumber = parts["SERIAL"] ?: "",
                firmwareVersion = parts["FIRMWARE"] ?: "",
                hardwareVersion = parts["HARDWARE"]?.toIntOrNull() ?: 0,
                batteryLevel = parts["BATTERY"]?.toIntOrNull() ?: 0,
                isCharging = parts["CHARGING"] == "true",
                isOnline = true,
                lastSeen = System.currentTimeMillis()
            )
            
            _deviceStatus.value = status
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse device status", e)
        }
    }
    
    private suspend fun downloadFirmware(url: String): ByteArray {
        return withTimeout(60_000L) {
            URL(url).openStream().use { it.readBytes() }
        }
    }
    
    private fun ByteArray.chunked(size: Int): List<ByteArray> {
        val result = mutableListOf<ByteArray>()
        var offset = 0
        while (offset < this.size) {
            val chunkSize = minOf(size, this.size - offset)
            result.add(copyOfRange(offset, offset + chunkSize))
            offset += chunkSize
        }
        return result
    }
    
    private fun ByteArray.toBase64(): String {
        return android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)
    }
    
    fun cleanup() {
        scope.cancel()
        @SuppressLint("MissingPermission")
        bluetoothGatt?.close()
    }
    
    companion object {
        private const val TAG = "AmuletBleManager"
    }
}
