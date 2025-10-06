package com.example.amulet.shared.domain.user.model

import com.example.amulet.shared.domain.privacy.model.UserConsents

data class User(
    val id: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val consents: UserConsents = UserConsents()
)
