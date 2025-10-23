package com.example.amulet.feature.devices.presentation.pairing

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.devices.model.Device
import com.example.amulet.shared.domain.devices.model.PairingData
import com.example.amulet.shared.domain.devices.model.PairingDeviceFound

/**
 * Контракт для экрана паринга устройства.
 */

data class PairingState(
    val step: PairingStep = PairingStep.SCAN_QR,
    val pairingData: PairingData? = null,
    val foundDevice: PairingDeviceFound? = null,
    val isScanning: Boolean = false,
    val isPairing: Boolean = false,
    val pairingProgress: String? = null,
    val pairedDevice: Device? = null,
    val error: AppError? = null
)

enum class PairingStep {
    SCAN_QR,
    CONFIRM_DEVICE,
    PAIRING,
    SUCCESS,
    ERROR
}

sealed interface PairingEvent {
    data class QrCodeScanned(val qrContent: String) : PairingEvent
    data class NfcTagRead(val nfcPayload: String) : PairingEvent
    data class ManualSerialEntered(val serialNumber: String, val claimToken: String) : PairingEvent
    data object StartPairing : PairingEvent
    data object CancelPairing : PairingEvent
    data object RetryPairing : PairingEvent
    data object DismissError : PairingEvent
}

sealed interface PairingSideEffect {
    data class PairingComplete(val device: Device) : PairingSideEffect
    data object NavigateBack : PairingSideEffect
}
