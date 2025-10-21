package com.example.amulet.core.ble.internal

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Политика автоматического переподключения при потере связи.
 */
@Singleton
class ReconnectPolicy @Inject constructor() {
    
    val maxAttempts: Int = 5
    val baseDelayMs: Long = 1_000L
    val maxDelayMs: Long = 30_000L
    val backoffMultiplier: Double = 2.0
    
    /**
     * Выполнить попытку переподключения с экспоненциальной задержкой.
     * 
     * @param attempt Номер попытки (начиная с 0)
     * @param action Действие для выполнения (подключение)
     * @return true если успешно, false если исчерпаны попытки
     */
    suspend fun <T> attemptReconnection(
        onAttempt: (attempt: Int) -> Unit = {},
        action: suspend () -> T
    ): Result<T> {
        var attempt = 0
        var currentDelay = baseDelayMs
        var lastException: Exception? = null
        
        while (attempt < maxAttempts) {
            try {
                onAttempt(attempt)
                return Result.success(action())
            } catch (e: Exception) {
                lastException = e
                attempt++
                
                if (attempt < maxAttempts) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * backoffMultiplier).toLong()
                        .coerceAtMost(maxDelayMs)
                }
            }
        }
        
        return Result.failure(
            lastException ?: Exception("Failed to reconnect after $maxAttempts attempts")
        )
    }
    
    /**
     * Вычислить задержку для указанной попытки.
     */
    fun calculateDelay(attempt: Int): Long {
        val delay = (baseDelayMs * Math.pow(backoffMultiplier, attempt.toDouble())).toLong()
        return delay.coerceAtMost(maxDelayMs)
    }
}

/**
 * Политика повторных попыток для команд.
 */
@Singleton
class RetryPolicy @Inject constructor() {
    
    val maxRetries: Int = 3
    val baseDelayMs: Long = 1_000L
    val maxDelayMs: Long = 5_000L
    
    /**
     * Выполнить операцию с повторными попытками при ошибке.
     * 
     * @param operation Операция для выполнения
     * @param maxRetries Максимальное количество повторов
     * @return Результат операции
     */
    suspend fun <T> executeWithRetry(
        operation: suspend () -> T,
        maxRetries: Int = this.maxRetries
    ): T {
        var lastException: Exception? = null
        var currentDelay = baseDelayMs
        
        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * 2).coerceAtMost(maxDelayMs)
                }
            }
        }
        
        throw lastException ?: Exception("Operation failed after $maxRetries attempts")
    }
}
