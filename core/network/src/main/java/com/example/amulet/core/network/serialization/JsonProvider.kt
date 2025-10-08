package com.example.amulet.core.network.serialization

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

object JsonProvider {

    fun create(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        explicitNulls = false
        classDiscriminator = "type"
        serializersModule = SerializersModule {}
    }
}
