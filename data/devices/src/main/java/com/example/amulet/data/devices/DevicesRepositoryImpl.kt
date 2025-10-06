package com.example.amulet.data.devices

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.DevicesRepository
import com.github.michaelbull.result.Ok
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevicesRepositoryImpl @Inject constructor() : DevicesRepository {
    override suspend fun syncDevices(): AppResult<Unit> = Ok(Unit)
}
