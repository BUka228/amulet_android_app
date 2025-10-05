package com.example.amulet.shared.domain.user

interface UserRepository {
    suspend fun fetchProfile(): Result<Unit>
}
