package com.example.amulet.shared.domain.privacy

interface PrivacyRepository {
    suspend fun updatePrivacySettings(): Result<Unit>
}
