package com.example.amulet.core.telemetry.logging

import android.util.Log
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

/**
 * Антилог для релизной сборки.
 *
 * Фильтрует сообщения ниже уровня INFO и проксирует вывод
 * в android.util.Log, подставляя дефолтный тег при его отсутствии.
 */
class ReleaseAntilog : Antilog() {

    /**
     * Логирует сообщение в зависимости от уровня приоритета.
     *
     * - Игнорирует уровни ниже [LogLevel.INFO].
     * - Использует [DEFAULT_TAG], если тег не задан.
     *
     * @param priority Уровень лога Napier
     * @param tag Необязательный тег
     * @param throwable Исключение для стека (опционально)
     * @param message Текст сообщения (опционально)
     */
    override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
        if (priority.ordinal < LogLevel.INFO.ordinal) return

        val usedTag = tag ?: DEFAULT_TAG
        val text = message ?: ""

        when (priority) {
            LogLevel.INFO -> Log.i(usedTag, text, throwable)
            LogLevel.WARNING -> Log.w(usedTag, text, throwable)
            LogLevel.ERROR, LogLevel.ASSERT -> Log.e(usedTag, text, throwable)
            else -> Log.i(usedTag, text, throwable)
        }
    }

    /** Константы по умолчанию для логирования. */
    private companion object {
        const val DEFAULT_TAG = "Amulet"
    }
}
