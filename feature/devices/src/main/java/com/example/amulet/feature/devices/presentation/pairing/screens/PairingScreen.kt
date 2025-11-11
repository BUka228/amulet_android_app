package com.example.amulet.feature.devices.presentation.pairing.screens

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.devices.presentation.pairing.PairingEvent
import com.example.amulet.feature.devices.presentation.pairing.PairingSideEffect
import com.example.amulet.feature.devices.presentation.pairing.PairingViewModel
import com.example.amulet.shared.domain.devices.model.ScannedAmulet
import com.example.amulet.shared.domain.devices.model.SignalStrength
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Экран добавления устройства с автоматическим сканированием.
 * Использует LocalScaffoldState для TopBar без вложенного Scaffold.
 * BLE сканирование + упоминание NFC.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PairingScreen(
    onNavigateBack: () -> Unit,
    onDeviceAdded: () -> Unit,
    viewModel: PairingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scaffoldState = LocalScaffoldState.current
    var showNameDialog by remember { mutableStateOf(false) }
    var deviceName by remember { mutableStateOf("") }
    
    // Запрос BLE разрешений
    val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        listOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    
    val permissionsState = rememberMultiplePermissionsState(bluetoothPermissions)
    
    // Запросить разрешения при входе на экран
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
    
    // Настройка TopBar через ScaffoldState
    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { Text("Добавить устройство") },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.onEvent(PairingEvent.NavigateBack) }) {
                                Icon(Icons.Default.ArrowBack, "Назад")
                            }
                        },
                        actions = {
                            // Иконка повтора сканирования
                            IconButton(
                                onClick = { 
                                    if (state.isScanning) {
                                        viewModel.onEvent(PairingEvent.StopScanning)
                                    } else {
                                        viewModel.onEvent(PairingEvent.StartScanning)
                                    }
                                }
                            ) {
                                if (state.isScanning) {
                                    // Анимированная иконка при сканировании
                                    val infiniteTransition = rememberInfiniteTransition(label = "topbar_scanning")
                                    val rotation by infiniteTransition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = 360f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1500, easing = LinearEasing),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "rotation"
                                    )
                                    Icon(
                                        Icons.Default.BluetoothSearching,
                                        contentDescription = "Остановить сканирование",
                                        modifier = Modifier.rotate(rotation)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Начать сканирование"
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                floatingActionButton = {}
            )
        }
    }
    
    // Автоматический запуск сканирования только после получения разрешений
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            viewModel.onEvent(PairingEvent.StartScanning)
        }
    }
    
    // Обработка side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is PairingSideEffect.DeviceAdded -> onDeviceAdded()
                is PairingSideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }
    
    // Контент без вложенного Scaffold
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Показываем UI запроса разрешений, если они не предоставлены
        if (!permissionsState.allPermissionsGranted) {
            PermissionsRationaleCard(
                onRequestPermissions = { permissionsState.launchMultiplePermissionRequest() },
                shouldShowRationale = permissionsState.shouldShowRationale
            )
            return@Column
        }
        
        // NFC подсказка с анимацией
        AnimatedVisibility(
            visible = state.showNfcHint,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
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
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Приложите устройство к телефону для быстрого подключения через NFC",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Статус сканирования (только во время активного поиска)
        AnimatedVisibility(
            visible = state.isScanning && state.foundDevices.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "scanning_pulse")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Icon(
                        Icons.Default.BluetoothSearching,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Найдено: ${state.foundDevices.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Заголовок списка
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Найденные устройства",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (state.foundDevices.isNotEmpty()) {
                Text(
                    "${state.foundDevices.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Список найденных устройств с анимацией
        AnimatedContent(
            targetState = state.foundDevices.isEmpty(),
            transitionSpec = {
                fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically()
            },
            label = "devices_list_animation"
        ) { isEmpty ->
            if (isEmpty) {
                EmptyDevicesState(
                    isScanning = state.isScanning
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = state.foundDevices,
                            key = { it.bleAddress }
                        ) { device ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
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
                    
                    // Прыгающие точки во время активного сканирования
                    AnimatedVisibility(
                        visible = state.isScanning,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            ScanningDotsIndicator()
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        
        // Ошибка с анимацией
        AnimatedVisibility(
            visible = state.error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            state.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            error.toString(),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = { viewModel.onEvent(PairingEvent.DismissError) }) {
                            Text("Закрыть")
                        }
                    }
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
    device: ScannedAmulet,
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
                    device.deviceName,
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

/**
 * Состояние пустого списка с подсказками
 */
@Composable
private fun EmptyDevicesState(isScanning: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isScanning) {
            // Круговой индикатор при активном сканировании
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                strokeWidth = 4.dp
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Поиск устройств",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Убедитесь, что устройство Amulet включено и находится рядом",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        } else {
            // Состояние когда сканирование завершено, но ничего не найдено
            Icon(
                Icons.Default.BluetoothSearching,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Устройства не найдены",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Нажмите на иконку обновления в верхнем правом углу для повторного сканирования",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Карточка с объяснением зачем нужны BLE разрешения
 */
@Composable
private fun PermissionsRationaleCard(
    onRequestPermissions: () -> Unit,
    shouldShowRationale: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Default.Bluetooth,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            "Требуются разрешения Bluetooth",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            if (shouldShowRationale) {
                "Для поиска и подключения к устройству Amulet необходим доступ к Bluetooth"
            } else {
                "Приложению нужен доступ к Bluetooth для поиска ближайших устройств Amulet"
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Button(
            onClick = onRequestPermissions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Предоставить доступ")
        }
    }
}

/**
 * Анимированные прыгающие точки для индикации продолжающегося сканирования
 */
@Composable
private fun ScanningDotsIndicator() {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "dot_$index")
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600,
                        delayMillis = index * 200,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offset_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(y = offsetY.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
}
