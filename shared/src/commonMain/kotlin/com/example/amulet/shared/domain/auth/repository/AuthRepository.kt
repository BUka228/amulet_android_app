package com.example.amulet.shared.domain.auth.repository

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.auth.model.UserCredentials
import com.example.amulet.shared.domain.user.model.User

interface AuthRepository {
    suspend fun signIn(credentials: UserCredentials): AppResult<String>
    suspend fun signOut(): AppResult<Unit>
    suspend fun establishSession(user: User): AppResult<Unit>
}
