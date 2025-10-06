package com.example.amulet.core.telemetry.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CrashlyticsAntilogTest {

    private lateinit var crashlytics: FirebaseCrashlytics
    private lateinit var delegate: Antilog
    private lateinit var antilog: CrashlyticsAntilog

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        crashlytics = mockk(relaxed = true)
        delegate = mockk(relaxed = true)
        antilog = CrashlyticsAntilog(crashlytics, delegate)
    }

    @Test
    fun `always delegates logging`() {
        antilog.performLog(LogLevel.DEBUG, "tag", null, "message")

        verify { delegate.log(LogLevel.DEBUG, "tag", null, "message") }
    }

    @Test
    fun `ignores crashlytics calls for levels below warning`() {
        antilog.performLog(LogLevel.INFO, null, null, null)

        verify(exactly = 0) { crashlytics.setCustomKey(any(), any<String>()) }
        verify(exactly = 0) { crashlytics.log(any()) }
        verify(exactly = 0) { crashlytics.recordException(any()) }
    }

    @Test
    fun `reports warning with defaults`() {
        val throwable = IllegalStateException("boom")

        antilog.performLog(LogLevel.WARNING, null, throwable, "something happened")

        verify { crashlytics.setCustomKey("log_priority", "WARNING") }
        verify { crashlytics.setCustomKey("log_tag", "Telemetry") }
        verify { crashlytics.log("something happened") }
        verify { crashlytics.recordException(throwable) }
    }

    @Test
    fun `skips crashlytics message when blank`() {
        antilog.performLog(LogLevel.ERROR, "tag", null, "  ")

        verify { crashlytics.setCustomKey("log_priority", "ERROR") }
        verify { crashlytics.setCustomKey("log_tag", "tag") }
        verify(exactly = 0) { crashlytics.log(any()) }
    }
}
