package com.example.amulet.data.user.mapper

import com.example.amulet.core.database.entity.UserEntity
import com.example.amulet.core.network.dto.user.UserDto
import com.example.amulet.shared.domain.privacy.model.UserConsents
import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.domain.user.model.UserId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.ExperimentalTime

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

    private fun JsonObject.toUserConsents(): UserConsents = UserConsents(
        analytics = this["analytics"]?.jsonPrimitive?.booleanOrNull ?: false,
        usage = this["usage"]?.jsonPrimitive?.booleanOrNull ?: false,
        crash = this["crash"]?.jsonPrimitive?.booleanOrNull ?: false,
        diagnostics = this["diagnostics"]?.jsonPrimitive?.booleanOrNull ?: false
    )

    private fun UserConsents?.toJsonString(): String =
        json.encodeToString(
            mapOf(
                "analytics" to (this?.analytics ?: false),
                "usage" to (this?.usage ?: false),
                "crash" to (this?.crash ?: false),
                "diagnostics" to (this?.diagnostics ?: false)
            )
        )
}
