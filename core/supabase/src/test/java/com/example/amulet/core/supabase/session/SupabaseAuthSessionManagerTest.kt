package com.example.amulet.core.supabase.session

import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.time.Instant
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.hours

@OptIn(kotlin.time.ExperimentalTime::class)
class SupabaseAuthSessionManagerTest {

    private lateinit var storage: SupabaseSessionStorage
    private lateinit var sessionManager: SupabaseAuthSessionManager

    @Before
    fun setUp() {
        storage = mockk(relaxed = true)
        sessionManager = SupabaseAuthSessionManager(storage)
    }

    @Test
    fun `loadSession returns session from storage`() = runTest {
        val expectedSession = createTestSession()
        coEvery { storage.loadSession() } returns expectedSession

        val result = sessionManager.loadSession()

        assertNotNull(result)
        assertEquals(expectedSession, result)
        coVerify(exactly = 1) { storage.loadSession() }
    }

    @Test
    fun `loadSession returns null when storage is empty`() = runTest {
        coEvery { storage.loadSession() } returns null

        val result = sessionManager.loadSession()

        assertNull(result)
        coVerify(exactly = 1) { storage.loadSession() }
    }

    @Test
    fun `saveSession delegates to storage`() = runTest {
        val session = createTestSession()

        sessionManager.saveSession(session)

        coVerify(exactly = 1) { storage.saveSession(session) }
    }

    @Test
    fun `deleteSession clears storage`() = runTest {
        sessionManager.deleteSession()

        coVerify(exactly = 1) { storage.clearSession() }
    }

    private fun createTestSession(): UserSession {
        val expiresAt = Instant.fromEpochMilliseconds(System.currentTimeMillis() + 3_600_000)
        return UserSession(
            accessToken = "test_token",
            refreshToken = "test_refresh",
            expiresAt = expiresAt,
            expiresIn = 3600L,
            tokenType = "bearer",
            user = UserInfo(id = "user123", aud = "authenticated", email = "test@example.com")
        )
    }
}
