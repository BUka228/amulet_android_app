package com.example.amulet.shared.domain.privacy.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class UserConsents(
    val analytics: Boolean = false,
    val marketing: Boolean = false,
    val notifications: Boolean = false,
    val updatedAt: Instant? = null
)
