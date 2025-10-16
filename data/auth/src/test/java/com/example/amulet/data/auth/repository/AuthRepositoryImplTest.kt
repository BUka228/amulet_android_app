package com.example.amulet.data.auth.repository

import com.example.amulet.data.auth.datasource.local.AuthLocalDataSource
import com.example.amulet.data.auth.datasource.remote.AuthRemoteDataSource
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.auth.UserSessionUpdater
import com.example.amulet.shared.domain.auth.model.UserCredentials
import com.example.amulet.shared.domain.auth.model.AuthSession
import com.example.amulet.shared.domain.auth.model.AuthTokens
import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.domain.user.model.UserId
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    @MockK
    private lateinit var remoteDataSource: AuthRemoteDataSource

    @MockK(relaxed = true)
    private lateinit var localDataSource: AuthLocalDataSource

    @MockK(relaxed = true)
    private lateinit var sessionUpdater: UserSessionUpdater

    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = AuthRepositoryImpl(remoteDataSource, localDataSource, sessionUpdater)
    }

    @Test
    fun `signUp delegates to remote`() = runTest {
        val credentials = UserCredentials(email = "user@example.com", password = "pwd")
        val session = AuthSession(
            userId = UserId("user-id"),
            tokens = AuthTokens("access", "refresh", 123L, "bearer")
        )
        coEvery { remoteDataSource.signUp(credentials) } returns com.github.michaelbull.result.Ok(session)

        val result = repository.signUp(credentials)

        assertEquals(session, result.component1())
        assertNull(result.component2())
        coVerify(exactly = 1) { remoteDataSource.signUp(credentials) }
        confirmVerified(remoteDataSource)
    }

    @Test
    fun `signIn delegates to remote`() = runTest {
        val credentials = UserCredentials(email = "user@example.com", password = "pwd")
        val session = AuthSession(
            userId = UserId("user-id"),
            tokens = AuthTokens("access", "refresh", 456L, "bearer")
        )
        coEvery { remoteDataSource.signIn(credentials) } returns com.github.michaelbull.result.Ok(session)

        val result = repository.signIn(credentials)

        assertEquals(session, result.component1())
        assertNull(result.component2())
        coVerify(exactly = 1) { remoteDataSource.signIn(credentials) }
        confirmVerified(remoteDataSource)
    }

    @Test
    fun `signInWithGoogle delegates to remote`() = runTest {
        val session = AuthSession(
            userId = UserId("user-id"),
            tokens = AuthTokens("access", "refresh", 789L, "bearer")
        )
        coEvery { remoteDataSource.signInWithGoogle("token") } returns com.github.michaelbull.result.Ok(session)

        val result = repository.signInWithGoogle("token")

        assertEquals(session, result.component1())
        assertNull(result.component2())
        coVerify(exactly = 1) { remoteDataSource.signInWithGoogle("token") }
        confirmVerified(remoteDataSource)
    }

    @Test
    fun `signOut clears session and local storage when remote succeeds`() = runTest {
        coEvery { remoteDataSource.signOut() } returns com.github.michaelbull.result.Ok(Unit)
        coJustRun { sessionUpdater.clearSession() }
        coJustRun { localDataSource.clearAll() }

        val result = repository.signOut()

        assertNotNull(result.component1())
        assertNull(result.component2())
        coVerify(exactly = 1) { remoteDataSource.signOut() }
        coVerify(exactly = 1) { sessionUpdater.clearSession() }
        coVerify(exactly = 1) { localDataSource.clearAll() }
    }

    @Test
    fun `signOut propagates remote failure without touching local state`() = runTest {
        coEvery { remoteDataSource.signOut() } returns com.github.michaelbull.result.Err(AppError.Network)

        val result = repository.signOut()

        assertNull(result.component1())
        assertEquals(AppError.Network, result.component2())
        coVerify(exactly = 1) { remoteDataSource.signOut() }
        coVerify(exactly = 0) { sessionUpdater.clearSession() }
        coVerify(exactly = 0) { localDataSource.clearAll() }
    }

    @Test
    fun `establishSession updates session`() = runTest {
        val user = User(id = UserId("user"))
        coJustRun { sessionUpdater.updateSession(user) }

        val result = repository.establishSession(user)

        assertNotNull(result.component1())
        assertNull(result.component2())
        coVerify(exactly = 1) { sessionUpdater.updateSession(user) }
    }

    @Test
    fun `establishSession returns unknown on failure`() = runTest {
        val user = User(id = UserId("user"))
        coEvery { sessionUpdater.updateSession(user) } throws IllegalStateException("failure")

        val result = repository.establishSession(user)

        assertNull(result.component1())
        assertEquals(AppError.Unknown, result.component2())
    }
}
