package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.domain.devices.model.PairingDeviceFound
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case для сканирования доступных BLE устройств.
 */
class ScanForDevicesUseCase(
    private val devicesRepository: DevicesRepository
) {
    operator fun invoke(timeoutMs: Long = 30_000L): Flow<PairingDeviceFound> {
        return devicesRepository.scanForDevices(timeoutMs)
    }
}
