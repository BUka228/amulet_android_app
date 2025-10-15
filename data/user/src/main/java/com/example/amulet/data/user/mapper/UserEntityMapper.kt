package com.example.amulet.data.user.mapper

import com.example.amulet.core.database.entity.UserEntity
import com.example.amulet.shared.domain.privacy.model.UserConsents
import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.domain.user.model.UserId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime

@Singleton
@OptIn(ExperimentalTime::class)
class UserEntityMapper @Inject constructor(
    private val json: Json
) {

    fun toDomain(entity: UserEntity): User = User(
        id = UserId(entity.id),
        displayName = entity.displayName,
        avatarUrl = entity.avatarUrl,
        timezone = entity.timezone,
        language = entity.language,
        consents = entity.consentsJson.toConsents(),
        createdAt = entity.createdAt?.let { Instant.fromEpochMilliseconds(it) },
        updatedAt = entity.updatedAt?.let { Instant.fromEpochMilliseconds(it) }
    )

    private fun String.toConsents(): UserConsents? = runCatching {
        val map = json.decodeFromString<Map<String, Boolean>>(this)
        UserConsents(
            analytics = map["analytics"] ?: false,
            usage = map["usage"] ?: false,
            crash = map["crash"] ?: false,
            diagnostics = map["diagnostics"] ?: false
        )
    }.getOrNull()
}
