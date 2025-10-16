package com.example.amulet.core.supabase.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.time.Duration.Companion.hours

@OptIn(kotlin.time.ExperimentalTime::class)
class SupabaseSessionStorageTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var storage: SupabaseSessionStorage

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope
        ) {
            tmpFolder.newFile("test_session.preferences_pb")
        }
        storage = SupabaseSessionStorage(dataStore)
    }

    @After
    fun tearDown() {
        tmpFolder.delete()
    }

    @Test
    fun `saveSession stores session correctly`() = testScope.runTest {
        val session = createTestSession()

        storage.saveSession(session)

        val loaded = storage.loadSession()
        assertNotNull(loaded)
        assertEquals(session.accessToken, loaded?.accessToken)
        assertEquals(session.refreshToken, loaded?.refreshToken)
        assertEquals(session.expiresIn, loaded?.expiresIn)
        assertEquals(session.tokenType, loaded?.tokenType)
        assertEquals(session.user?.id, loaded?.user?.id)
        assertEquals(session.user?.email, loaded?.user?.email)
    }

    @Test
    fun `loadSession returns null when no session stored`() = testScope.runTest {
        val loaded = storage.loadSession()
        
        assertNull(loaded)
    }

    @Test
    fun `clearSession removes all data`() = testScope.runTest {
        val session = createTestSession()
        storage.saveSession(session)

        storage.clearSession()

        val loaded = storage.loadSession()
        assertNull(loaded)
    }

    @Test
    fun `observeSession emits null initially`() = testScope.runTest {
        val observed = storage.observeSession().first()
        
        assertNull(observed)
    }

    @Test
    fun `observeSession emits session after save`() = testScope.runTest {
        val session = createTestSession()
        
        storage.saveSession(session)
        testScheduler.advanceUntilIdle()
        
        val observed = storage.observeSession().first()
        assertNotNull(observed)
        assertEquals(session.accessToken, observed?.accessToken)
    }

    @Test
    fun `saveSession handles null refresh token`() = testScope.runTest {
        val session = createTestSession(refreshToken = null)

        storage.saveSession(session)

        val loaded = storage.loadSession()
        assertNotNull(loaded)
        assertNull(loaded?.refreshToken)
    }

    @Test
    fun `saveSession handles null user`() = testScope.runTest {
        val session = createTestSession(userInfo = null)

        storage.saveSession(session)

        val loaded = storage.loadSession()
        assertNotNull(loaded)
        assertNull(loaded?.user)
    }

    @Test
    fun `saveSession overwrites existing session`() = testScope.runTest {
        val firstSession = createTestSession(accessToken = "token1")
        val secondSession = createTestSession(accessToken = "token2")

        storage.saveSession(firstSession)
        storage.saveSession(secondSession)

        val loaded = storage.loadSession()
        assertEquals("token2", loaded?.accessToken)
    }

    private fun createTestSession(
        accessToken: String = "test_access_token",
        refreshToken: String? = "test_refresh_token",
        tokenType: String = "bearer",
        userInfo: UserInfo? = UserInfo(id = "user123", aud = "authenticated", email = "test@example.com")
    ): UserSession {
        val expiresAt = Instant.fromEpochMilliseconds(System.currentTimeMillis() + 3_600_000)
        return UserSession(
            accessToken = accessToken,
            refreshToken = refreshToken ?: "",
            expiresAt = expiresAt,
            expiresIn = 3600L,
            tokenType = tokenType,
            user = userInfo
        )
    }
}
