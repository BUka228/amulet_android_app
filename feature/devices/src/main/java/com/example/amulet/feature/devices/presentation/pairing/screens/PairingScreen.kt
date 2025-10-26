package com.example.amulet.feature.devices.presentation.pairing.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.feature.devices.presentation.pairing.PairingEvent
import com.example.amulet.feature.devices.presentation.pairing.PairingSideEffect
import com.example.amulet.feature.devices.presentation.pairing.PairingViewModel
import com.example.amulet.shared.domain.devices.model.PairingDeviceFound
import com.example.amulet.shared.domain.devices.model.SignalStrength

/**
 * Упрощенный экран добавления устройства.
 * Один экран: BLE сканирование + упоминание NFC.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(
    onNavigateBack: () -> Unit,
    onDeviceAdded: () -> Unit,
    viewModel: PairingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showNameDialog by remember { mutableStateOf(false) }
    var deviceName by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is PairingSideEffect.DeviceAdded -> onDeviceAdded()
                is PairingSideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить устройство") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(PairingEvent.NavigateBack) }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // NFC подсказка
            if (state.showNfcHint) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Nfc,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Приложите устройство к телефону для быстрого подключения через NFC",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
            
            // Заголовок сканирования
            Text(
                "Доступные устройства",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            
            // Кнопка сканирования
            if (!state.isScanning) {
                Button(
                    onClick = { viewModel.onEvent(PairingEvent.StartScanning) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Bluetooth, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Начать сканирование")
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.onEvent(PairingEvent.StopScanning) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Остановить сканирование")
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Список найденных устройств
            if (state.foundDevices.isEmpty() && state.isScanning) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Поиск устройств...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (state.foundDevices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Нажмите кнопку для поиска устройств",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.foundDevices) { device ->
                        DeviceCard(
                            device = device,
                            isSelected = state.selectedDevice == device,
                            onClick = {
                                viewModel.onEvent(PairingEvent.SelectDevice(device))
                                showNameDialog = true
                            }
                        )
                    }
                }
            }
            
            // Ошибка
            state.error?.let { error ->
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        error.toString(),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
    
    // Диалог ввода имени
    if (showNameDialog && state.selectedDevice != null) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Имя устройства") },
            text = {
                Column {
                    Text("Введите имя для устройства ${state.selectedDevice?.deviceName ?: "Amulet"}")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deviceName,
                        onValueChange = { deviceName = it },
                        label = { Text("Имя") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(PairingEvent.ConnectAndAddDevice(deviceName))
                        showNameDialog = false
                    }
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
    
    // Прогресс подключения
    if (state.isConnecting) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Подключение") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text(state.connectionProgress ?: "Подключение...")
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(PairingEvent.CancelConnection) }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun DeviceCard(
    device: PairingDeviceFound,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Bluetooth,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    device.deviceName ?: "Amulet",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    device.bleAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SignalIndicator(device.signalStrength)
        }
    }
}

@Composable
private fun SignalIndicator(strength: SignalStrength) {
    val color = when (strength) {
        SignalStrength.EXCELLENT -> MaterialTheme.colorScheme.primary
        SignalStrength.GOOD -> MaterialTheme.colorScheme.primary
        SignalStrength.FAIR -> MaterialTheme.colorScheme.tertiary
        SignalStrength.WEAK -> MaterialTheme.colorScheme.error
    }
    
    Text(
        when (strength) {
            SignalStrength.EXCELLENT -> "Отлично"
            SignalStrength.GOOD -> "Хорошо"
            SignalStrength.FAIR -> "Средне"
            SignalStrength.WEAK -> "Слабо"
        },
        style = MaterialTheme.typography.bodySmall,
        color = color
    )
}
