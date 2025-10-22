package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.domain.devices.model.ConnectionStatus
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase для наблюдения за состоянием BLE подключения.
 */
class ObserveConnectionStateUseCase(
    private val devicesRepository: DevicesRepository
) {
    operator fun invoke(): Flow<ConnectionStatus> {
        return devicesRepository.observeConnectionState()
    }
}
