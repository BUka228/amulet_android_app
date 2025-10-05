package com.example.amulet.data.devices

import com.example.amulet.shared.domain.devices.DevicesRepository
import org.koin.core.module.Module
import org.koin.dsl.module

class DevicesRepositoryImpl : DevicesRepository {
    override suspend fun syncDevices(): Result<Unit> = Result.success(Unit)
}

val devicesDataModule: Module = module {
    single<DevicesRepository> { DevicesRepositoryImpl() }
}
