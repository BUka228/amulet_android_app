package com.example.amulet.feature.devices.presentation.pairing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = state.step,
            label = "pairing_step",
            transitionSpec = {
                fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                    slideInVertically(animationSpec = tween(300), initialOffsetY = { it / 3 }) togetherWith
                    fadeOut(animationSpec = tween(200)) +
                    slideOutVertically(animationSpec = tween(200), targetOffsetY = { -it / 3 })
            }
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
}

@Composable
fun ChooseMethodStep(
    onEvent: (PairingEvent) -> Unit,
    isNfcAvailable: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Заголовок с анимированной иконкой
        val infiniteTransition = rememberInfiniteTransition(label = "icon_pulse")
        val iconScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "icon_scale"
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = stringResource(R.string.pairing_title),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.pairing_scan_qr_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        // QR карточка
        ElevatedCard(
            onClick = { onEvent(PairingEvent.ChooseQrScanning) },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.pairing_scan_qr_button),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.pairing_quick_easy),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // NFC карточка
        ElevatedCard(
            onClick = { onEvent(PairingEvent.ChooseNfcReading) },
            modifier = Modifier.fillMaxWidth(),
            enabled = isNfcAvailable,
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (isNfcAvailable) 
                    MaterialTheme.colorScheme.secondaryContainer
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            if (isNfcAvailable)
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = if (isNfcAvailable)
                            MaterialTheme.colorScheme.onSecondary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(if (isNfcAvailable) R.string.pairing_use_nfc_button else R.string.pairing_nfc_unavailable),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isNfcAvailable)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isNfcAvailable) stringResource(R.string.pairing_tap_tag) else stringResource(R.string.pairing_nfc_not_available),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isNfcAvailable)
                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Ручной ввод внизу
        TextButton(
            onClick = { onEvent(PairingEvent.ChooseManualEntry) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.pairing_manual_entry_button),
                style = MaterialTheme.typography.labelLarge
            )
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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.pairing_confirm_device),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.pairing_serial_number),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = state.pairingData?.serialNumber ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                AnimatedVisibility(
                    visible = state.isScanning,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = stringResource(R.string.pairing_searching_device),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                AnimatedVisibility(
                    visible = state.foundDevice != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    state.foundDevice?.let { device ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = stringResource(R.string.pairing_device_found),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
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
            Text(
                stringResource(R.string.pairing_scan_qr_button),
                style = MaterialTheme.typography.labelLarge
            )
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
    // Анимация пульсации
    val infiniteTransition = rememberInfiniteTransition(label = "progress_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            // Внешнее кольцо пульсации
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale)
                    .alpha(pulseAlpha)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        CircleShape
                    )
            )
            // Прогресс индикатор
            CircularProgressIndicator(
                modifier = Modifier.size(72.dp),
                strokeWidth = 6.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.pairing_connecting_ble),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            progress?.let { progressText ->
                Text(
                    text = progressText,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        PairingStepIndicator(
            currentStep = 2,
            totalSteps = 4
        )

        Text(
            text = stringResource(R.string.pairing_keep_bluetooth_on),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SuccessStep(
    device: com.example.amulet.shared.domain.devices.model.Device?,
    onNavigateBack: () -> Unit
) {
    // Анимация появления галочки
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "success_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400),
        label = "success_alpha"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(140.dp)
        ) {
            // Фоновый круг
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    )
            )
            // Иконка успеха
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .alpha(alpha),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.pairing_success_title),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha)
            )

            device?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.alpha(alpha)
                ) {
                    Text(
                        text = it.name ?: it.serialNumber,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Text(
                text = stringResource(R.string.pairing_device_ready),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(alpha)
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alpha)
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
    // Анимация волн NFC
    val infiniteTransition = rememberInfiniteTransition(label = "nfc_waves")
    val wave1Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )
    val wave1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1_alpha"
    )

    val wave2Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing, delayMillis = 700),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )
    val wave2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing, delayMillis = 700),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2_alpha"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(180.dp)
        ) {
            // Волны NFC
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(wave1Scale)
                    .alpha(wave1Alpha)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(wave2Scale)
                    .alpha(wave2Alpha)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    )
            )
            // Центральная иконка
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.pairing_nfc_ready_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.pairing_nfc_ready_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.common_cancel))
        }
    }
}
