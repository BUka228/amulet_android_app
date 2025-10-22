package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.domain.devices.model.DeviceConnectionProgress
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase для подключения к уже привязанному устройству.
 */
class ConnectToDeviceUseCase(
    private val devicesRepository: DevicesRepository
) {
    operator fun invoke(
        serialNumber: String,
        timeoutMs: Long = 30_000L
    ): Flow<DeviceConnectionProgress> {
        return devicesRepository.connectToDevice(serialNumber, timeoutMs)
    }
}
