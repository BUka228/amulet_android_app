package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.repository.DevicesRepository

/**
 * UseCase для отвязки устройства от аккаунта.
 */
class UnclaimDeviceUseCase(
    private val devicesRepository: DevicesRepository
) {
    suspend operator fun invoke(deviceId: DeviceId): AppResult<Unit> {
        return devicesRepository.unclaimDevice(deviceId)
    }
}
