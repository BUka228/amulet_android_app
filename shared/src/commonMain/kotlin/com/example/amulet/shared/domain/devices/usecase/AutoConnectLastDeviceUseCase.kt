package com.example.amulet.shared.domain.devices.usecase

import com.example.amulet.shared.domain.devices.model.BleConnectionState
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import kotlinx.coroutines.flow.first
import com.example.amulet.shared.core.logging.Logger

class AutoConnectLastDeviceUseCase(
    private val devicesRepository: DevicesRepository
) {
    suspend operator fun invoke() {
        Logger.d("AutoConnect: start", tag = TAG)
        val lastUsed = try {
            devicesRepository.getLastConnectedDevice()
        } catch (e: Exception) {
            Logger.e("AutoConnect: failed to get last connected device: ${'$'}e", tag = TAG)
            null
        }

        if (lastUsed == null) {
            Logger.d("AutoConnect: no last connected device found", tag = TAG)
            return
        }

        Logger.d("AutoConnect: last device id=${'$'}{lastUsed.id.value} ble=${'$'}{lastUsed.bleAddress}", tag = TAG)

        try {
            val flow = devicesRepository.connectToDevice(lastUsed.bleAddress)
            flow.first { state ->
                when (state) {
                    is BleConnectionState.Connected -> {
                        Logger.d("AutoConnect: connected to ${'$'}{lastUsed.bleAddress}", tag = TAG)
                        true
                    }
                    is BleConnectionState.Failed -> {
                        Logger.e("AutoConnect: failed to connect to ${'$'}{lastUsed.bleAddress} error=${'$'}{state.error}", tag = TAG)
                        true
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            Logger.e("AutoConnect: exception during connect to ${'$'}{lastUsed.bleAddress}: ${'$'}e", tag = TAG)
        }
    }

    private companion object {
        private const val TAG = "AutoConnectLastDevice"
    }
}
