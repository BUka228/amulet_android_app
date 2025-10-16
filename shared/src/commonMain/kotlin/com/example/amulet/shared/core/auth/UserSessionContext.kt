package com.example.amulet.shared.core.auth

import com.example.amulet.shared.domain.privacy.model.UserConsents
import com.example.amulet.shared.domain.user.model.UserId

sealed interface UserSessionContext {

    data object Loading : UserSessionContext

    data object LoggedOut : UserSessionContext

    data class LoggedIn(
        val userId: UserId,
        val displayName: String?,
        val avatarUrl: String?,
        val timezone: String?,
        val language: String?,
        val consents: UserConsents
    ) : UserSessionContext
}
