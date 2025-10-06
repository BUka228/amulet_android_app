package com.example.amulet.core.auth.session.impl

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.example.amulet.core.auth.datastore.UserSessionPreferencesSerializer
import com.example.amulet.core.auth.session.proto.UserSessionPreferences
import com.example.amulet.shared.core.auth.UserSessionContext
import com.example.amulet.shared.domain.privacy.model.UserConsents
import com.example.amulet.shared.domain.user.model.User
import java.io.File
import kotlin.io.path.createTempDirectory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class UserSessionManagerImplTest {

    private val resources = mutableListOf<SessionResource>()

    @After
    fun tearDown() {
        resources.forEach(SessionResource::close)
        resources.clear()
    }

    @Test
    fun `выдает LoggedOut если сессия не сохранена`() = runTest {
        val resource = createResource()
        val state = resource.manager.sessionContext.first { it !is UserSessionContext.Loading }

        assertTrue(state is UserSessionContext.LoggedOut)
    }

    @Test
    fun `обновление сессии выдает LoggedIn состояние`() = runTest {
        val resource = createResource()
        val user = User(
            id = "user-123",
            displayName = "Sample",
            avatarUrl = "https://example.com/avatar.png",
            consents = UserConsents(analytics = true, marketing = false)
        )

        resource.manager.updateSession(user)
        val state = resource.manager.sessionContext.first { it is UserSessionContext.LoggedIn }

        val loggedIn = state as UserSessionContext.LoggedIn
        assertEquals(user.id, loggedIn.userId)
        assertEquals(user.displayName, loggedIn.displayName)
        assertEquals(user.avatarUrl, loggedIn.avatarUrl)
        assertEquals(user.consents, loggedIn.consents)
    }

    @Test
    fun `очистка сессии возвращает LoggedOut`() = runTest {
        val resource = createResource()
        val user = User(id = "user", consents = UserConsents())

        resource.manager.updateSession(user)
        resource.manager.sessionContext.first { it is UserSessionContext.LoggedIn }

        resource.manager.clearSession()
        val state = resource.manager.sessionContext.first { it is UserSessionContext.LoggedOut }

        assertTrue(state is UserSessionContext.LoggedOut)
    }

    @Test
    fun `сессия сохраняется в datastore`() = runTest {
        val resource = createResource()
        val user = User(id = "persisted", displayName = "First", consents = UserConsents(analytics = true))

        resource.manager.updateSession(user)
        resource.manager.sessionContext.first { it is UserSessionContext.LoggedIn }

        val stored = resource.dataStore.data.first()
        assertEquals(user.id, stored.userId)
        assertEquals(user.displayName, stored.displayName)
    }

    private fun TestScope.createManager(storeFile: File): SessionResource {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        val dataStore: DataStore<UserSessionPreferences> = DataStoreFactory.create(
            serializer = UserSessionPreferencesSerializer,
            scope = scope
        ) {
            storeFile
        }
        val resource = SessionResource(UserSessionManagerImpl(dataStore), dataStore, scope, storeFile)
        resources += resource
        return resource
    }

    private fun TestScope.createResource(file: File = newStoreFile()): SessionResource =
        createManager(file)

    private fun newStoreFile(): File {
        val directory = createTempDirectory(prefix = "session-manager").toFile()
        directory.deleteOnExit()
        return File(directory, "session.pb").apply { deleteOnExit() }
    }

    private data class SessionResource(
        val manager: UserSessionManagerImpl,
        val dataStore: DataStore<UserSessionPreferences>,
        val scope: TestScope,
        val file: File
    ) {
        fun close() {
            scope.cancel()
            file.parentFile?.deleteRecursively()
        }
    }
}
