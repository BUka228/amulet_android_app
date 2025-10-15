package com.example.amulet.core.telemetry.logging

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TelemetryAntilogTest {

    private lateinit var reporter: RecordingTelemetryReporter
    private lateinit var delegate: RecordingAntilog
    private lateinit var antilog: TelemetryAntilog

    @BeforeEach
    fun setUp() {
        reporter = RecordingTelemetryReporter()
        delegate = RecordingAntilog()
        antilog = TelemetryAntilog(
            reporter = reporter,
            delegate = delegate,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        )
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
    fun `does not report logs below warning`() {
        antilog.logForTest(LogLevel.INFO, null, null, null)

        assertTrue(reporter.entries.isEmpty())
    }

    @Test
    fun `reports warning logs`() {
        val throwable = IllegalStateException("boom")

        antilog.logForTest(LogLevel.WARNING, "tag", throwable, "something happened")

        assertEquals(1, reporter.entries.size)
        val entry = reporter.entries.single()
        assertEquals(LogLevel.WARNING, entry.level)
        assertEquals("tag", entry.tag)
        assertEquals("something happened", entry.message)
        assertEquals(throwable, entry.throwable)
    }

    private class RecordingTelemetryReporter : TelemetryReporter {
        data class Entry(
            val level: LogLevel,
            val tag: String?,
            val message: String?,
            val throwable: Throwable?
        )

        val entries = mutableListOf<Entry>()

        override suspend fun reportLog(level: LogLevel, tag: String?, message: String?, throwable: Throwable?) {
            entries += Entry(level, tag, message, throwable)
        }
    }

    private class RecordingAntilog : Antilog() {
        data class LogEntry(
            val priority: LogLevel,
            val tag: String?,
            val throwable: Throwable?,
            val message: String?
        )

        val entries = mutableListOf<LogEntry>()

        override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
            entries += LogEntry(priority, tag, throwable, message)
        }
    }

    private fun TelemetryAntilog.logForTest(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        val method = TelemetryAntilog::class.java.getDeclaredMethod(
            "performLog",
            LogLevel::class.java,
            String::class.java,
            Throwable::class.java,
            String::class.java
        )
        method.isAccessible = true
        method.invoke(this, priority, tag, throwable, message)
    }
}
