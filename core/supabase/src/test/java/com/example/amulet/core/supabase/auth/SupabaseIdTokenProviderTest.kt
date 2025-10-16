package com.example.amulet.core.supabase.auth

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SupabaseIdTokenProviderTest {

    private lateinit var authManager: SupabaseAuthManager
    private lateinit var idTokenProvider: SupabaseIdTokenProvider

    @Before
    fun setUp() {
        authManager = mockk()
        idTokenProvider = SupabaseIdTokenProvider(authManager)
    }

    @Test
    fun `getIdToken returns authorization header from auth manager`() = runTest {
        coEvery { authManager.getAuthorizationHeader() } returns "Bearer test_token"

        val token = idTokenProvider.getIdToken()

        assertEquals("Bearer test_token", token)
        coVerify(exactly = 1) { authManager.getAuthorizationHeader() }
    }

    @Test
    fun `getIdToken returns null when auth manager returns null`() = runTest {
        coEvery { authManager.getAuthorizationHeader() } returns null

        val token = idTokenProvider.getIdToken()

        assertNull(token)
        coVerify(exactly = 1) { authManager.getAuthorizationHeader() }
    }
}
