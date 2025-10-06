package com.example.amulet.shared.domain.privacy.model

data class UserConsents(
    val analytics: Boolean = false,
    val marketing: Boolean = false
)
