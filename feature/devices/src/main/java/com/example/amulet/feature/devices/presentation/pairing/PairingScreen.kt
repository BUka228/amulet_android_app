package com.example.amulet.feature.devices.presentation.pairing

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.core.design.scaffold.ShowOnlyTopBar
import com.example.amulet.feature.devices.R
import com.example.amulet.feature.devices.presentation.components.PairingStepIndicator
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PairingRoute(
    viewModel: PairingViewModel = hiltViewModel(),
    onPairingComplete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Инициализация NFC доступности
    LaunchedEffect(Unit) {
        val isNfcAvailable = viewModel.nfcManager.isNfcAvailable()
        viewModel.setNfcAvailability(isNfcAvailable)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is PairingSideEffect.PairingComplete -> {
                    onPairingComplete()
                }
                is PairingSideEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    PairingScreen(
        state = uiState,
        onEvent = viewModel::handleEvent,
        onNavigateBack = onNavigateBack,
        qrScanManager = viewModel.qrScanManager,
        onQrScanned = viewModel::onQrScanned
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(
    state: PairingState,
    onEvent: (PairingEvent) -> Unit,
    onNavigateBack: () -> Unit,
    qrScanManager: com.example.amulet.feature.devices.scanner.QrScanManager,
    onQrScanned: (String) -> Unit
) {
    val scaffoldState = LocalScaffoldState.current

    // Настраиваем TopBar через централизованный scaffold
    scaffoldState.ShowOnlyTopBar {
        TopAppBar(
            title = { Text(stringResource(R.string.pairing_title)) },
            navigationIcon = {
                IconButton(onClick = { onEvent(PairingEvent.CancelPairing) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            AnimatedContent(
                targetState = state.step,
                label = "pairing_step"
            ) { step ->
                when (step) {
                    PairingStep.CHOOSE_METHOD -> {
                        ChooseMethodStep(
                            onEvent = onEvent,
                            isNfcAvailable = state.isNfcAvailable
                        )
                    }
                    PairingStep.QR_SCANNING -> {
                        QrScanningStep(
                            qrScanManager = qrScanManager,
                            onQrScanned = onQrScanned,
                            onCancel = { onEvent(PairingEvent.CancelPairing) }
                        )
                    }
                    PairingStep.NFC_READING -> {
                        NfcReadingStep(
                            onCancel = { onEvent(PairingEvent.CancelPairing) }
                        )
                    }
                    PairingStep.CONFIRM_DEVICE -> {
                        ConfirmDeviceStep(
                            state = state,
                            onEvent = onEvent
                        )
                    }
                    PairingStep.PAIRING -> {
                        PairingInProgressStep(
                            progress = state.pairingProgress
                        )
                    }
                    PairingStep.SUCCESS -> {
                        SuccessStep(
                            device = state.pairedDevice,
                            onNavigateBack = onNavigateBack
                        )
                    }
                    PairingStep.ERROR -> {
                        ErrorStep(
                            error = state.error,
                            onRetry = { onEvent(PairingEvent.RetryPairing) },
                            onCancel = { onEvent(PairingEvent.CancelPairing) }
                        )
                    }
                }
            }
        }
}

@Composable
fun ChooseMethodStep(
    onEvent: (PairingEvent) -> Unit,
    isNfcAvailable: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.pairing_scan_qr_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.pairing_scan_qr_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = { onEvent(PairingEvent.ChooseQrScanning) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.pairing_scan_qr_button))
        }

        OutlinedButton(
            onClick = { onEvent(PairingEvent.ChooseNfcReading) },
            modifier = Modifier.fillMaxWidth(),
            enabled = isNfcAvailable
        ) {
            Icon(Icons.Default.Sensors, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(if (isNfcAvailable) R.string.pairing_use_nfc_button else R.string.pairing_nfc_unavailable))
        }

        TextButton(
            onClick = { onEvent(PairingEvent.ChooseManualEntry) }
        ) {
            Text(stringResource(R.string.pairing_manual_entry_button))
        }
    }
}

@Composable
fun ConfirmDeviceStep(
    state: PairingState,
    onEvent: (PairingEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.pairing_scan_qr_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.device_details_serial),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = state.pairingData?.serialNumber ?: "",
                    style = MaterialTheme.typography.titleMedium
                )

                if (state.isScanning) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = stringResource(R.string.pairing_searching_device),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                state.foundDevice?.let { device ->
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.pairing_device_found),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { onEvent(PairingEvent.StartPairing) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.foundDevice != null && !state.isScanning
        ) {
            Text(stringResource(R.string.pairing_scan_qr_button))
        }

        OutlinedButton(
            onClick = { onEvent(PairingEvent.CancelPairing) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.common_cancel))
        }
    }
}

@Composable
fun PairingInProgressStep(
    progress: String?
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = stringResource(R.string.pairing_connecting_ble),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        progress?.let { progressText ->
            Text(
                text = progressText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        PairingStepIndicator(
            currentStep = 2,
            totalSteps = 4
        )
    }
}

@Composable
fun SuccessStep(
    device: com.example.amulet.shared.domain.devices.model.Device?,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.pairing_success_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        device?.let {
            Text(
                text = it.name ?: it.serialNumber,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.pairing_success_button))
        }
    }
}

@Composable
fun ErrorStep(
    error: com.example.amulet.shared.core.AppError?,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Text(
            text = stringResource(R.string.pairing_error_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        error?.let {
            Text(
                text = it.toString(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.pairing_retry_button))
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.common_cancel))
        }
    }
}

@Composable
fun QrScanningStep(
    qrScanManager: com.example.amulet.feature.devices.scanner.QrScanManager,
    onQrScanned: (String) -> Unit,
    onCancel: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // QR Scanner View
        com.example.amulet.feature.devices.presentation.pairing.components.QrScannerView(
            qrScanManager = qrScanManager,
            onQrScanned = onQrScanned,
            modifier = Modifier.fillMaxSize()
        )
        
        // Cancel button overlay
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Text(stringResource(R.string.common_cancel))
        }
    }
}

@Composable
fun NfcReadingStep(
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = Icons.Default.Sensors,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp)
        )

        Text(
            text = stringResource(R.string.pairing_nfc_ready_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.pairing_nfc_ready_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.common_cancel))
        }
    }
}
