package com.example.amulet.data.privacy

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.privacy.PrivacyRepository
import com.github.michaelbull.result.Ok
import org.koin.core.module.Module
import org.koin.dsl.module

class PrivacyRepositoryImpl : PrivacyRepository {
    override suspend fun updatePrivacySettings(): AppResult<Unit> = Ok(Unit)
}

val privacyDataModule: Module = module {
    single<PrivacyRepository> { PrivacyRepositoryImpl() }
}
