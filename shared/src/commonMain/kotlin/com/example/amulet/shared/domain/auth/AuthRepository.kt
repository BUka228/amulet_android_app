package com.example.amulet.shared.domain.auth

import com.example.amulet.shared.core.AppResult

interface AuthRepository {
    suspend fun refreshSession(): AppResult<Unit>
}
