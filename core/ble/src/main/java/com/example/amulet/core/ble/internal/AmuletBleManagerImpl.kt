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
import com.example.amulet.shared.domain.devices.model.NotificationType
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    private suspend fun sendCommandInternal(
        command: AmuletCommand,
        timeoutMs: Long = GattConstants.COMMAND_TIMEOUT_MS
    ): BleResult {
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
                commandString.startsWith("ADD_SEGMENTS") ||
                commandString.startsWith("COMMIT_PLAN") ||
                commandString.startsWith("ROLLBACK_PLAN") -> animationCharacteristic
            else -> txCharacteristic
        } ?: run {
            Logger.e("sendCommandInternal: Required GATT characteristic is null for command: $commandString", null, tag = TAG)
            throw IllegalStateException("Required GATT characteristic not found for command: $commandString")
        }
        
        val commandBytes = commandString.toByteArray(Charsets.UTF_8)
        
        // Для настроечных команд (яркость/вибро) надёжнее всегда писать с подтверждением
        val forceWriteWithResponse = commandName == "SET_BRIGHTNESS" || commandName == "SET_VIB_STRENGTH"
        
        // Подбираем тип записи: по умолчанию WRITE_NO_RESPONSE, но для forceWriteWithResponse — WRITE_DEFAULT
        val supportsWriteNoResponse =
            (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0
        val writeType = if (!forceWriteWithResponse && supportsWriteNoResponse) {
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
            withTimeout(timeoutMs) {
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
        val totalBytes = plan.payload.size
        val totalChunks = if (totalBytes == 0) 0 else ((totalBytes + GattConstants.ANIMATION_PAYLOAD_CHUNK_SIZE - 1) / GattConstants.ANIMATION_PAYLOAD_CHUNK_SIZE)

        Logger.d("uploadAnimation: planId=${plan.id} payloadBytes=$totalBytes totalChunks=$totalChunks", tag = TAG)
        emit(UploadProgress(totalChunks, 0, UploadState.Preparing))

        suspend fun uploadOnce(): BleResult {
            // BEGIN_PLAN:pattern_id:total_duration_ms
            val beginPlanParameters = buildList {
                add(plan.id)
                add(plan.totalDurationMs.toString())
                if (plan.isPreview) {
                    add("PREVIEW")
                }
            }
            sendCommandInternal(
                AmuletCommand.Custom(
                    command = "BEGIN_PLAN",
                    parameters = beginPlanParameters
                )
            )
            flowControlManager.waitForReady()

            emit(UploadProgress(totalChunks, 0, UploadState.Uploading))

            val chunks = if (totalBytes == 0) emptyList() else plan.payload.chunked(GattConstants.ANIMATION_PAYLOAD_CHUNK_SIZE)
            chunks.forEachIndexed { index, chunk ->
                flowControlManager.executeWithFlowControl(GattConstants.COMMAND_TIMEOUT_MS) {
                    val base64 = chunk.toBase64()
                    val addSegmentsCommand = AmuletCommand.Custom(
                        command = "ADD_SEGMENTS",
                        parameters = listOf(plan.id, (index + 1).toString(), base64)
                    )
                    sendCommandInternal(addSegmentsCommand)
                }

                emit(UploadProgress(totalChunks, index + 1, UploadState.Uploading))
            }

            // COMMIT_PLAN
            emit(UploadProgress(totalChunks, totalChunks, UploadState.Committing))
            val commitTimeoutMs = (plan.totalDurationMs + GattConstants.COMMAND_TIMEOUT_MS)
                .coerceAtMost(GattConstants.ANIMATION_TIMEOUT_MS)
            Logger.d(
                "uploadAnimation: COMMIT_PLAN with timeoutMs=$commitTimeoutMs totalDurationMs=${plan.totalDurationMs}",
                tag = TAG
            )
            return sendCommandInternal(
                AmuletCommand.Custom("COMMIT_PLAN", listOf(plan.id)),
                timeoutMs = commitTimeoutMs
            )
        }

        try {
            val firstResult = uploadOnce()
            val finalResult = if (firstResult is BleResult.Error) {
                Logger.w(
                    "uploadAnimation: COMMIT_PLAN failed on first attempt for planId=${plan.id}, result=$firstResult, retrying once",
                    null,
                    tag = TAG
                )
                val secondResult = uploadOnce()
                if (secondResult is BleResult.Error) {
                    Logger.e(
                        "uploadAnimation: COMMIT_PLAN failed on retry for planId=${plan.id}, result=$secondResult",
                        null,
                        tag = TAG
                    )
                    emit(UploadProgress(totalChunks, 0, UploadState.Failed(Exception("COMMIT_PLAN failed: $secondResult"))))
                    return@flow
                }
                secondResult
            } else {
                firstResult
            }

            if (finalResult is BleResult.Success) {
                emit(UploadProgress(totalChunks, totalChunks, UploadState.Completed))
            }

        } catch (e: Exception) {
            Logger.e("uploadAnimation: failed for planId=${plan.id}", e, tag = TAG)
            emit(UploadProgress(totalChunks, 0, UploadState.Failed(e)))
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
    
    override suspend fun getProtocolVersion(): String? = coroutineScope {
        try {
            Logger.d("getProtocolVersion: sending request", tag = TAG)

            var version: String? = null

            val waitJob = async {
                withTimeout(GattConstants.COMMAND_TIMEOUT_MS) {
                    _notifications.first { message ->
                        when {
                            message.startsWith("NOTIFY:PROTOCOL_VERSION:") -> {
                                version = message.substringAfter("NOTIFY:PROTOCOL_VERSION:")
                                true
                            }
                            // Поддержка формата OK:GET_PROTOCOL_VERSION[:vX.Y]
                            message.startsWith("OK:GET_PROTOCOL_VERSION") -> {
                                val suffix = message.substringAfter(
                                    delimiter = "OK:GET_PROTOCOL_VERSION:",
                                    missingDelimiterValue = ""
                                )
                                if (suffix.isNotEmpty()) {
                                    version = suffix
                                }
                                true
                            }
                            // Текущая прошивка отправляет версию отдельной строкой вида "v2.0"
                            message.startsWith("v") && message.getOrNull(1)?.isDigit() == true -> {
                                version = message
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
            }

            sendCommandInternal(AmuletCommand.GetProtocolVersion)

            waitJob.await()

            Logger.d("getProtocolVersion: result=$version", tag = TAG)
            version
        } catch (e: Exception) {
            Logger.e("getProtocolVersion: failed", e, tag = TAG)
            null
        }
    }
    
    override fun observeNotifications(type: NotificationType?): Flow<String> {
        if (type == null) return _notifications.asSharedFlow()
        return _notifications.asSharedFlow().filter { message ->
            when (type) {
                NotificationType.BATTERY -> message.startsWith("NOTIFY:BATTERY:")
                NotificationType.STATUS -> message.startsWith("NOTIFY:STATUS:")
                NotificationType.OTA -> message.startsWith("NOTIFY:OTA:")
                NotificationType.WIFI_OTA -> message.startsWith("NOTIFY:WIFI_OTA:")
                NotificationType.PATTERN -> message.startsWith("NOTIFY:PATTERN:")
                NotificationType.ANIMATION -> message.startsWith("NOTIFY:ANIMATION:")
                NotificationType.CUSTOM -> message.startsWith("NOTIFY:")
            }
        }
    }
    
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
        private var connectContinuationCompleted: Boolean = false
        
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Logger.d(
                "onConnectionStateChange: status=$status newState=$newState device=${gatt.device?.address}",
                tag = TAG
            )
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.value = ConnectionState.Connected
                    if (!connectContinuationCompleted && continuation != null) {
                        connectContinuationCompleted = true
                        continuation.resume(Unit)
                    }
                    gatt.requestMtu(GattConstants.PREFERRED_MTU)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = ConnectionState.Disconnected
                    if (!connectContinuationCompleted && continuation != null) {
                        connectContinuationCompleted = true
                        val exception = Exception("Disconnected while connecting")
                        Logger.e("onConnectionStateChange: disconnected before connect completed", exception, tag = TAG)
                        continuation.resumeWithException(exception)
                    }
                    
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
                        val previous = _deviceStatus.value
                        val updated = previous?.copy(
                            batteryLevel = level,
                            isOnline = true,
                            lastSeen = System.currentTimeMillis()
                        ) ?: DeviceStatus(
                            firmwareVersion = "",
                            hardwareVersion = 0,
                            batteryLevel = level,
                            isCharging = false,
                            isOnline = true,
                            lastSeen = System.currentTimeMillis()
                        )
                        _deviceStatus.value = updated
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
        
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Logger.d(
                "onCharacteristicRead: char=${characteristic.uuid} status=$status",
                tag = TAG
            )
            if (status != BluetoothGatt.GATT_SUCCESS) return

            when (characteristic.uuid) {
                GattConstants.BATTERY_LEVEL_CHARACTERISTIC_UUID -> {
                    val level = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                    Logger.d("onCharacteristicRead: batteryLevel=$level", tag = TAG)
                    scope.launch {
                        _batteryLevel.emit(level)
                        val previous = _deviceStatus.value
                        val updated = previous?.copy(
                            batteryLevel = level,
                            isOnline = true,
                            lastSeen = System.currentTimeMillis()
                        ) ?: DeviceStatus(
                            firmwareVersion = "",
                            hardwareVersion = 0,
                            batteryLevel = level,
                            isCharging = false,
                            isOnline = true,
                            lastSeen = System.currentTimeMillis()
                        )
                        _deviceStatus.value = updated
                    }
                }
                GattConstants.AMULET_DEVICE_STATUS_CHARACTERISTIC_UUID -> {
                    val statusData = characteristic.value.toString(Charsets.UTF_8)
                    Logger.d("onCharacteristicRead: deviceStatus='$statusData'", tag = TAG)
                    parseDeviceStatus(statusData)
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
                if (message == "STATE:READY_FOR_DATA") {
                    readInitialStatusIfNeeded()
                }
            }
            
            if (message.startsWith("OK:")) {
                val okCommandName = message.removePrefix("OK:").substringBefore(":")
                val expected = pendingCommandName
                if (expected == null) {
                    // Никто не ждёт результата — это спонтанный OK (например, от PLAY_LAST/CLEAR_ALL), не трогаем канал
                    Logger.d(
                        "handleNotification: OK with no pending command, commandName=$okCommandName message='$message'",
                        tag = TAG
                    )
                } else if (expected == okCommandName) {
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

            // Нотификации о запуске/завершении анимации
            when {
                message.startsWith("NOTIFY:PATTERN:STARTED:") -> {
                    val patternId = message.substringAfter("NOTIFY:PATTERN:STARTED:")
                    Logger.d("handleNotification: pattern started id=$patternId", tag = TAG)
                }
                message.startsWith("NOTIFY:ANIMATION:COMPLETE:") -> {
                    val patternId = message.substringAfter("NOTIFY:ANIMATION:COMPLETE:")
                    Logger.d("handleNotification: animation complete id=$patternId", tag = TAG)
                }
            }
            
            // Отправка уведомления в общий поток
            _notifications.emit(message)
        }
    }

    private suspend fun readInitialStatusIfNeeded() {
        if (_deviceStatus.value != null) return

        val gatt = bluetoothGatt ?: return
        val statusChar = deviceStatusCharacteristic
        val batteryChar = batteryCharacteristic

        withContext(Dispatchers.Main) {
            var readStarted = false
            if (statusChar != null) {
                readStarted = gatt.readCharacteristic(statusChar)
                Logger.d("readInitialStatusIfNeeded: read statusChar started=$readStarted", tag = TAG)
            }
            if (!readStarted && batteryChar != null) {
                val batteryRead = gatt.readCharacteristic(batteryChar)
                Logger.d("readInitialStatusIfNeeded: read batteryChar started=$batteryRead", tag = TAG)
            }
        }
    }
    
    private fun parseDeviceStatus(statusData: String) {
        // Парсинг формата: "SERIAL:xxx;FIRMWARE:xxx;HARDWARE:xxx;BATTERY:xx"
        try {
            val raw = statusData.trim()

            // Некоторые прошивки могут отправлять укороченный статус, например "READY"
            if (raw.equals("READY", ignoreCase = true)) {
                val previous = _deviceStatus.value
                val status = previous?.copy(
                    isOnline = true,
                    lastSeen = System.currentTimeMillis()
                ) ?: DeviceStatus(
                    firmwareVersion = "",
                    hardwareVersion = 0,
                    batteryLevel = 0,
                    isCharging = false,
                    isOnline = true,
                    lastSeen = System.currentTimeMillis()
                )

                Logger.d("parseDeviceStatus: READY shorthand, using status=$status from '$statusData'", tag = TAG)
                _deviceStatus.value = status
                return
            }

            val parts = raw
                .split(";")
                .mapNotNull { token ->
                    val idx = token.indexOf(":")
                    if (idx <= 0 || idx == token.lastIndex) return@mapNotNull null
                    val key = token.substring(0, idx)
                    val value = token.substring(idx + 1)
                    key to value
                }
                .toMap()

            if (parts.isEmpty()) {
                Logger.w("parseDeviceStatus: unsupported format '$statusData'", null, tag = TAG)
                return
            }
            
            val status = DeviceStatus(
                firmwareVersion = parts["FIRMWARE"] ?: "",
                hardwareVersion = parts["HARDWARE"]?.toIntOrNull() ?: 0,
                batteryLevel = parts["BATTERY"]?.toIntOrNull() ?: 0,
                isCharging = parts["CHARGING"] == "true",
                isOnline = true,
                lastSeen = System.currentTimeMillis()
            )
            
            Logger.d("parseDeviceStatus: parsed status=$status from '$statusData'", tag = TAG)
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
