package com.example.amulet.shared.domain.user.repository

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.domain.user.model.UserId

interface UserRepository {
    suspend fun fetchProfile(userId: UserId): AppResult<User>
}
