package com.example.amulet_android_app.presentation.session

import com.example.amulet.core.auth.session.UserSessionManager
import com.example.amulet.shared.core.auth.UserSessionContext
import com.example.amulet.shared.domain.privacy.model.UserConsents
import com.example.amulet.shared.domain.user.model.UserId
import com.example.amulet_android_app.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Rule
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class SessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `emits LoggedOut when session manager provides logged out`() = runTest {
        val manager = FakeUserSessionManager(UserSessionContext.LoggedOut)
        val viewModel = SessionViewModel(manager)

        assertEquals(AuthState.LoggedOut, viewModel.state.value)
    }

    @Test
    fun `emits LoggedIn when session manager provides user`() = runTest {
        val context = UserSessionContext.LoggedIn(
            userId = UserId("id"),
            displayName = null,
            avatarUrl = null,
            timezone = null,
            language = null,
            consents = UserConsents()
        )
        val manager = FakeUserSessionManager(context)
        val viewModel = SessionViewModel(manager)

        assertEquals(AuthState.LoggedIn, viewModel.state.value)
    }
}

@OptIn(ExperimentalTime::class)
private class FakeUserSessionManager(initial: UserSessionContext) : UserSessionManager {
    private val flow = MutableStateFlow(initial)

    override val sessionContext: StateFlow<UserSessionContext> = flow

    override val currentContext: UserSessionContext
        get() = flow.value

    override suspend fun updateSession(user: com.example.amulet.shared.domain.user.model.User) {
        flow.value = UserSessionContext.LoggedIn(
            userId = user.id,
            displayName = user.displayName,
            avatarUrl = user.avatarUrl,
            timezone = user.timezone,
            language = user.language,
            consents = user.consents ?: UserConsents()
        )
    }

    override suspend fun clearSession() {
        flow.value = UserSessionContext.LoggedOut
    }
}
