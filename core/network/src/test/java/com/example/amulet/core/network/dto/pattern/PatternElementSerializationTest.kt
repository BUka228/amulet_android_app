package com.example.amulet.core.network.dto.pattern

import com.example.amulet.core.network.serialization.JsonProvider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class PatternElementSerializationTest {

    private val json: Json = JsonProvider.create()

    @Test
    fun `round trip spec with timeline`() {
        val spec = PatternSpecDto(
            type = "custom",
            hardwareVersion = 100,
            duration = 1000,
            loop = true,
            timeline = PatternTimelineDto(
                durationMs = 1000,
                tracks = listOf(
                    TimelineTrackDto(
                        target = TargetRingDto,
                        clips = listOf(
                            TimelineClipDto(
                                startMs = 0,
                                durationMs = 1000,
                                color = "#FF0000"
                            )
                        )
                    )
                )
            )
        )

        val encoded = json.encodeToString(spec)
        val decoded = json.decodeFromString<PatternSpecDto>(encoded)

        assertEquals(spec, decoded)
    }
}
