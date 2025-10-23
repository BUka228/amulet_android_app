package com.example.amulet.feature.devices.presentation.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.devices.model.PairingData
import com.example.amulet.shared.domain.devices.model.PairingProgress
import com.example.amulet.shared.domain.devices.usecase.PairAndClaimDeviceUseCase
import com.example.amulet.shared.domain.devices.usecase.ScanForPairingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val scanForPairingUseCase: ScanForPairingUseCase,
    private val pairAndClaimDeviceUseCase: PairAndClaimDeviceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingState())
    val uiState: StateFlow<PairingState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<PairingSideEffect>()
    val sideEffect: SharedFlow<PairingSideEffect> = _sideEffect.asSharedFlow()

    fun handleEvent(event: PairingEvent) {
        when (event) {
            is PairingEvent.QrCodeScanned -> handleQrCodeScanned(event.qrContent)
            is PairingEvent.NfcTagRead -> handleNfcTagRead(event.nfcPayload)
            is PairingEvent.ManualSerialEntered -> handleManualEntry(event.serialNumber, event.claimToken)
            is PairingEvent.StartPairing -> startPairing()
            is PairingEvent.CancelPairing -> cancelPairing()
            is PairingEvent.RetryPairing -> retryPairing()
            is PairingEvent.DismissError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun handleQrCodeScanned(qrContent: String) {
        val pairingData = PairingData.fromQrCode(qrContent)
        if (pairingData != null) {
            _uiState.update { 
                it.copy(
                    pairingData = pairingData,
                    step = PairingStep.CONFIRM_DEVICE,
                    error = null
                )
            }
            startScanningForDevice(pairingData.serialNumber)
        } else {
            _uiState.update { 
                it.copy(error = AppError.Validation(mapOf("qr" to "invalid_qr_format")))
            }
        }
    }

    private fun handleNfcTagRead(nfcPayload: String) {
        val pairingData = PairingData.fromNfcPayload(nfcPayload)
        if (pairingData != null) {
            _uiState.update { 
                it.copy(
                    pairingData = pairingData,
                    step = PairingStep.CONFIRM_DEVICE,
                    error = null
                )
            }
            startScanningForDevice(pairingData.serialNumber)
        } else {
            _uiState.update { 
                it.copy(error = AppError.Validation(mapOf("nfc" to "invalid_nfc_format")))
            }
        }
    }

    private fun handleManualEntry(serialNumber: String, claimToken: String) {
        val pairingData = PairingData(serialNumber, claimToken)
        _uiState.update { 
            it.copy(
                pairingData = pairingData,
                step = PairingStep.CONFIRM_DEVICE,
                error = null
            )
        }
        startScanningForDevice(serialNumber)
    }

    private fun startScanningForDevice(serialNumber: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            scanForPairingUseCase(serialNumberFilter = serialNumber, timeoutMs = 10_000L)
                .take(1) // Берем первое найденное устройство
                .collect { device ->
                    _uiState.update { 
                        it.copy(
                            foundDevice = device,
                            isScanning = false
                        )
                    }
                }
        }
    }

    private fun startPairing() {
        val pairingData = _uiState.value.pairingData ?: return
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    step = PairingStep.PAIRING,
                    isPairing = true,
                    error = null
                )
            }

            pairAndClaimDeviceUseCase(
                serialNumber = pairingData.serialNumber,
                claimToken = pairingData.claimToken,
                deviceName = null
            ).collect { progress ->
                handlePairingProgress(progress)
            }
        }
    }

    private fun handlePairingProgress(progress: PairingProgress) {
        when (progress) {
            is PairingProgress.SearchingDevice -> {
                _uiState.update { it.copy(pairingProgress = "searching_device") }
            }
            is PairingProgress.DeviceFound -> {
                _uiState.update { it.copy(pairingProgress = "device_found") }
            }
            is PairingProgress.ConnectingBle -> {
                _uiState.update { it.copy(pairingProgress = "connecting_ble") }
            }
            is PairingProgress.ClaimingOnServer -> {
                _uiState.update { it.copy(pairingProgress = "claiming_server") }
            }
            is PairingProgress.ConfiguringDevice -> {
                _uiState.update { it.copy(pairingProgress = "configuring_device") }
            }
            is PairingProgress.Completed -> {
                _uiState.update { 
                    it.copy(
                        step = PairingStep.SUCCESS,
                        isPairing = false,
                        pairedDevice = progress.device,
                        pairingProgress = null
                    )
                }
                viewModelScope.launch {
                    _sideEffect.emit(PairingSideEffect.PairingComplete(progress.device))
                }
            }
            is PairingProgress.Failed -> {
                _uiState.update { 
                    it.copy(
                        step = PairingStep.ERROR,
                        isPairing = false,
                        error = progress.error,
                        pairingProgress = null
                    )
                }
            }
        }
    }

    private fun cancelPairing() {
        viewModelScope.launch {
            _sideEffect.emit(PairingSideEffect.NavigateBack)
        }
    }

    private fun retryPairing() {
        _uiState.update { 
            PairingState(step = PairingStep.SCAN_QR)
        }
    }
}
