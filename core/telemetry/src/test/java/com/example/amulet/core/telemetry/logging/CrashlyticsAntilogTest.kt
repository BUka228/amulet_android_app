package com.example.amulet.core.telemetry.logging

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CrashlyticsAntilogTest {

    private lateinit var crashlytics: RecordingCrashlyticsReporter
    private lateinit var delegate: RecordingAntilog
    private lateinit var antilog: CrashlyticsAntilog

    @BeforeEach
    fun setUp() {
        crashlytics = RecordingCrashlyticsReporter()
        delegate = RecordingAntilog()
        antilog = CrashlyticsAntilog(crashlytics, delegate)
    }

    @Test
    fun `always delegates logging`() {
        antilog.logForTest(LogLevel.DEBUG, "tag", null, "message")

        assertEquals(
            listOf(RecordingAntilog.LogEntry(LogLevel.DEBUG, "tag", null, "message")),
            delegate.entries
        )
    }

    @Test
    fun `ignores crashlytics calls for levels below warning`() {
        antilog.logForTest(LogLevel.INFO, null, null, null)

        assertTrue(crashlytics.customKeys.isEmpty())
        assertTrue(crashlytics.loggedMessages.isEmpty())
        assertTrue(crashlytics.recordedExceptions.isEmpty())
    }

    @Test
    fun `reports warning with defaults`() {
        val throwable = IllegalStateException("boom")

        antilog.logForTest(LogLevel.WARNING, null, throwable, "something happened")

        assertEquals(
            listOf("log_priority" to "WARNING", "log_tag" to "Telemetry"),
            crashlytics.customKeys
        )
        assertEquals(listOf("something happened"), crashlytics.loggedMessages)
        assertEquals(listOf(throwable), crashlytics.recordedExceptions)
    }

    @Test
    fun `skips crashlytics message when blank`() {
        antilog.logForTest(LogLevel.ERROR, "tag", null, "  ")

        assertEquals(
            listOf("log_priority" to "ERROR", "log_tag" to "tag"),
            crashlytics.customKeys
        )
        assertTrue(crashlytics.loggedMessages.isEmpty())
        assertTrue(crashlytics.recordedExceptions.isEmpty())
    }

    private fun CrashlyticsAntilog.logForTest(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        val method = CrashlyticsAntilog::class.java.getDeclaredMethod(
            "performLog",
            LogLevel::class.java,
            String::class.java,
            Throwable::class.java,
            String::class.java
        )
        method.isAccessible = true
        method.invoke(this, priority, tag, throwable, message)
    }

    private class RecordingCrashlyticsReporter : CrashlyticsReporter {
        val customKeys = mutableListOf<Pair<String, String>>()
        val loggedMessages = mutableListOf<String>()
        val recordedExceptions = mutableListOf<Throwable>()

        override fun setCustomKey(key: String, value: String) {
            customKeys += key to value
        }

        override fun log(message: String) {
            loggedMessages += message
        }

        override fun recordException(throwable: Throwable) {
            recordedExceptions += throwable
        }
    }

    private class RecordingAntilog : Antilog() {
        val entries = mutableListOf<LogEntry>()

        override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
            entries += LogEntry(priority, tag, throwable, message)
        }

        data class LogEntry(
            val priority: LogLevel,
            val tag: String?,
            val throwable: Throwable?,
            val message: String?
        )
    }
}
