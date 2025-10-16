package com.example.amulet.core.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.time.Instant
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

@OptIn(kotlin.time.ExperimentalTime::class)
class SupabaseAuthManagerTest {

    private lateinit var supabaseClient: SupabaseClient
    private lateinit var auth: Auth
    private lateinit var authManager: SupabaseAuthManager

    @Before
    fun setUp() {
        auth = mockk(relaxed = true)
        supabaseClient = mockk {
            every { auth } returns auth
        }
        authManager = SupabaseAuthManager(supabaseClient)
    }

    @Test
    fun `getCurrentSession returns current session`() {
        val session = createTestSession()
        every { auth.currentSessionOrNull() } returns session

        val result = authManager.getCurrentSession()

        assertEquals(session, result)
    }

    @Test
    fun `getCurrentSession returns null when no session`() {
        every { auth.currentSessionOrNull() } returns null

        val result = authManager.getCurrentSession()

        assertNull(result)
    }

    @Test
    fun `getAccessToken returns token from valid session`() = runTest {
        val session = createTestSession()
        every { auth.currentSessionOrNull() } returns session

        val token = authManager.getAccessToken()

        assertEquals("test_access_token", token)
    }

    @Test
    fun `getAccessToken returns null when no session`() = runTest {
        every { auth.currentSessionOrNull() } returns null

        val token = authManager.getAccessToken()

        assertNull(token)
    }

    @Test
    fun `getAuthorizationHeader returns formatted header`() = runTest {
        val session = createTestSession(
            accessToken = "abc123",
            tokenType = "bearer"
        )
        every { auth.currentSessionOrNull() } returns session

        val header = authManager.getAuthorizationHeader()

        assertEquals("Bearer abc123", header)
    }

    @Test
    fun `getAuthorizationHeader capitalizes token type`() = runTest {
        val session = createTestSession(
            accessToken = "abc123",
            tokenType = "bearer"
        )
        every { auth.currentSessionOrNull() } returns session

        val header = authManager.getAuthorizationHeader()

        assertTrue(header!!.startsWith("Bearer"))
    }

    @Test
    fun `getAuthorizationHeader returns null when no session`() = runTest {
        every { auth.currentSessionOrNull() } returns null

        val header = authManager.getAuthorizationHeader()

        assertNull(header)
    }

    @Test
    fun `refreshToken refreshes session successfully`() = runTest {
        val oldSession = createTestSession(refreshToken = "refresh_token")
        val newSession = createTestSession(accessToken = "new_token")
        every { auth.currentSessionOrNull() } returns oldSession
        coEvery { auth.refreshSession("refresh_token") } returns newSession

        val result = authManager.refreshToken()

        assertTrue(result.isSuccess)
        assertEquals(newSession, result.getOrNull())
        coVerify(exactly = 1) { auth.refreshSession("refresh_token") }
    }

    @Test
    fun `refreshToken fails when no session`() = runTest {
        every { auth.currentSessionOrNull() } returns null

        val result = authManager.refreshToken()

        assertTrue(result.isFailure)
    }

    @Test
    fun `refreshToken fails when no refresh token`() = runTest {
        val session = createTestSession(refreshToken = null)
        every { auth.currentSessionOrNull() } returns session

        val result = authManager.refreshToken()

        assertTrue(result.isFailure)
    }

    @Test
    fun `clearSession signs out`() = runTest {
        coEvery { auth.signOut() } returns Unit

        authManager.clearSession()

        coVerify(exactly = 1) { auth.signOut() }
    }

    @Test
    fun `getAccessToken refreshes when token expires soon`() = runTest {
        // Token expires in 30 seconds (less than 60 second threshold)
        val nowMillis = System.currentTimeMillis()
        val expiresAt = Instant.fromEpochMilliseconds(nowMillis + 30_000)
        val oldSession = createTestSession(
            accessToken = "old_token",
            refreshToken = "refresh_token",
            expiresAt = expiresAt,
            expiresIn = 30L
        )
        val newSession = createTestSession(accessToken = "new_token")
        
        every { auth.currentSessionOrNull() } returns oldSession
        coEvery { auth.refreshSession("refresh_token") } returns newSession

        val token = authManager.getAccessToken()

        assertEquals("new_token", token)
        coVerify(exactly = 1) { auth.refreshSession("refresh_token") }
    }

    private fun createTestSession(
        accessToken: String = "test_access_token",
        refreshToken: String? = "test_refresh_token",
        tokenType: String = "bearer",
        expiresAt: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis() + 3_600_000),
        expiresIn: Long = 3600L
    ): UserSession {
        return mockk {
            every { accessToken } returns accessToken
            every { refreshToken } returns refreshToken
            every { tokenType } returns tokenType
            every { expiresAt } returns expiresAt
            every { expiresIn } returns expiresIn
            every { user } returns UserInfo(id = "user123", aud = "authenticated", email = "test@example.com")
        }
    }
}
