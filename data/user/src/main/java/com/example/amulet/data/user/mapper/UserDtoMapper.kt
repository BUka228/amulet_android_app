package com.example.amulet.data.user.mapper

import com.example.amulet.core.database.entity.UserEntity
import com.example.amulet.core.network.dto.user.UserDto
import com.example.amulet.shared.domain.privacy.model.UserConsents
import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.domain.user.model.UserId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

@Singleton
@OptIn(ExperimentalTime::class)
class UserDtoMapper @Inject constructor(
    private val json: Json
) {

    fun toDomain(dto: UserDto): User = User(
        id = UserId(dto.id),
        displayName = dto.displayName,
        avatarUrl = dto.avatarUrl,
        timezone = dto.timezone,
        language = dto.language,
        consents = dto.toConsents(),
        createdAt = dto.createdAt?.value?.let { Instant.fromEpochMilliseconds(it) },
        updatedAt = dto.updatedAt?.value?.let { Instant.fromEpochMilliseconds(it) }
    )

    fun toEntity(dto: UserDto): UserEntity {
        val consents = dto.toConsents()
        return UserEntity(
            id = dto.id,
            displayName = dto.displayName,
            avatarUrl = dto.avatarUrl,
            timezone = dto.timezone,
            language = dto.language,
            consentsJson = consents.toJsonString(),
            createdAt = dto.createdAt?.value,
            updatedAt = dto.updatedAt?.value
        )
    }

    private fun UserDto.toConsents(): UserConsents? =
        consents?.toUserConsents()

    private fun JsonObject.toUserConsents(): UserConsents {
        val analytics = this["analytics"]?.jsonPrimitive?.booleanOrNull ?: false
        val marketing = this["marketing"]?.jsonPrimitive?.booleanOrNull ?: false
        val notifications = this["notifications"]?.jsonPrimitive?.booleanOrNull ?: false
        val updatedAt: Instant? = this["updatedAt"]?.jsonPrimitive?.content?.let { 
            Instant.parse(it)
        }
        
        return UserConsents(
            analytics = analytics,
            marketing = marketing,
            notifications = notifications,
            updatedAt = updatedAt
        )
    }

    private fun UserConsents?.toJsonString(): String =
        json.encodeToString(
            mapOf(
                "analytics" to (this?.analytics ?: false),
                "marketing" to (this?.marketing ?: false),
                "notifications" to (this?.notifications ?: false),
                "updatedAt" to (this?.updatedAt?.toString())
            )
        )
}
