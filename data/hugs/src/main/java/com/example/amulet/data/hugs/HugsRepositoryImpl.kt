package com.example.amulet.data.hugs

import com.example.amulet.shared.domain.hugs.HugsRepository
import org.koin.core.module.Module
import org.koin.dsl.module

class HugsRepositoryImpl : HugsRepository {
    override suspend fun sendHug(): Result<Unit> {
        return Result.success(Unit)
    }
}

val hugsDataModule: Module = module {
    single<HugsRepository> { HugsRepositoryImpl() }
}
