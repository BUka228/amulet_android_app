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
import com.example.amulet.core.ble.AmuletBleManager
import com.example.amulet.core.ble.model.AnimationPlan
import com.example.amulet.core.ble.model.BleResult
import com.example.amulet.core.ble.model.ConnectionState
import com.example.amulet.core.ble.model.DeviceReadyState
import com.example.amulet.core.ble.model.DeviceStatus
import com.example.amulet.core.ble.model.FirmwareInfo
import com.example.amulet.core.ble.model.OtaProgress
import com.example.amulet.core.ble.model.OtaState
import com.example.amulet.core.ble.model.UploadProgress
import com.example.amulet.core.ble.model.UploadState
import com.example.amulet.core.ble.model.toCommandString
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.devices.model.AmuletCommand
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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
    
    private val commandResultChannel = Channel<BleResult>(capacity = Channel.BUFFERED)
    
    // Характеристики GATT
    private var txCharacteristic: BluetoothGattCharacteristic? = null
    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    private var batteryCharacteristic: BluetoothGattCharacteristic? = null
    private var deviceInfoCharacteristic: BluetoothGattCharacteristic? = null
    private var deviceStatusCharacteristic: BluetoothGattCharacteristic? = null
    private var otaCharacteristic: BluetoothGattCharacteristic? = null
    private var animationCharacteristic: BluetoothGattCharacteristic? = null
    
    // Счётчик незавершённых writeDescriptor при настройке уведомлений
    private var pendingNotificationDescriptors: Int = 0
    private val notificationDescriptorQueue: MutableList<BluetoothGattDescriptor> = mutableListOf()
    
    // Имя команды, для которой сейчас ожидается OK:...
    private var pendingCommandName: String? = null
    
    @SuppressLint("MissingPermission")
    override suspend fun connect(deviceAddress: String, autoReconnect: Boolean) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Logger.e("connect: Bluetooth adapter is not available or disabled", null, tag = TAG)
            throw IllegalStateException("Bluetooth adapter is not available or disabled")
        }
        
        this.currentDeviceAddress = deviceAddress
        this.autoReconnect = autoReconnect
        
        _connectionState.value = ConnectionState.Connecting
        Logger.d("connect: start deviceAddress=$deviceAddress autoReconnect=$autoReconnect", tag = TAG)
        
        return suspendCancellableCoroutine { continuation ->
            try {
                val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
                
                // Закрываем предыдущий GATT, если был
                bluetoothGatt?.close()
                
                bluetoothGatt = device.connectGatt(
                    context,
                    autoReconnect,
                    createGattCallback(continuation),
                    BluetoothDevice.TRANSPORT_LE
                )
                
                continuation.invokeOnCancellation {
                    bluetoothGatt?.close()
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Failed(e)
                Logger.e("connect: failed for deviceAddress=$deviceAddress", e, tag = TAG)
                continuation.resumeWithException(e)
            }
        }
    }
    
    @SuppressLint("MissingPermission")
    override suspend fun disconnect() {
        Logger.d("disconnect: start", tag = TAG)
        autoReconnect = false
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = ConnectionState.Disconnected
        flowControlManager.reset()
        Logger.d("disconnect: completed", tag = TAG)
    }
    
    override suspend fun sendCommand(command: AmuletCommand): BleResult {
        val commandString = command.toCommandString()
        Logger.d("sendCommand: $commandString", tag = TAG)
        val result = retryPolicy.executeWithRetry(
            operation = { sendCommandInternal(command) }
        )
        Logger.d("sendCommand: result=$result for $commandString", tag = TAG)
        return result
    }
    
    @SuppressLint("MissingPermission")
    private suspend fun sendCommandInternal(command: AmuletCommand): BleResult {
        val gatt = bluetoothGatt ?: run {
            Logger.e("sendCommandInternal: Not connected, command=${command.toCommandString()}", null, tag = TAG)
            throw IllegalStateException("Not connected")
        }
        val commandString = command.toCommandString()
        val commandName = commandString.substringBefore(":")
        pendingCommandName = commandName
        
        // Гарантируем, что сервисы/характеристики обнаружены перед отправкой команды
        ensureServicesDiscovered(gatt)
        
        val characteristic = when {
            commandString.startsWith("START_OTA") ||
                commandString.startsWith("OTA_CHUNK") ||
                commandString.startsWith("OTA_COMMIT") -> otaCharacteristic
            commandString.startsWith("BEGIN_PLAN") ||
                commandString.startsWith("ADD_COMMAND") ||
                commandString.startsWith("COMMIT_PLAN") ||
                commandString.startsWith("ROLLBACK_PLAN") -> animationCharacteristic
            else -> txCharacteristic
        } ?: run {
            Logger.e("sendCommandInternal: Required GATT characteristic is null for command: $commandString", null, tag = TAG)
            throw IllegalStateException("Required GATT characteristic not found for command: $commandString")
        }
        
        val commandBytes = commandString.toByteArray(Charsets.UTF_8)
        
        // Подбираем тип записи: для Nordic UART и похожих характеристик обычно используется WRITE_NO_RESPONSE
        val supportsWriteNoResponse =
            (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0
        val writeType = if (supportsWriteNoResponse) {
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        } else {
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        }
        
        Logger.d(
            "sendCommandInternal: writing command='$commandString' length=${commandBytes.size} to characteristic=${characteristic.uuid} writeType=$writeType supportsWriteNoResponse=$supportsWriteNoResponse",
            tag = TAG
        )
        
        val writeSuccess = withContext(Dispatchers.Main) {
            Logger.d("sendCommandInternal: calling writeCharacteristic on main thread", tag = TAG)
            characteristic.value = commandBytes
            characteristic.writeType = writeType
            gatt.writeCharacteristic(characteristic)
        }
        if (!writeSuccess) {
            Logger.e("sendCommandInternal: writeCharacteristic returned false for command='$commandString'", null, tag = TAG)
            return BleResult.Error("WRITE_FAILED", "Failed to write command")
        }
        
        return try {
            withTimeout(GattConstants.COMMAND_TIMEOUT_MS) {
                commandResultChannel.receive()
            }.also {
                Logger.d("sendCommandInternal: result=$it for command='$commandString'", tag = TAG)
            }
        } catch (e: Exception) {
            Logger.e("sendCommandInternal: timeout waiting for result for command='$commandString'", e, tag = TAG)
            BleResult.Error("TIMEOUT", e.message ?: "Timeout waiting for command response")
        } finally {
            // На всякий случай очищаем pendingCommandName, чтобы не путать последующие OK
            pendingCommandName = null
        }
    }
    
    override fun uploadAnimation(plan: AnimationPlan): Flow<UploadProgress> = flow {
        Logger.d("uploadAnimation: planId=${plan.id} commands=${plan.commands.size}", tag = TAG)
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
            Logger.e("uploadAnimation: failed for planId=${plan.id}", e, tag = TAG)
            emit(UploadProgress(plan.commands.size, 0, UploadState.Failed(e)))
        }
    }
    
    override fun startOtaUpdate(firmwareInfo: FirmwareInfo): Flow<OtaProgress> = flow {
        Logger.d(
            "startOtaUpdate: version=${firmwareInfo.version} size=${firmwareInfo.size} url=${firmwareInfo.url}",
            tag = TAG
        )
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
            Logger.e("startOtaUpdate: failed", e, tag = TAG)
            emit(OtaProgress(firmwareInfo.size, 0, OtaState.Failed(e)))
        }
    }
    
    override fun startWifiOtaUpdate(firmwareInfo: FirmwareInfo): Flow<OtaProgress> = flow {
        Logger.d(
            "startWifiOtaUpdate: version=${firmwareInfo.version} size=${firmwareInfo.size} url=${firmwareInfo.url}",
            tag = TAG
        )
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
            Logger.e("startWifiOtaUpdate: failed", e, tag = TAG)
            emit(OtaProgress(firmwareInfo.size, 0, OtaState.Failed(e)))
        }
    }
    
    override suspend fun getProtocolVersion(): String? {
        return try {
            Logger.d("getProtocolVersion: sending request", tag = TAG)
            sendCommandInternal(AmuletCommand.GetProtocolVersion)
            
            var version: String? = null
            
            withTimeout(GattConstants.COMMAND_TIMEOUT_MS) {
                _notifications.first { message ->
                    when {
                        message.startsWith("NOTIFY:PROTOCOL_VERSION:") -> {
                            version = message.substringAfter("NOTIFY:PROTOCOL_VERSION:")
                            true
                        }
                        message.startsWith("OK:GET_PROTOCOL_VERSION") -> {
                            version = message.substringAfter("OK:GET_PROTOCOL_VERSION:")
                            true
                        }
                        message.startsWith("ERROR:GET_PROTOCOL_VERSION") -> {
                            version = null
                            true
                        }
                        else -> false
                    }
                }
            }
            
            Logger.d("getProtocolVersion: result=$version", tag = TAG)
            version
        } catch (e: Exception) {
            Logger.e("getProtocolVersion: failed", e, tag = TAG)
            null
        }
    }
    
    override fun observeNotifications(): Flow<String> = _notifications.asSharedFlow()
    
    @SuppressLint("MissingPermission")
    private suspend fun ensureServicesDiscovered(gatt: BluetoothGatt) {
        // Если уже в состоянии ServicesDiscovered и характеристики заполнены, ничего не делаем
        if (_connectionState.value is ConnectionState.ServicesDiscovered && txCharacteristic != null) {
            return
        }
        
        Logger.d("ensureServicesDiscovered: starting service discovery", tag = TAG)
        val started = gatt.discoverServices()
        if (!started) {
            Logger.e("ensureServicesDiscovered: discoverServices() returned false", null, tag = TAG)
            throw IllegalStateException("Service discovery failed to start")
        }
        
        val state = try {
            withTimeout(GattConstants.DISCOVERY_TIMEOUT_MS) {
                _connectionState.first { it is ConnectionState.ServicesDiscovered || it is ConnectionState.Failed }
            }
        } catch (e: Exception) {
            Logger.e("ensureServicesDiscovered: timeout waiting for ServicesDiscovered", e, tag = TAG)
            throw IllegalStateException("Service discovery timeout", e)
        }
        
        when (state) {
            is ConnectionState.ServicesDiscovered -> {
                Logger.d("ensureServicesDiscovered: ServicesDiscovered reached", tag = TAG)
            }
            is ConnectionState.Failed -> {
                Logger.e("ensureServicesDiscovered: discovery failed: ${state.cause}", state.cause, tag = TAG)
                throw IllegalStateException("Service discovery failed", state.cause)
            }
            else -> {
                // Не должны сюда попасть, но на всякий случай
                Logger.e("ensureServicesDiscovered: unexpected state=$state", null, tag = TAG)
                throw IllegalStateException("Unexpected connection state after discovery: $state")
            }
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun createGattCallback(
        continuation: CancellableContinuation<Unit>? = null
    ) = object : BluetoothGattCallback() {
        
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Logger.d(
                "onConnectionStateChange: status=$status newState=$newState device=${gatt.device?.address}",
                tag = TAG
            )
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
            Logger.d("onServicesDiscovered: status=$status", tag = TAG)
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
                otaCharacteristic = amuletService?.getCharacteristic(GattConstants.AMULET_OTA_CHARACTERISTIC_UUID)
                animationCharacteristic = amuletService?.getCharacteristic(GattConstants.AMULET_ANIMATION_CHARACTERISTIC_UUID)
                
                Logger.d(
                    "onServicesDiscovered: uartService=${uartService != null} tx=${txCharacteristic != null} " +
                        "rx=${rxCharacteristic != null} battery=${batteryCharacteristic != null} " +
                        "amuletService=${amuletService != null} ota=${otaCharacteristic != null} " +
                        "animation=${animationCharacteristic != null}",
                    tag = TAG
                )
                
                // Готовим очередь дескрипторов для последовательной настройки уведомлений
                pendingNotificationDescriptors = 0
                notificationDescriptorQueue.clear()

                fun enqueueCccd(characteristic: BluetoothGattCharacteristic?) {
                    if (characteristic == null) return
                    val descriptor = characteristic.getDescriptor(GattConstants.CLIENT_CHARACTERISTIC_CONFIG_UUID)
                    if (descriptor != null) {
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        notificationDescriptorQueue.add(descriptor)
                    } else {
                        Logger.w(
                            "onServicesDiscovered: CLIENT_CHARACTERISTIC_CONFIG_UUID descriptor not found for characteristic=${characteristic.uuid}",
                            null,
                            tag = TAG
                        )
                    }
                }

                // Включить уведомления (setCharacteristicNotification) и собрать очередь дескрипторов
                rxCharacteristic?.let { enableNotifications(gatt, it); enqueueCccd(it) }
                batteryCharacteristic?.let { enableNotifications(gatt, it); enqueueCccd(it) }
                deviceStatusCharacteristic?.let { enableNotifications(gatt, it); enqueueCccd(it) }
                animationCharacteristic?.let { enableNotifications(gatt, it); enqueueCccd(it) }

                pendingNotificationDescriptors = notificationDescriptorQueue.size

                if (pendingNotificationDescriptors == 0) {
                    Logger.d("onServicesDiscovered: no descriptors to configure, marking ServicesDiscovered immediately", tag = TAG)
                    _connectionState.value = ConnectionState.ServicesDiscovered
                    flowControlManager.reset()
                    continuation?.resume(Unit)
                } else {
                    Logger.d(
                        "onServicesDiscovered: pendingNotificationDescriptors=$pendingNotificationDescriptors, starting descriptor queue",
                        tag = TAG
                    )
                    writeNextNotificationDescriptor(gatt)
                }
            } else {
                val exception = Exception("Service discovery failed")
                Logger.e("onServicesDiscovered: failed with status=$status", exception, tag = TAG)
                _connectionState.value = ConnectionState.Failed(exception)
                continuation?.resumeWithException(exception)
            }
        }
        
        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            Logger.d(
                "onDescriptorWrite: descriptor=${descriptor.uuid} char=${descriptor.characteristic.uuid} status=$status pendingBefore=$pendingNotificationDescriptors",
                tag = TAG
            )
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Logger.w("onDescriptorWrite: non-success status=$status for descriptor=${descriptor.uuid}", null, tag = TAG)
            }
            if (pendingNotificationDescriptors > 0) {
                pendingNotificationDescriptors--
            }

            if (notificationDescriptorQueue.isNotEmpty()) {
                // Удаляем только что записанный дескриптор из очереди
                if (notificationDescriptorQueue.first() == descriptor) {
                    notificationDescriptorQueue.removeAt(0)
                } else {
                    notificationDescriptorQueue.remove(descriptor)
                }
            }

            if (notificationDescriptorQueue.isEmpty()) {
                Logger.d("onDescriptorWrite: all notification descriptors configured, marking ServicesDiscovered", tag = TAG)
                _connectionState.value = ConnectionState.ServicesDiscovered
                flowControlManager.reset()
                continuation?.resume(Unit)
            } else {
                Logger.d(
                    "onDescriptorWrite: pendingNotificationDescriptors=$pendingNotificationDescriptors, writing next descriptor", 
                    tag = TAG
                )
                writeNextNotificationDescriptor(gatt)
            }
        }
        
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            when (characteristic.uuid) {
                GattConstants.NORDIC_UART_RX_CHARACTERISTIC_UUID -> {
                    val message = characteristic.value.toString(Charsets.UTF_8)
                    Logger.d("onCharacteristicChanged: UART RX '$message'", tag = TAG)
                    handleNotification(message)
                }
                GattConstants.BATTERY_LEVEL_CHARACTERISTIC_UUID -> {
                    val level = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                    Logger.d("onCharacteristicChanged: batteryLevel=$level", tag = TAG)
                    scope.launch {
                        _batteryLevel.emit(level)
                    }
                }
                GattConstants.AMULET_DEVICE_STATUS_CHARACTERISTIC_UUID -> {
                    val statusData = characteristic.value.toString(Charsets.UTF_8)
                    Logger.d("onCharacteristicChanged: deviceStatus='$statusData'", tag = TAG)
                    parseDeviceStatus(statusData)
                }
                GattConstants.AMULET_ANIMATION_CHARACTERISTIC_UUID -> {
                    val message = characteristic.value.toString(Charsets.UTF_8)
                    Logger.d("onCharacteristicChanged: animationMessage='$message'", tag = TAG)
                    handleNotification(message)
                }
            }
        }
        
        @SuppressLint("MissingPermission")
        private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            Logger.d("enableNotifications: characteristic=${characteristic.uuid}", tag = TAG)
            gatt.setCharacteristicNotification(characteristic, true)
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun writeNextNotificationDescriptor(gatt: BluetoothGatt) {
        val next = notificationDescriptorQueue.firstOrNull() ?: return
        Logger.d(
            "writeNextNotificationDescriptor: descriptor=${next.uuid} char=${next.characteristic.uuid} remaining=${notificationDescriptorQueue.size}",
            tag = TAG
        )
        val started = gatt.writeDescriptor(next)
        if (!started) {
            Logger.w(
                "writeNextNotificationDescriptor: writeDescriptor returned false for descriptor=${next.uuid}, skipping remaining and marking ServicesDiscovered",
                null,
                tag = TAG
            )
            notificationDescriptorQueue.clear()
            pendingNotificationDescriptors = 0
            _connectionState.value = ConnectionState.ServicesDiscovered
            flowControlManager.reset()
        }
    }
    
    private fun handleNotification(message: String) {
        scope.launch {
            Logger.d("BLE Notify: $message", tag = TAG)
            // Обработка Flow Control состояний
            if (message.startsWith("STATE:")) {
                flowControlManager.handleDeviceState(message)
            }
            
            if (message.startsWith("OK:")) {
                val okCommandName = message.removePrefix("OK:").substringBefore(":")
                val expected = pendingCommandName
                if (expected == null || expected == okCommandName) {
                    Logger.d(
                        "handleNotification: OK matched for commandName=$okCommandName expected=$expected message='$message'",
                        tag = TAG
                    )
                    pendingCommandName = null
                    commandResultChannel.trySend(BleResult.Success)
                } else {
                    Logger.d(
                        "handleNotification: ignoring OK for commandName=$okCommandName, pending=$expected message='$message'",
                        tag = TAG
                    )
                }
            } else if (message.startsWith("ERROR:")) {
                val parts = message.split(":", limit = 4)
                val code = parts.getOrNull(2) ?: "UNKNOWN_ERROR"
                val errorMessage = parts.getOrNull(3) ?: "Device reported error"
                Logger.w(
                    "handleNotification: ERROR message='$message' code=$code errorMessage=$errorMessage",
                    null,
                    tag = TAG
                )
                pendingCommandName = null
                commandResultChannel.trySend(BleResult.Error(code, errorMessage))
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
                firmwareVersion = parts["FIRMWARE"] ?: "",
                hardwareVersion = parts["HARDWARE"]?.toIntOrNull() ?: 0,
                batteryLevel = parts["BATTERY"]?.toIntOrNull() ?: 0,
                isCharging = parts["CHARGING"] == "true",
                isOnline = true,
                lastSeen = System.currentTimeMillis()
            )
            
            _deviceStatus.value = status
        } catch (e: Exception) {
            Logger.e("parseDeviceStatus: failed for '$statusData'", e, tag = TAG)
        }
    }
    
    private suspend fun downloadFirmware(url: String): ByteArray {
        Logger.d("downloadFirmware: url=$url", tag = TAG)
        return withTimeout(60_000L) {
            URL(url).openStream().use { it.readBytes() }
        }.also { data ->
            Logger.d("downloadFirmware: loaded bytes=${data.size}", tag = TAG)
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
        Logger.d("cleanup: closing GATT and cancelling scope", tag = TAG)
        scope.cancel()
        @SuppressLint("MissingPermission")
        bluetoothGatt?.close()
    }
    
    companion object {
        private const val TAG = "AmuletBleManager"
    }
}
