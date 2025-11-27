package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.domain.devices.model.DeviceSessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * UseCase, объединяющий BLE-состояние и живой статус устройства в единый поток.
 */
class ObserveDeviceSessionStatusUseCase(
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase,
    private val observeConnectedDeviceStatusUseCase: ObserveConnectedDeviceStatusUseCase,
) {

    operator fun invoke(): Flow<DeviceSessionStatus> =
        combine(
            observeConnectionStateUseCase(),
            observeConnectedDeviceStatusUseCase(),
        ) { connection, live ->
            DeviceSessionStatus(
                connection = connection,
                liveStatus = live,
            )
        }
}
