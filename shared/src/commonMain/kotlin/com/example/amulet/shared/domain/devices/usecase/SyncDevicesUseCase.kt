package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.repository.DevicesRepository

/**
 * UseCase для синхронизации устройств с сервером.
 */
class SyncDevicesUseCase(
    private val devicesRepository: DevicesRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return devicesRepository.syncDevices()
    }
}
