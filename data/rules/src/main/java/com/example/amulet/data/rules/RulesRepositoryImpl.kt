package com.example.amulet.data.rules

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.rules.RulesRepository
import com.github.michaelbull.result.Ok
import org.koin.core.module.Module
import org.koin.dsl.module

class RulesRepositoryImpl : RulesRepository {
    override suspend fun syncRules(): AppResult<Unit> = Ok(Unit)
}

val rulesDataModule: Module = module {
    single<RulesRepository> { RulesRepositoryImpl() }
}
