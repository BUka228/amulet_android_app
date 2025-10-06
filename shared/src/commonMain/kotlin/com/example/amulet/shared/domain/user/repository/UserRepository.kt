package com.example.amulet.shared.domain.user.repository

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.user.model.User

interface UserRepository {
    suspend fun fetchProfile(userId: String): AppResult<User>
}
