package com.example.amulet.data.auth.repository

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.auth.model.UserCredentials
import com.example.amulet.shared.domain.privacy.model.UserConsents
import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.core.auth.UserSessionUpdater
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest {

    private val sessionUpdater = RecordingUserSessionUpdater()
    private val repository = AuthRepositoryImpl(sessionUpdater)

    @Test
    fun `sign in returns email`() = runTest {
        val credentials = UserCredentials(email = "user@example.com", password = "pwd")

        val result = repository.signIn(credentials)

        assertEquals(Ok(credentials.email), result)
    }

    @Test
    fun `sign in returns default id when email blank`() = runTest {
        val credentials = UserCredentials(email = "", password = "pwd")

        val result = repository.signIn(credentials)

        assertEquals(Ok("session-user"), result)
    }

    @Test
    fun `sign out clears session`() = runTest {
        val result = repository.signOut()

        assertEquals(1, sessionUpdater.clearInvocations)
        assertEquals(Ok(Unit), result)
    }

    @Test
    fun `sign out propagates failure`() = runTest {
        sessionUpdater.throwOnClear = true

        val result = repository.signOut()

        assertEquals(1, sessionUpdater.clearInvocations)
        assertEquals(Err(AppError.Unknown), result)
    }

    @Test
    fun `establish session updates session`() = runTest {
        val user = User(id = "user", consents = UserConsents(analytics = true))

        val result = repository.establishSession(user)

        assertEquals(listOf(user), sessionUpdater.updatedUsers)
        assertEquals(Ok(Unit), result)
    }

    @Test
    fun `establish session propagates failure`() = runTest {
        sessionUpdater.throwOnUpdate = true
        val user = User(id = "user", consents = UserConsents())

        val result = repository.establishSession(user)

        assertEquals(Err(AppError.Unknown), result)
    }

    private class RecordingUserSessionUpdater : UserSessionUpdater {
        val updatedUsers = mutableListOf<User>()
        var clearInvocations: Int = 0
        var throwOnUpdate: Boolean = false
        var throwOnClear: Boolean = false

        override suspend fun updateSession(user: User) {
            if (throwOnUpdate) throw IllegalStateException("update failed")
            updatedUsers += user
        }

        override suspend fun clearSession() {
            clearInvocations += 1
            if (throwOnClear) throw IllegalStateException("clear failed")
        }
    }
}
