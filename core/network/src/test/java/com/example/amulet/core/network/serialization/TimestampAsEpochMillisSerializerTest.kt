package com.example.amulet.core.network.serialization

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class TimestampAsEpochMillisSerializerTest {

    private val json: Json = JsonProvider.create()

    @Test
    fun `deserializes ISO timestamp`() {
        val timestamp = json.decodeFromString<ApiTimestamp>("\"2024-01-01T00:00:00Z\"")

        assertEquals(1704067200000L, timestamp.value)
    }

    @Test
    fun `deserializes firestore timestamp object`() {
        val value = json.decodeFromString<ApiTimestamp>(
            """{"_seconds":1704067200,"_nanoseconds":500000000}"""
        )

        assertEquals(1704067200500L, value.value)
    }

    @Test
    fun `serializes back to ISO string`() {
        val encoded = json.encodeToString(ApiTimestamp(1704067200000L))

        assertEquals("\"2024-01-01T00:00:00Z\"", encoded)
    }
}
