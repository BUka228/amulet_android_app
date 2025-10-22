package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.repository.OtaRepository

/**
 * UseCase для отмены текущего OTA обновления.
 */
class CancelOtaUpdateUseCase(
    private val otaRepository: OtaRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return otaRepository.cancelOtaUpdate()
    }
}
