package com.example.amulet.core.auth.session.impl

import androidx.datastore.core.DataStore
import com.example.amulet.core.auth.session.UserSessionManager
import com.example.amulet.core.auth.session.proto.UserConsentsProto
import com.example.amulet.core.auth.session.proto.UserSessionPreferences
import com.example.amulet.shared.core.auth.UserSessionContext
import com.example.amulet.shared.core.auth.UserSessionContext.LoggedIn
import com.example.amulet.shared.domain.privacy.model.UserConsents
import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.domain.user.model.UserId
import java.util.UUID
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
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Singleton
@OptIn(ExperimentalTime::class)
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

    override suspend fun clearSession() {
        dataStore.updateData { UserSessionPreferences.getDefaultInstance() }
    }

    override suspend fun enableGuestMode(displayName: String?, language: String?) {
        val guestSessionId = UUID.randomUUID().toString()
        dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setIsGuest(true)
                .setGuestSessionId(guestSessionId)
                .setDisplayName(displayName ?: "Гость")
                .setLanguage(language.orEmpty())
                .clearUserId()
                .clearAvatarUrl()
                .clearTimezone()
                .clearConsents()
                .build()
        }
    }

    private fun UserSessionPreferences.toSessionContext(): UserSessionContext {
        // Гостевой режим
        if (isGuest && guestSessionId.isNotBlank()) {
            return UserSessionContext.Guest(
                sessionId = guestSessionId,
                displayName = displayName.takeIf { it.isNotBlank() },
                language = language.takeIf { it.isNotBlank() }
            )
        }
        
        // Обычная авторизованная сессия
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
            consents = consentsModel
        )
    }

    private fun UserConsents.toProto(): UserConsentsProto =
        UserConsentsProto.newBuilder()
            .setAnalytics(analytics)
            .setMarketing(marketing)
            .setNotifications(notifications)
            .setUpdatedAt(updatedAt?.toString() ?: "")
            .build()

    private fun UserConsentsProto.toModel(): UserConsents {
        val parsedInstant = if (updatedAt.isNotBlank()) {
            try {
                // Пытаемся парсить как ISO-8601 (новый формат)
                Instant.parse(updatedAt)
            } catch (e: Exception) {
                try {
                    // Fallback: пытаемся парсить как epochSeconds (старый формат)
                    Instant.fromEpochSeconds(updatedAt.toLong())
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            null
        }
        
        return UserConsents(
            analytics = analytics,
            marketing = marketing,
            notifications = notifications,
            updatedAt = parsedInstant
        )
    }
}
