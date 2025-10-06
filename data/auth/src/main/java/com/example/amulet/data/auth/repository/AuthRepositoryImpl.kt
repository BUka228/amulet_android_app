package com.example.amulet.data.auth.repository

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.auth.UserSessionUpdater
import com.example.amulet.shared.domain.auth.model.UserCredentials
import com.example.amulet.shared.domain.auth.repository.AuthRepository
import com.example.amulet.shared.domain.user.model.User
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userSessionUpdater: UserSessionUpdater
) : AuthRepository {

    override suspend fun signIn(credentials: UserCredentials): AppResult<String> =
        Ok(credentials.email.ifBlank { DEFAULT_USER_ID })

    override suspend fun signOut(): AppResult<Unit> = runCatching {
        userSessionUpdater.clearSession()
    }.fold(
        onSuccess = { Ok(Unit) },
        onFailure = { Err(AppError.Unknown) }
    )

    override suspend fun establishSession(user: User): AppResult<Unit> = runCatching {
        userSessionUpdater.updateSession(user)
    }.fold(
        onSuccess = { Ok(Unit) },
        onFailure = { Err(AppError.Unknown) }
    )

    private companion object {
        const val DEFAULT_USER_ID = "session-user"
    }
}
