package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.domain.devices.model.PairingDeviceFound
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase для сканирования устройств для паринга.
 */
class ScanForPairingUseCase(
    private val devicesRepository: DevicesRepository
) {
    operator fun invoke(
        serialNumberFilter: String? = null,
        timeoutMs: Long = 10_000L
    ): Flow<PairingDeviceFound> {
        return devicesRepository.scanForPairing(serialNumberFilter, timeoutMs)
    }
}
