package com.example.amulet.core.supabase.auth

import com.example.amulet.shared.core.auth.IdTokenProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация IdTokenProvider для Supabase.
 * Предоставляет Authorization заголовок для HTTP-запросов.
 */
@Singleton
class SupabaseIdTokenProvider @Inject constructor(
    private val authManager: SupabaseAuthManager
) : IdTokenProvider {
    
    override suspend fun getIdToken(): String? {
        return authManager.getAuthorizationHeader()
    }
}
