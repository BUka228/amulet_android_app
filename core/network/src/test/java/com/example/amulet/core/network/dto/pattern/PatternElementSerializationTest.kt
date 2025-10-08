package com.example.amulet.core.network.dto.pattern

import com.example.amulet.core.network.serialization.JsonProvider
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PatternElementSerializationTest {

    private val json: Json = JsonProvider.create()

    @Test
    fun `decodes gradient element`() {
        val payload = """
            {
              "type":"light",
              "hardwareVersion":100,
              "elements":[
                {
                  "type":"gradient",
                  "startTime":0,
                  "duration":1000,
                  "params":{
                    "colors":["#FF0000","#00FF00"],
                    "direction":"clockwise"
                  }
                }
              ]
            }
        """.trimIndent()

        val spec = json.decodeFromString<PatternSpecDto>(payload)

        assertEquals("light", spec.type)
        val element = spec.elements.first() as PatternElementGradientDto
        assertEquals(listOf("#FF0000", "#00FF00"), element.params.colors)
        assertEquals("clockwise", element.params.direction)
    }

    @Test
    fun `round trip sequence element`() {
        val spec = PatternSpecDto(
            type = "custom",
            hardwareVersion = 100,
            elements = listOf(
                PatternElementSequenceDto(
                    startTime = 0,
                    duration = 500,
                    params = SequenceParamsDto(
                        steps = listOf(
                            LedActionDto(ledIndex = 0, color = "#FFFFFF", durationMs = 100),
                            DelayActionDto(durationMs = 50)
                        )
                    )
                )
            )
        )

        val encoded = json.encodeToString(spec)
        assertTrue(encoded.contains("\"type\":\"sequence\""))

        val decoded = json.decodeFromString<PatternSpecDto>(encoded)
        val sequence = decoded.elements.first() as PatternElementSequenceDto
        assertEquals(2, sequence.params.steps.size)
        assertTrue(sequence.params.steps.first() is LedActionDto)
    }
}
