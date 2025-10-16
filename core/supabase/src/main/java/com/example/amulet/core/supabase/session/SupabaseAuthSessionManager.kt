package com.example.amulet.core.supabase.session

import com.example.amulet.shared.core.logging.Logger
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Кастомный SessionManager для Supabase SDK.
 * Автоматически сохраняет и восстанавливает сессии через SupabaseSessionStorage.
 */
@Singleton
class SupabaseAuthSessionManager @Inject constructor(
    private val storage: SupabaseSessionStorage
) : SessionManager {

    override suspend fun deleteSession() {
        Logger.d("Deleting Supabase session", TAG)
        storage.clearSession()
    }

    override suspend fun loadSession(): UserSession? {
        Logger.d("Loading Supabase session from storage", TAG)
        val session = storage.loadSession()
        return if (session != null) {
            Logger.i("Supabase session loaded successfully", TAG)
            session
        } else {
            Logger.d("No Supabase session found in storage", TAG)
            null
        }
    }

    override suspend fun saveSession(session: UserSession) {
        Logger.d("Saving Supabase session to storage", TAG)
        storage.saveSession(session)
    }

    private companion object {
        private const val TAG = "SupabaseAuthSessionManager"
    }
}
