package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.model.Device
import com.example.amulet.shared.domain.devices.repository.DevicesRepository


/**
 * Use case для добавления нового устройства в локальную БД.
 */
class AddDeviceUseCase(
    private val devicesRepository: DevicesRepository
) {
    suspend operator fun invoke(
        bleAddress: String,
        name: String,
        hardwareVersion: Int
    ): AppResult<Device> {
        return devicesRepository.addDevice(
            bleAddress = bleAddress,
            name = name,
            hardwareVersion = hardwareVersion
        )
    }
}
