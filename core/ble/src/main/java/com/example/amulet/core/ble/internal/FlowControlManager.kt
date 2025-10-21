package com.example.amulet.core.ble.internal

import com.example.amulet.core.ble.model.DeviceReadyState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Менеджер управления потоком данных (Flow Control) для BLE.
 * 
 * Реализует механизм подтверждения готовности устройства для предотвращения
 * переполнения буфера и потери данных при OTA и загрузке анимаций.
 * 
 * Протокол:
 * 1. Приложение отправляет пакет данных
 * 2. Устройство переходит в состояние PROCESSING
 * 3. После обработки устройство отправляет STATE:READY_FOR_DATA
 * 4. Приложение отправляет следующий пакет
 */
@Singleton
class FlowControlManager @Inject constructor() {
    
    private val _readyState = MutableStateFlow<DeviceReadyState>(DeviceReadyState.Busy)
    val readyState: StateFlow<DeviceReadyState> = _readyState.asStateFlow()
    
    /**
     * Ожидать готовности устройства к приему данных.
     * 
     * @param timeoutMs Таймаут ожидания в миллисекундах
     * @throws kotlinx.coroutines.TimeoutCancellationException если таймаут превышен
     */
    suspend fun waitForReady(timeoutMs: Long = DEFAULT_TIMEOUT_MS): DeviceReadyState {
        return withTimeout(timeoutMs) {
            readyState.first { it is DeviceReadyState.ReadyForData }
        }
    }
    
    /**
     * Выполнить операцию с контролем потока.
     * Ожидает готовности устройства, выполняет операцию и устанавливает статус Processing.
     * 
     * @param timeoutMs Таймаут ожидания готовности
     * @param operation Операция для выполнения
     */
    suspend fun <T> executeWithFlowControl(
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        operation: suspend () -> T
    ): T {
        waitForReady(timeoutMs)
        _readyState.value = DeviceReadyState.Processing
        
        return try {
            operation()
        } catch (e: Exception) {
            _readyState.value = DeviceReadyState.Error("OPERATION_FAILED", e.message ?: "Unknown error")
            throw e
        }
    }
    
    /**
     * Обработать сообщение о состоянии от устройства.
     * 
     * @param stateMessage Сообщение вида "STATE:READY_FOR_DATA", "STATE:PROCESSING" и т.д.
     */
    fun handleDeviceState(stateMessage: String) {
        val state = when {
            stateMessage == "STATE:READY_FOR_DATA" -> DeviceReadyState.ReadyForData
            stateMessage == "STATE:PROCESSING" -> DeviceReadyState.Processing
            stateMessage == "STATE:BUSY" -> DeviceReadyState.Busy
            stateMessage.startsWith("STATE:ERROR") -> {
                val parts = stateMessage.split(":")
                val code = parts.getOrNull(2) ?: "UNKNOWN_ERROR"
                val message = parts.getOrNull(3) ?: "Device reported error"
                DeviceReadyState.Error(code, message)
            }
            else -> return // Неизвестное состояние, игнорируем
        }
        
        _readyState.value = state
    }
    
    /**
     * Сбросить состояние в ReadyForData.
     */
    fun reset() {
        _readyState.value = DeviceReadyState.ReadyForData
    }
    
    /**
     * Установить состояние ошибки.
     */
    fun setError(code: String, message: String) {
        _readyState.value = DeviceReadyState.Error(code, message)
    }
    
    companion object {
        private const val DEFAULT_TIMEOUT_MS = 10_000L
    }
}
