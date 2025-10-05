package com.example.amulet.data.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.HugsRepository
import com.github.michaelbull.result.Ok
import org.koin.core.module.Module
import org.koin.dsl.module

class HugsRepositoryImpl : HugsRepository {
    override suspend fun sendHug(): AppResult<Unit> = Ok(Unit)
}

val hugsDataModule: Module = module {
    single<HugsRepository> { HugsRepositoryImpl() }
}
