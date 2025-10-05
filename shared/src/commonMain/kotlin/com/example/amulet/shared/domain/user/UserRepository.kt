package com.example.amulet.shared.domain.user

import com.example.amulet.shared.core.AppResult

interface UserRepository {
    suspend fun fetchProfile(): AppResult<Unit>
}
