package com.example.amulet.feature.patterns.presentation.preview

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.patterns.presentation.components.PatternVisualizer
import com.example.amulet.shared.domain.patterns.usecase.PreviewProgress
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PatternPreviewRoute(
    viewModel: PatternPreviewViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is PatternPreviewSideEffect.ShowSnackbar -> {
                    // Handle snackbar
                }
                is PatternPreviewSideEffect.ShowDeviceRequired -> {
                    // Handle device required dialog
                }
                is PatternPreviewSideEffect.ShowBleConnectionError -> {
                    // Handle BLE error
                }
                is PatternPreviewSideEffect.NavigateToDeviceSelection -> {
                    // Navigate to device selection
                }
            }
        }
    }

    PatternPreviewScreen(
        state = uiState,
        onEvent = viewModel::handleEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternPreviewScreen(
    state: PatternPreviewState,
    onEvent: (PatternPreviewEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scaffoldState = LocalScaffoldState.current

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { Text("Предпросмотр паттерна") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                            }
                        }
                    )
                },
                floatingActionButton = {}
            )
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.pattern == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Паттерн не найден",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Визуализатор паттерна
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PatternVisualizer(
                        pattern = state.pattern,
                        modifier = Modifier.size(200.dp),
                        isAnimated = state.isPlaying
                    )
                }
            }

            // Информация о паттерне
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = state.pattern.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    state.pattern.description?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Элементов:", style = MaterialTheme.typography.bodyMedium)
                        Text("${state.pattern.spec.elements.size}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Зацикливание:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (state.pattern.spec.loop) "Да" else "Нет",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Режим предпросмотра
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Режим предпросмотра",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Switch(
                            checked = !state.localPreview,
                            onCheckedChange = { onEvent(PatternPreviewEvent.TogglePreviewMode) }
                        )
                    }

                    Text(
                        if (state.localPreview) "Локальная визуализация" else "На устройстве",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Выбор устройства для предпросмотра
                    if (!state.localPreview) {
                        if (state.devices.isEmpty()) {
                            Text(
                                "Нет подключенных устройств",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            var expanded by remember { mutableStateOf(false) }
                            
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = state.selectedDevice?.name ?: "Выберите устройство",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Устройство") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    state.devices.forEach { device ->
                                        DropdownMenuItem(
                                            text = { Text(device.name ?: device.id.value) },
                                            onClick = {
                                                onEvent(PatternPreviewEvent.SelectDevice(device.id.value))
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Прогресс отправки на устройство
            state.progress?.let { progress ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            when (progress) {
                                is PreviewProgress.Compiling -> "Компиляция паттерна..."
                                is PreviewProgress.Uploading -> "Отправка на устройство... ${progress.percent}%"
                                is PreviewProgress.Playing -> "Воспроизведение"
                                is PreviewProgress.Failed -> "Ошибка"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        if (progress is PreviewProgress.Uploading) {
                            LinearProgressIndicator(
                                progress = progress.percent / 100f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Кнопки управления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!state.isPlaying) {
                    Button(
                        onClick = { onEvent(PatternPreviewEvent.PlayPattern) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Воспроизвести")
                    }
                } else {
                    if (state.isPaused) {
                        FilledTonalButton(
                            onClick = { onEvent(PatternPreviewEvent.PlayPattern) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Продолжить")
                        }
                    } else {
                        FilledTonalButton(
                            onClick = { onEvent(PatternPreviewEvent.PausePattern) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Пауза")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = { onEvent(PatternPreviewEvent.StopPattern) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Стоп")
                    }
                }
            }
        }
    }
