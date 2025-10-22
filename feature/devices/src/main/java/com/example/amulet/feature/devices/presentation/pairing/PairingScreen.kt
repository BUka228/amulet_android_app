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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.feature.devices.presentation.components.PairingStepIndicator
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PairingRoute(
    viewModel: PairingViewModel = hiltViewModel(),
    onPairingComplete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(
    state: PairingState,
    onEvent: (PairingEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить устройство") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(PairingEvent.CancelPairing) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedContent(
                targetState = state.step,
                label = "pairing_step"
            ) { step ->
                when (step) {
                    PairingStep.SCAN_QR -> {
                        ScanQrStep(
                            onEvent = onEvent,
                            isScanning = state.isScanning
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
}

@Composable
fun ScanQrStep(
    onEvent: (PairingEvent) -> Unit,
    isScanning: Boolean
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
            text = "Отсканируйте QR код",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Найдите QR код на упаковке вашего амулета",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = { /* TODO: Открыть сканер QR */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Сканировать QR код")
        }

        OutlinedButton(
            onClick = { /* TODO: Использовать NFC */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Sensors, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Использовать NFC")
        }

        TextButton(
            onClick = { /* TODO: Открыть ручной ввод */ }
        ) {
            Text("Ввести серийный номер вручную")
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
            text = "Подтвердите устройство",
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
                    text = "Серийный номер:",
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
                            text = "Поиск устройства...",
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
                                text = "Устройство найдено",
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
            Text("Подключить устройство")
        }

        OutlinedButton(
            onClick = { onEvent(PairingEvent.CancelPairing) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Отмена")
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
            text = "Подключение устройства...",
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
            text = "Устройство подключено!",
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
            Text("Готово")
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
            text = "Ошибка подключения",
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
            Text("Попробовать снова")
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Отмена")
        }
    }
}
