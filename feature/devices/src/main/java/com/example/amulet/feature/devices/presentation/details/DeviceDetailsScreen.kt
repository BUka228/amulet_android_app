package com.example.amulet.feature.devices.presentation.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.feature.devices.presentation.components.DeviceStatusChip
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DeviceDetailsRoute(
    deviceId: String,
    viewModel: DeviceDetailsViewModel = hiltViewModel(),
    onNavigateToOta: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is DeviceDetailsSideEffect.NavigateToOta -> {
                    onNavigateToOta()
                }
                is DeviceDetailsSideEffect.DeviceUnclaimedNavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    DeviceDetailsScreen(
        state = uiState,
        onEvent = viewModel::handleEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailsScreen(
    state: DeviceDetailsState,
    onEvent: (DeviceDetailsEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.device?.name ?: "Устройство") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            state.device?.let { device ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Основная информация
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Информация",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Серийный номер:")
                                Text(device.serialNumber, style = MaterialTheme.typography.bodyMedium)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Версия прошивки:")
                                Text(device.firmwareVersion, style = MaterialTheme.typography.bodyMedium)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Статус:")
                                DeviceStatusChip(status = device.status)
                            }
                        }
                    }

                    // OTA обновление
                    state.firmwareUpdate?.takeIf { it.updateAvailable }?.let { update ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Доступно обновление",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Версия ${update.version}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                FilledTonalButton(
                                    onClick = { onEvent(DeviceDetailsEvent.NavigateToOta) }
                                ) {
                                    Icon(Icons.Default.SystemUpdate, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Обновить")
                                }
                            }
                        }
                    }

                    // Настройки
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Настройки",
                                style = MaterialTheme.typography.titleMedium
                            )

                            // Имя устройства
                            var deviceName by remember(device.name) { mutableStateOf(device.name ?: "") }
                            OutlinedTextField(
                                value = deviceName,
                                onValueChange = { deviceName = it },
                                label = { Text("Имя устройства") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                trailingIcon = {
                                    if (deviceName != device.name) {
                                        TextButton(
                                            onClick = { onEvent(DeviceDetailsEvent.UpdateName(deviceName)) }
                                        ) {
                                            Text("Сохранить")
                                        }
                                    }
                                }
                            )

                            // Яркость
                            Column {
                                Text("Яркость LED")
                                Slider(
                                    value = device.settings.brightness.toFloat(),
                                    onValueChange = { 
                                        onEvent(DeviceDetailsEvent.UpdateBrightness(it.toDouble()))
                                    },
                                    valueRange = 0f..1f
                                )
                            }

                            // Вибрация
                            Column {
                                Text("Интенсивность вибрации")
                                Slider(
                                    value = device.settings.haptics.toFloat(),
                                    onValueChange = { 
                                        onEvent(DeviceDetailsEvent.UpdateHaptics(it.toDouble()))
                                    },
                                    valueRange = 0f..1f
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Отвязать устройство?") },
            text = { Text("Это действие отвяжет устройство от вашего аккаунта. Вы сможете привязать его снова позже.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(DeviceDetailsEvent.UnclaimDevice)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Отвязать", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
