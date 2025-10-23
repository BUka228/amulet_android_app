package com.example.amulet.feature.devices.presentation.ota

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.shared.domain.devices.model.OtaUpdateState as OtaProgressStage
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OtaUpdateRoute(
    deviceId: String,
    viewModel: OtaUpdateViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is OtaUpdateSideEffect.UpdateCompleted -> {
                    // Можно показать snackbar перед возвратом
                    onNavigateBack()
                }
                is OtaUpdateSideEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    OtaUpdateScreen(
        state = uiState,
        onEvent = viewModel::handleEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtaUpdateScreen(
    state: OtaUpdateState,
    onEvent: (OtaUpdateEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showWifiDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Обновление прошивки") },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(OtaUpdateEvent.NavigateBack) },
                        enabled = !state.isUpdating
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.isUpdating -> {
                    OtaProgressView(
                        progress = state.otaProgress,
                        method = state.updateMethod,
                        onCancel = { onEvent(OtaUpdateEvent.CancelUpdate) }
                    )
                }
                state.firmwareUpdate != null -> {
                    UpdateAvailableView(
                        firmwareUpdate = state.firmwareUpdate,
                        onStartBleUpdate = { onEvent(OtaUpdateEvent.StartBleUpdate) },
                        onStartWifiUpdate = { showWifiDialog = true }
                    )
                }
                else -> {
                    NoUpdateAvailableView(
                        onNavigateBack = onNavigateBack
                    )
                }
            }
        }
    }

    if (showWifiDialog) {
        WifiCredentialsDialog(
            onDismiss = { showWifiDialog = false },
            onConfirm = { ssid, password ->
                onEvent(OtaUpdateEvent.StartWifiUpdate(ssid, password))
                showWifiDialog = false
            }
        )
    }
}

@Composable
fun UpdateAvailableView(
    firmwareUpdate: com.example.amulet.shared.domain.devices.model.FirmwareUpdate,
    onStartBleUpdate: () -> Unit,
    onStartWifiUpdate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Доступно обновление",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Версия ${firmwareUpdate.version}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                firmwareUpdate.notes?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "Размер: ${firmwareUpdate.size / 1024} КБ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Text(
            text = "Выберите способ обновления:",
            style = MaterialTheme.typography.titleMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onStartBleUpdate
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Обновление через BLE",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Медленнее, но надежнее. Устройство должно быть рядом.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onStartWifiUpdate
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Обновление через Wi-Fi",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Быстрее. Потребуется Wi-Fi сеть.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun OtaProgressView(
    progress: com.example.amulet.shared.domain.devices.model.OtaUpdateProgress?,
    method: OtaUpdateMethod?,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = when (method) {
                OtaUpdateMethod.BLE -> Icons.Default.Bluetooth
                OtaUpdateMethod.WIFI -> Icons.Default.Wifi
                null -> Icons.Default.Bluetooth
            },
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = when (progress?.state) {
                OtaProgressStage.PREPARING -> "Подготовка..."
                OtaProgressStage.DOWNLOADING -> "Загрузка прошивки..."
                OtaProgressStage.TRANSFERRING -> "Передача на устройство..."
                OtaProgressStage.VERIFYING -> "Проверка целостности..."
                OtaProgressStage.INSTALLING -> "Установка..."
                OtaProgressStage.COMPLETED -> "Обновление завершено!"
                else -> "Обновление..."
            },
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        progress?.let { prog ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { prog.percent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${prog.percent}%",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${prog.currentBytes / 1024} / ${prog.totalBytes / 1024} КБ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = prog.state == OtaProgressStage.COMPLETED) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (progress?.state != OtaProgressStage.COMPLETED) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отменить")
            }
        }
    }
}

@Composable
fun NoUpdateAvailableView(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Прошивка актуальна",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "У вас установлена последняя версия прошивки",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onNavigateBack) {
            Text("Назад")
        }
    }
}

@Composable
fun WifiCredentialsDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройка Wi-Fi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = ssid,
                    onValueChange = { ssid = it },
                    label = { Text("SSID (имя сети)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(ssid, password) },
                enabled = ssid.isNotBlank() && password.isNotBlank()
            ) {
                Text("Подтвердить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
