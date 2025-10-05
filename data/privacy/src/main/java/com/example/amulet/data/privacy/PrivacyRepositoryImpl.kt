package com.example.amulet.data.privacy

import com.example.amulet.shared.domain.privacy.PrivacyRepository
import org.koin.core.module.Module
import org.koin.dsl.module

class PrivacyRepositoryImpl : PrivacyRepository {
    override suspend fun updatePrivacySettings(): Result<Unit> = Result.success(Unit)
}

val privacyDataModule: Module = module {
    single<PrivacyRepository> { PrivacyRepositoryImpl() }
}
