package com.example.amulet.core.database.converter

import androidx.room.TypeConverter
import com.example.amulet.shared.domain.privacy.model.UserConsents
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

internal object UserConverters {

    private val json: Json = Json

    @OptIn(ExperimentalTime::class)
    @TypeConverter
    @JvmStatic
    fun fromUserConsents(value: UserConsents?): String {
        val jsonObject = buildJsonObject {
            put("analytics", value?.analytics ?: false)
            put("marketing", value?.marketing ?: false)
            put("notifications", value?.notifications ?: false)
            value?.updatedAt?.toString()?.let { put("updatedAt", it) }
        }
        return json.encodeToString(JsonObject.serializer(), jsonObject)
    }

    @OptIn(ExperimentalTime::class)
    @TypeConverter
    @JvmStatic
    fun toUserConsents(value: String?): UserConsents? {
        if (value == null) return null

        return runCatching {
            val jsonObject = json.decodeFromString(JsonObject.serializer(), value)

            val analytics = jsonObject["analytics"]?.jsonPrimitive?.booleanOrNull ?: false
            val marketing = jsonObject["marketing"]?.jsonPrimitive?.booleanOrNull ?: false
            val notifications = jsonObject["notifications"]?.jsonPrimitive?.booleanOrNull ?: false
            val updatedAt = jsonObject["updatedAt"]?.jsonPrimitive?.content?.let { Instant.parse(it) }

            UserConsents(
                analytics = analytics,
                marketing = marketing,
                notifications = notifications,
                updatedAt = updatedAt
            )
        }.getOrNull()
    }
}
