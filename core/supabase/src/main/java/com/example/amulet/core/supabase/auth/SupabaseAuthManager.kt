package com.example.amulet.core.supabase.auth

import com.example.amulet.shared.core.logging.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Менеджер Supabase-аутентификации.
 * Централизованное управление сессией и токенами.
 */
@Singleton
@OptIn(kotlin.time.ExperimentalTime::class)
class SupabaseAuthManager @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    
    private val auth get() = supabaseClient.auth
    private val refreshMutex = Mutex()
    
    /**
     * Получить текущую активную сессию или null.
     */
    fun getCurrentSession(): UserSession? = auth.currentSessionOrNull()
    
    /**
     * Получить access token с автоматическим обновлением при необходимости.
     */
    suspend fun getAccessToken(): String? = refreshMutex.withLock {
        val session = auth.currentSessionOrNull() ?: return@withLock null
        
        if (shouldRefreshSession(session)) {
            refreshSessionIfPossible(session)?.accessToken
        } else {
            session.accessToken
        }
    }
    
    /**
     * Получить полный Authorization заголовок (например, "Bearer xxx").
     */
    suspend fun getAuthorizationHeader(): String? {
        val token = getAccessToken() ?: return null
        val session = auth.currentSessionOrNull() ?: return null
        val tokenType = session.tokenType.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        }
        return "$tokenType $token"
    }
    
    /**
     * Принудительное обновление токена.
     */
    suspend fun refreshToken(): Result<UserSession> = runCatching {
        refreshMutex.withLock {
            val session = auth.currentSessionOrNull()
                ?: error("No active session to refresh")
            val refreshToken = session.refreshToken
                ?: error("No refresh token available")
            
            Logger.d("Manually refreshing Supabase session", TAG)
            auth.refreshSession(refreshToken)
        }
    }
    
    /**
     * Очистить сессию (logout).
     */
    suspend fun clearSession() {
        Logger.d("Clearing Supabase session", TAG)
        auth.signOut()
    }
    
    private suspend fun refreshSessionIfPossible(session: UserSession): UserSession? {
        val refreshToken = session.refreshToken
        if (refreshToken.isNullOrBlank()) {
            Logger.w("Cannot refresh session: no refresh token", tag = TAG)
            return session
        }
        
        return runCatching {
            Logger.d("Auto-refreshing Supabase session", TAG)
            auth.refreshSession(refreshToken)
        }.onFailure { throwable ->
            Logger.w("Session refresh failed", throwable, TAG)
        }.getOrNull() ?: session
    }
    
    private fun shouldRefreshSession(session: UserSession): Boolean {
        val expiresAtMillis = session.expiresAt.toEpochMilliseconds()
        val nowMillis = System.currentTimeMillis()
        val remainingMillis = expiresAtMillis - nowMillis

        // Обновляем за 60 секунд до истечения
        return remainingMillis <= REFRESH_THRESHOLD.inWholeMilliseconds
    }
    
    private companion object {
        private const val TAG = "SupabaseAuthManager"
        private val REFRESH_THRESHOLD: Duration = 60.seconds
    }
}
