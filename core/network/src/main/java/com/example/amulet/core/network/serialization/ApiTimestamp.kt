package com.example.amulet.core.network.serialization

import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@JvmInline
@Serializable(with = TimestampAsEpochMillisSerializer::class)
value class ApiTimestamp(val value: Long)

object TimestampAsEpochMillisSerializer : KSerializer<ApiTimestamp> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ApiTimestamp", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): ApiTimestamp {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("TimestampAsEpochMillisSerializer supports only JSON")
        val element = jsonDecoder.decodeJsonElement()
        return when (element) {
            is JsonPrimitive -> ApiTimestamp(parseStringTimestamp(element))
            is JsonObject -> ApiTimestamp(parseObjectTimestamp(element))
            else -> error("Unsupported timestamp format: $element")
        }
    }

    override fun serialize(encoder: Encoder, value: ApiTimestamp) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("TimestampAsEpochMillisSerializer supports only JSON")
        val isoString = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(value.value))
        jsonEncoder.encodeString(isoString)
    }

    private fun parseStringTimestamp(primitive: JsonPrimitive): Long {
        val content = primitive.content
        return runCatching { Instant.parse(content).toEpochMilli() }
            .getOrElse { error("Invalid ISO timestamp: $content") }
    }

    private fun parseObjectTimestamp(obj: JsonObject): Long {
        val seconds = obj["_seconds"]?.jsonPrimitive?.content?.toLongOrNull()
        val nanos = obj["_nanoseconds"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
        if (seconds != null) {
            return seconds * 1_000 + nanos / 1_000_000
        }
        error("Invalid timestamp object: $obj")
    }
}
