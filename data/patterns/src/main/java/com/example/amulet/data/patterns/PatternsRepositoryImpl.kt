package com.example.amulet.data.patterns

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.github.michaelbull.result.Ok
import org.koin.core.module.Module
import org.koin.dsl.module

class PatternsRepositoryImpl : PatternsRepository {
    override suspend fun syncPatterns(): AppResult<Unit> = Ok(Unit)
}

val patternsDataModule: Module = module {
    single<PatternsRepository> { PatternsRepositoryImpl() }
}
