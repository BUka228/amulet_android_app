package com.example.amulet.core.database.dao

import com.example.amulet.core.database.BaseDatabaseTest
import com.example.amulet.core.database.entity.TelemetryEventEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TelemetryDaoTest : BaseDatabaseTest() {

    private lateinit var telemetryDao: TelemetryDao

    @Before
    fun prepare() {
        telemetryDao = database.telemetryDao()
    }

    @Test
    fun `mark sent updates events`() = runTest {
        val event = TelemetryEventEntity(
            id = "event",
            userId = "user",
            type = "app_open",
            payloadJson = "{}",
            createdAt = 10L,
            timestamp = 10L,
            sentAt = null
        )
        telemetryDao.upsert(event)

        telemetryDao.markSent(ids = listOf(event.id), sentAt = 20L)

        val pending = telemetryDao.findPending(limit = 10)
        assertEquals(emptyList<TelemetryEventEntity>(), pending)
        val observed = telemetryDao.observeByUser("user").first()
        assertEquals(20L, observed.first().sentAt)
    }

    @Test
    fun `cleanup removes sent events before cutoff`() = runTest {
        val oldEvent = TelemetryEventEntity("old", "user", "type", "{}", 0L, 0L, 5L)
        val newEvent = TelemetryEventEntity("new", "user", "type", "{}", 100L, 100L, 120L)
        telemetryDao.upsert(listOf(oldEvent, newEvent))

        val removed = telemetryDao.cleanupSent(cutoffTime = 100L)
        assertEquals(1, removed)
        val remaining = telemetryDao.findPending(limit = 10)
        assertEquals(emptyList<TelemetryEventEntity>(), remaining)
    }
}
