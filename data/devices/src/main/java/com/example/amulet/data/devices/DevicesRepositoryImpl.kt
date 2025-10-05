package com.example.amulet.data.devices

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.DevicesRepository
import com.github.michaelbull.result.Ok
import org.koin.core.module.Module
import org.koin.dsl.module

class DevicesRepositoryImpl : DevicesRepository {
    override suspend fun syncDevices(): AppResult<Unit> = Ok(Unit)
}

val devicesDataModule: Module = module {
    single<DevicesRepository> { DevicesRepositoryImpl() }
}
