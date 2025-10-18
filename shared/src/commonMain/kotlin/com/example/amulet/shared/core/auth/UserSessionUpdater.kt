package com.example.amulet.shared.core.auth

import com.example.amulet.shared.domain.user.model.User

interface UserSessionUpdater {
    suspend fun updateSession(user: User)
    suspend fun clearSession()
    suspend fun enableGuestMode(displayName: String? = null, language: String? = null)
}
