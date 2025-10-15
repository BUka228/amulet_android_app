package com.example.amulet.shared.core.auth

import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.domain.auth.model.AuthTokens

interface UserSessionUpdater {
    suspend fun updateSession(user: User)
    suspend fun updateTokens(tokens: AuthTokens)
    suspend fun clearSession()
}
