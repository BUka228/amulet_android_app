package com.example.amulet.data.privacy

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.privacy.PrivacyRepository
import com.github.michaelbull.result.Ok
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrivacyRepositoryImpl @Inject constructor() : PrivacyRepository {
    override suspend fun updatePrivacySettings(): AppResult<Unit> = Ok(Unit)
}
