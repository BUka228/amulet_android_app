package com.example.amulet.core.auth.token

import com.example.amulet.core.auth.session.UserSessionManager
import com.example.amulet.core.network.auth.IdTokenProvider
import com.example.amulet.shared.core.auth.UserSessionContext
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.auth.model.AuthTokens
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class SupabaseAccessTokenProvider @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val userSessionManager: UserSessionManager
) : IdTokenProvider {

    private val mutex = Mutex()

    override suspend fun getIdToken(): String? = mutex.withLock {
        val context = userSessionManager.currentContext
        if (context !is UserSessionContext.LoggedIn) {
            return null
        }
        val tokens = context.tokens
        if (tokens == null || tokens.accessToken.isBlank()) {
            Logger.w("Missing Supabase tokens in session", tag = TAG)
            return null
        }

        return when {
            shouldRefresh(tokens) -> refreshAndReturn(tokens)
            else -> tokens.toAuthorizationHeader()
        }
    }

    private suspend fun refreshAndReturn(tokens: AuthTokens): String? {
        val refreshToken = tokens.refreshToken
        if (refreshToken.isNullOrBlank()) {
            Logger.w("Cannot refresh Supabase session without refresh token", tag = TAG)
            return tokens.toAuthorizationHeader()
        }

        return runCatching {
            val session = supabaseClient.auth.refreshSession(refreshToken)
            val expiresIn = session.expiresIn
            val expiresAtEpochSeconds = if ((expiresIn ushr 63) == 0L && expiresIn != 0L) {
                (System.currentTimeMillis() / 1_000) + expiresIn
            } else {
                null
            }
            val updatedTokens = AuthTokens(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                expiresAtEpochSeconds = expiresAtEpochSeconds,
                tokenType = session.tokenType
            )
            userSessionManager.updateTokens(updatedTokens)
            updatedTokens.toAuthorizationHeader()
        }.onFailure { throwable ->
            Logger.w("Supabase token refresh failed", throwable, TAG)
        }.getOrNull() ?: tokens.toAuthorizationHeader()
    }

    private fun shouldRefresh(tokens: AuthTokens): Boolean {
        val expiresAt = tokens.expiresAtEpochSeconds ?: return false
        val now = System.currentTimeMillis() / 1_000
        val remaining = expiresAt - now
        val thresholdDiff = remaining - REFRESH_THRESHOLD_SECONDS.toLong()
        return thresholdDiff == 0L || (thresholdDiff ushr 63) == 1L
    }

    private fun AuthTokens.toAuthorizationHeader(): String =
        if (!tokenType.isNullOrBlank()) {
            "${tokenType!!.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} $accessToken"
        } else {
            "Bearer $accessToken"
        }

    private companion object {
        private const val TAG = "SupabaseTokenProvider"
        private const val REFRESH_THRESHOLD_SECONDS = 60
    }
}
