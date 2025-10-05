package com.example.amulet.data.patterns

import com.example.amulet.shared.domain.patterns.PatternsRepository
import org.koin.core.module.Module
import org.koin.dsl.module

class PatternsRepositoryImpl : PatternsRepository {
    override suspend fun syncPatterns(): Result<Unit> = Result.success(Unit)
}

val patternsDataModule: Module = module {
    single<PatternsRepository> { PatternsRepositoryImpl() }
}
