package com.example.amulet.data.user.mapper

import com.example.amulet.core.database.entity.UserEntity
import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.domain.user.model.UserId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

@Singleton
@OptIn(ExperimentalTime::class)
class UserEntityMapper @Inject constructor() {

    fun toDomain(entity: UserEntity): User = User(
        id = UserId(entity.id),
        displayName = entity.displayName,
        avatarUrl = entity.avatarUrl,
        timezone = entity.timezone,
        language = entity.language,
        consents = entity.consents,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )
}
