package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.domain.devices.model.ScannedAmulet
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case для сканирования доступных BLE устройств.
 * Возвращает поток списков всех найденных устройств.
 */
class ScanForDevicesUseCase(
    private val devicesRepository: DevicesRepository
) {
    operator fun invoke(timeoutMs: Long = 30_000L): Flow<List<ScannedAmulet>> {
        return devicesRepository.scanForDevices(timeoutMs)
    }
}
