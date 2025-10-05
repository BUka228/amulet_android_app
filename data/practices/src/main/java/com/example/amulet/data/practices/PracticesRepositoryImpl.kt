package com.example.amulet.data.practices

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.github.michaelbull.result.Ok
import org.koin.core.module.Module
import org.koin.dsl.module

class PracticesRepositoryImpl : PracticesRepository {
    override suspend fun loadPractices(): AppResult<Unit> = Ok(Unit)
}

val practicesDataModule: Module = module {
    single<PracticesRepository> { PracticesRepositoryImpl() }
}
