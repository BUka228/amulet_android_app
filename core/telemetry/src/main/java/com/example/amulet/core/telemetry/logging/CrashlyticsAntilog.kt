package com.example.amulet.core.telemetry.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

/**
 * Антилог, интегрированный с Firebase Crashlytics.
 *
 * Делегирует базовое логирование в переданный [delegate], а события
 * уровня WARNING и выше дополнительно отправляет в Crashlytics вместе
 * с ключами приоритета и тега, сообщением и исключением (если есть).
 */
class CrashlyticsAntilog(
    private val crashlytics: FirebaseCrashlytics,
    private val delegate: Antilog
) : Antilog() {

    /**
     * Логирует через делегата и, при необходимости, отправляет данные в Crashlytics.
     *
     * - Всегда вызывает [delegate.log].
     * - Для уровней ниже [LogLevel.WARNING] прекращает обработку.
     * - Для WARNING+ записывает ключи и сообщение в Crashlytics, а также исключение.
     *
     * @param priority Уровень лога Napier
     * @param tag Необязательный тег сообщения
     * @param throwable Исключение для передачи в Crashlytics (опционально)
     * @param message Текст сообщения (опционально)
     */
    override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
        delegate.log(priority, tag, throwable, message)

        if (priority.ordinal < LogLevel.WARNING.ordinal) return

        crashlytics.setCustomKey(KEY_PRIORITY, priority.name)
        crashlytics.setCustomKey(KEY_TAG, tag ?: DEFAULT_TAG)

        if (!message.isNullOrBlank()) {
            crashlytics.log(message)
        }

        throwable?.let(crashlytics::recordException)
    }

    /** Ключи и значения по умолчанию для Crashlytics. */
    private companion object {
        const val KEY_PRIORITY = "log_priority"
        const val KEY_TAG = "log_tag"
        const val DEFAULT_TAG = "Telemetry"
    }
}
