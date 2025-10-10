package com.example.amulet.shared.domain.privacy.model

data class UserConsents(
    val analytics: Boolean = false,
    val usage: Boolean = false,
    val crash: Boolean = false,
    val diagnostics: Boolean = false
)
