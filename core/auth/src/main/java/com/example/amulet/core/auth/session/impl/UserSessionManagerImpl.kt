package com.example.amulet.core.auth.session.impl

import androidx.datastore.core.DataStore
import com.example.amulet.core.auth.session.UserSessionManager
import com.example.amulet.core.auth.session.proto.UserConsentsProto
import com.example.amulet.core.auth.session.proto.UserSessionPreferences
import com.example.amulet.shared.core.auth.UserSessionContext
import com.example.amulet.shared.core.auth.UserSessionContext.LoggedIn
import com.example.amulet.shared.domain.auth.model.AuthTokens
import com.example.amulet.shared.domain.privacy.model.UserConsents
import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.domain.user.model.UserId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Singleton
class UserSessionManagerImpl @Inject constructor(
    private val dataStore: DataStore<UserSessionPreferences>
) : UserSessionManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val internalState: StateFlow<UserSessionContext> = dataStore.data
        .catch { emit(UserSessionPreferences.getDefaultInstance()) }
        .map { preferences -> preferences.toSessionContext() }
        .stateIn(scope, SharingStarted.Eagerly, UserSessionContext.Loading)

    override val sessionContext: StateFlow<UserSessionContext> = internalState

    override val currentContext: UserSessionContext
        get() = internalState.value

    override suspend fun updateSession(user: User) {
        dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setUserId(user.id.value)
                .setDisplayName(user.displayName.orEmpty())
                .setAvatarUrl(user.avatarUrl.orEmpty())
                .setTimezone(user.timezone.orEmpty())
                .setLanguage(user.language.orEmpty())
                .setConsents((user.consents ?: UserConsents()).toProto())
                .build()
        }
    }

    override suspend fun updateTokens(tokens: AuthTokens) {
        dataStore.updateData { preferences ->
            preferences.toBuilder().apply {
                accessToken = tokens.accessToken
                if (tokens.refreshToken != null) {
                    refreshToken = tokens.refreshToken
                } else {
                    clearRefreshToken()
                }
                tokens.expiresAtEpochSeconds?.let {
                    accessTokenExpiresAt = it
                } ?: clearAccessTokenExpiresAt()
                if (tokens.tokenType != null) {
                    tokenType = tokens.tokenType
                } else {
                    clearTokenType()
                }
            }.build()
        }
    }

    override suspend fun clearSession() {
        dataStore.updateData { UserSessionPreferences.getDefaultInstance() }
    }

    private fun UserSessionPreferences.toSessionContext(): UserSessionContext {
        if (userId.isNullOrBlank()) {
            return UserSessionContext.LoggedOut
        }
        val consentsModel = if (hasConsents()) {
            consents.toModel()
        } else {
            UserConsents()
        }
        return LoggedIn(
            userId = UserId(userId),
            displayName = displayName.takeIf { it.isNotBlank() },
            avatarUrl = avatarUrl.takeIf { it.isNotBlank() },
            timezone = timezone.takeIf { it.isNotBlank() },
            language = language.takeIf { it.isNotBlank() },
            consents = consentsModel,
            tokens = tokensOrNull()
        )
    }

    private fun UserSessionPreferences.tokensOrNull(): AuthTokens? {
        if (accessToken.isNullOrBlank()) return null
        val refresh = refreshToken.takeIf { it.isNotBlank() }
        val expiresAt = accessTokenExpiresAt.takeIf { it != 0L }
        val type = tokenType.takeIf { it.isNotBlank() }
        return AuthTokens(
            accessToken = accessToken,
            refreshToken = refresh,
            expiresAtEpochSeconds = expiresAt,
            tokenType = type
        )
    }

    private fun UserConsents.toProto(): UserConsentsProto =
        UserConsentsProto.newBuilder()
            .setAnalytics(analytics)
            .setUsage(usage)
            .setCrash(crash)
            .setDiagnostics(diagnostics)
            .build()

    private fun UserConsentsProto.toModel(): UserConsents =
        UserConsents(
            analytics = analytics,
            usage = usage,
            crash = crash,
            diagnostics = diagnostics
        )
}
