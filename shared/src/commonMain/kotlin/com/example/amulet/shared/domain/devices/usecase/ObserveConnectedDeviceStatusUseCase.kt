package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.domain.devices.model.DeviceLiveStatus
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase для наблюдения за живым статусом подключенного устройства.
 */
class ObserveConnectedDeviceStatusUseCase(
    private val devicesRepository: DevicesRepository
) {
    operator fun invoke(): Flow<DeviceLiveStatus?> {
        return devicesRepository.observeConnectedDeviceStatus()
    }
}
