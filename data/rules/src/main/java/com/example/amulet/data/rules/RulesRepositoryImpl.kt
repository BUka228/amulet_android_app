package com.example.amulet.data.rules

import com.example.amulet.shared.domain.rules.RulesRepository
import org.koin.core.module.Module
import org.koin.dsl.module

class RulesRepositoryImpl : RulesRepository {
    override suspend fun syncRules(): Result<Unit> = Result.success(Unit)
}

val rulesDataModule: Module = module {
    single<RulesRepository> { RulesRepositoryImpl() }
}
