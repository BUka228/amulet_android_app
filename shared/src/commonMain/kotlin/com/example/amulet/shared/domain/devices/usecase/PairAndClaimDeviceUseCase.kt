package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.domain.devices.model.PairingProgress
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase для полного процесса паринга и привязки устройства.
 * Инкапсулирует: сканирование -> подключение -> claim на сервере -> конфигурация.
 */
class PairAndClaimDeviceUseCase(
    private val devicesRepository: DevicesRepository
) {
    operator fun invoke(
        serialNumber: String,
        claimToken: String,
        deviceName: String? = null
    ): Flow<PairingProgress> {
        return devicesRepository.pairAndClaimDevice(
            serialNumber = serialNumber,
            claimToken = claimToken,
            deviceName = deviceName
        )
    }
}
