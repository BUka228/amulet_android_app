package com.example.amulet.shared.domain.auth

interface AuthRepository {
    suspend fun refreshSession(): Result<Unit>
}
