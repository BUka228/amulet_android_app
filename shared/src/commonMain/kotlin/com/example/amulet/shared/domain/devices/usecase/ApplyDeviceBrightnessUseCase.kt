package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.repository.DevicesRepository

/**
 * UseCase для применения яркости устройства на физическом амулете.
 */
class ApplyDeviceBrightnessUseCase(
    private val devicesRepository: DevicesRepository
) {
    suspend operator fun invoke(
        deviceId: DeviceId,
        brightness: Double
    ): AppResult<Unit> {
        return devicesRepository.applyBrightnessToDevice(deviceId, brightness)
    }
}
