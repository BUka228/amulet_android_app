package com.example.amulet.data.practices

import com.example.amulet.shared.domain.practices.PracticesRepository
import org.koin.core.module.Module
import org.koin.dsl.module

class PracticesRepositoryImpl : PracticesRepository {
    override suspend fun loadPractices(): Result<Unit> = Result.success(Unit)
}

val practicesDataModule: Module = module {
    single<PracticesRepository> { PracticesRepositoryImpl() }
}
