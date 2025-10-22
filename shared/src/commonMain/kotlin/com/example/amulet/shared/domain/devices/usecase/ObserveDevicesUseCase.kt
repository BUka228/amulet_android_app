package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.domain.devices.model.Device
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase для наблюдения за списком устройств пользователя.
 */
class ObserveDevicesUseCase(
    private val devicesRepository: DevicesRepository
) {
    operator fun invoke(): Flow<List<Device>> {
        return devicesRepository.observeDevices()
    }
}
