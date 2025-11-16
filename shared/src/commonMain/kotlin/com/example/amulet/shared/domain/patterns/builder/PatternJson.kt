package com.example.amulet.shared.domain.patterns.builder

import com.example.amulet.shared.domain.patterns.model.PatternSpec
import kotlinx.serialization.json.Json

object PatternJson {
    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = false
        }
    }

    fun encode(spec: PatternSpec): String = json.encodeToString(PatternSpec.serializer(), spec)
    fun decode(text: String): PatternSpec = json.decodeFromString(PatternSpec.serializer(), text)
}
