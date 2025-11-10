package com.example.amulet.feature.patterns.presentation.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.amulet.feature.patterns.presentation.components.PatternElementEditor
import com.example.amulet.shared.domain.patterns.model.PatternKind
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PatternEditorRoute(
    viewModel: PatternEditorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPreview: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is PatternEditorSideEffect.NavigateBack -> onNavigateBack()
                is PatternEditorSideEffect.NavigateToPreview -> onNavigateToPreview()
                is PatternEditorSideEffect.ShowSnackbar -> {
                    // Handle snackbar
                }
                is PatternEditorSideEffect.ShowDiscardConfirmation -> {
                    // Handle confirmation dialog
                }
                is PatternEditorSideEffect.ShowPublishDialog -> {
                    // Handle publish dialog
                }
                is PatternEditorSideEffect.ShowElementPicker -> {
                    // Handle element picker
                }
            }
        }
    }

    PatternEditorScreen(
        state = uiState,
        onEvent = viewModel::handleEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternEditorScreen(
    state: PatternEditorState,
    onEvent: (PatternEditorEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scaffoldState = LocalScaffoldState.current

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { Text(if (state.isEditing) "Редактор паттерна" else "Новый паттерн") },
                        navigationIcon = {
                            IconButton(onClick = { onEvent(PatternEditorEvent.DiscardChanges) }) {
                                Icon(Icons.Default.Close, contentDescription = "Закрыть")
                            }
                        },
                        actions = {
                            IconButton(onClick = { onEvent(PatternEditorEvent.PreviewPattern) }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Предпросмотр")
                            }
                            IconButton(
                                onClick = { onEvent(PatternEditorEvent.SavePattern) },
                                enabled = !state.isSaving
                            ) {
                                if (state.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = "Сохранить")
                                }
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
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Основная информация
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = state.title,
                            onValueChange = { onEvent(PatternEditorEvent.UpdateTitle(it)) },
                            label = { Text("Название") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = state.validationErrors.containsKey("title"),
                            supportingText = state.validationErrors["title"]?.let { { Text(it) } }
                        )

                        OutlinedTextField(
                            value = state.description,
                            onValueChange = { onEvent(PatternEditorEvent.UpdateDescription(it)) },
                            label = { Text("Описание (опционально)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )

                        // Тип паттерна
                        Text("Тип паттерна", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PatternKind.values().forEach { kind ->
                                FilterChip(
                                    selected = state.kind == kind,
                                    onClick = { onEvent(PatternEditorEvent.UpdateKind(kind)) },
                                    label = {
                                        Text(
                                            when (kind) {
                                                PatternKind.LIGHT -> "Свет"
                                                PatternKind.HAPTIC -> "Вибрация"
                                                PatternKind.COMBO -> "Комбо"
                                            }
                                        )
                                    }
                                )
                            }
                        }

                        // Зацикливание
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Зацикливание", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = state.loop,
                                onCheckedChange = { onEvent(PatternEditorEvent.UpdateLoop(it)) }
                            )
                        }
                    }
                }
            }

            // Список элементов
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Элементы паттерна (${state.elements.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    FilledTonalButton(
                        onClick = { onEvent(PatternEditorEvent.AddElement(createDefaultElement())) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Добавить")
                    }
                }
            }

            itemsIndexed(
                items = state.elements,
                key = { index, _ -> index }
            ) { index, element ->
                PatternElementEditor(
                    element = element,
                    index = index,
                    isSelected = state.selectedElementIndex == index,
                    onSelect = { onEvent(PatternEditorEvent.SelectElement(index)) },
                    onUpdate = { onEvent(PatternEditorEvent.UpdateElement(index, it)) },
                    onRemove = { onEvent(PatternEditorEvent.RemoveElement(index)) },
                    onMoveUp = if (index > 0) {
                        { onEvent(PatternEditorEvent.MoveElement(index, index - 1)) }
                    } else null,
                    onMoveDown = if (index < state.elements.size - 1) {
                        { onEvent(PatternEditorEvent.MoveElement(index, index + 1)) }
                    } else null
                )
            }

            if (state.elements.isEmpty()) {
                item {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Паттерн пуст",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Добавьте элементы для создания анимации",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// Вспомогательная функция для создания элемента по умолчанию
private fun createDefaultElement(): com.example.amulet.shared.domain.patterns.model.PatternElement {
    return com.example.amulet.shared.domain.patterns.model.PatternElementBreathing(
        color = "#FF0000",
        durationMs = 2000
    )
}
