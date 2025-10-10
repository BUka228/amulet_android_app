package com.example.amulet.shared.domain.auth.repository

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.auth.model.UserCredentials
import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.domain.user.model.UserId

interface AuthRepository {
    suspend fun signUp(credentials: UserCredentials): AppResult<UserId>
    suspend fun signIn(credentials: UserCredentials): AppResult<UserId>
    suspend fun signInWithGoogle(idToken: String): AppResult<UserId>
    suspend fun signOut(): AppResult<Unit>
    suspend fun establishSession(user: User): AppResult<Unit>
}
