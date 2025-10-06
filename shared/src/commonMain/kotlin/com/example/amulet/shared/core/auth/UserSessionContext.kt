package com.example.amulet.shared.core.auth

import com.example.amulet.shared.domain.privacy.model.UserConsents

sealed interface UserSessionContext {

    data object Loading : UserSessionContext

    data object LoggedOut : UserSessionContext

    data class LoggedIn(
        val userId: String,
        val displayName: String?,
        val avatarUrl: String?,
        val consents: UserConsents
    ) : UserSessionContext
}
