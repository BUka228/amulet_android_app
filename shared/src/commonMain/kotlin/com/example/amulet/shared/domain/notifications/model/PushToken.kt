package com.example.amulet.shared.domain.notifications.model

import kotlinx.datetime.Instant

data class PushToken(
    val id: String,
    val token: String,
    val platform: String?,
    val createdAt: Instant?,
    val lastSeenAt: Instant?
)

data class PushTokenRegistration(
    val token: String,
    val platform: String?,
    val lastSeenAt: Instant?
)
