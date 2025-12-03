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

@Singleton
@OptIn(ExperimentalTime::class)
class UserDtoMapper @Inject constructor() {

    fun toDomain(dto: UserDto): User = User(
        id = UserId(dto.id),
        displayName = dto.displayName,
        avatarUrl = dto.avatarUrl,
        email = null,
        timezone = dto.timezone,
        language = dto.language,
        consents = dto.toConsents(),
        createdAt = dto.createdAt?.value?.let { Instant.fromEpochMilliseconds(it) },
        updatedAt = dto.updatedAt?.value?.let { Instant.fromEpochMilliseconds(it) }
    )

    fun toEntity(dto: UserDto): UserEntity = UserEntity(
        id = dto.id,
        displayName = dto.displayName,
        avatarUrl = dto.avatarUrl,
        timezone = dto.timezone,
        language = dto.language,
        consents = dto.toConsents(),
        createdAt = dto.createdAt?.value?.let { Instant.fromEpochMilliseconds(it) },
        updatedAt = dto.updatedAt?.value?.let { Instant.fromEpochMilliseconds(it) }
    )

    private fun UserDto.toConsents(): UserConsents? = consents?.toUserConsents()

    private fun Map<String, Any?>.toUserConsents(): UserConsents {
        val analytics = (this["analytics"] as? Boolean) ?: false
        val marketing = (this["marketing"] as? Boolean) ?: false
        val notifications = (this["notifications"] as? Boolean) ?: false
        val updatedAtString = this["updatedAt"] as? String
        val updatedAt = updatedAtString?.let { Instant.parse(it) }

        return UserConsents(
            analytics = analytics,
            marketing = marketing,
            notifications = notifications,
            updatedAt = updatedAt
        )
    }
}
