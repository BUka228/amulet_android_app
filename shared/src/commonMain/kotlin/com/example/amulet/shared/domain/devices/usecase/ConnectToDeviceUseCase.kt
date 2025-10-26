package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.domain.devices.model.DeviceConnectionProgress
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase для подключения к устройству по BLE адресу.
 */
class ConnectToDeviceUseCase(
    private val devicesRepository: DevicesRepository
) {
    operator fun invoke(bleAddress: String): Flow<DeviceConnectionProgress> {
        return devicesRepository.connectToDevice(bleAddress)
    }
}
