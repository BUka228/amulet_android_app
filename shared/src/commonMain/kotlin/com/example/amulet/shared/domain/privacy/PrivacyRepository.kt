package com.example.amulet.shared.domain.privacy

import com.example.amulet.shared.core.AppResult

interface PrivacyRepository {
    suspend fun updatePrivacySettings(): AppResult<Unit>
}
