package com.example.amulet.shared.domain.user.model

import com.example.amulet.shared.domain.privacy.model.UserConsents
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class User(
    val id: UserId,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val timezone: String? = null,
    val language: String? = null,
    val consents: UserConsents? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)
