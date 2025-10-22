package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.model.FirmwareUpdate
import com.example.amulet.shared.domain.devices.repository.OtaRepository

/**
 * UseCase для проверки доступности обновления прошивки.
 */
class CheckFirmwareUpdateUseCase(
    private val otaRepository: OtaRepository
) {
    suspend operator fun invoke(deviceId: DeviceId): AppResult<FirmwareUpdate?> {
        return otaRepository.checkFirmwareUpdate(deviceId)
    }
}
