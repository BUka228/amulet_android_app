package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.model.FirmwareUpdate
import com.example.amulet.shared.domain.devices.model.OtaUpdateProgress
import com.example.amulet.shared.domain.devices.repository.OtaRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase для запуска OTA обновления через Wi-Fi.
 */
class StartWifiOtaUpdateUseCase(
    private val otaRepository: OtaRepository
) {
    operator fun invoke(
        deviceId: DeviceId,
        ssid: String,
        password: String,
        firmwareUpdate: FirmwareUpdate
    ): Flow<OtaUpdateProgress> {
        return otaRepository.startWifiOtaUpdate(deviceId, ssid, password, firmwareUpdate)
    }
}
