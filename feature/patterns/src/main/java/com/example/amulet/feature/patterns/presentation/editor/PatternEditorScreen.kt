package com.example.amulet.feature.patterns.presentation.editor

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.textfield.AmuletTextField
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.patterns.R
import com.example.amulet.feature.patterns.presentation.components.PatternElementEditor
import com.example.amulet.feature.patterns.presentation.components.PatternElementPickerDialog
import com.example.amulet.feature.patterns.presentation.components.PublishPatternData
import com.example.amulet.feature.patterns.presentation.components.PublishPatternDialog
import com.example.amulet.feature.patterns.presentation.components.AmuletAvatar2D
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import com.example.amulet.shared.domain.patterns.model.PatternKind
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PatternEditorRoute(
    viewModel: PatternEditorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPreview: (com.example.amulet.shared.domain.patterns.model.PatternSpec) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showElementPicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showPublishDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is PatternEditorSideEffect.NavigateBack -> onNavigateBack()
                is PatternEditorSideEffect.NavigateToPreview -> {
                    // Переход на полноэкранное превью (для отправки на устройство)
                    onNavigateToPreview(effect.spec)
                }
                is PatternEditorSideEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is PatternEditorSideEffect.ShowDiscardConfirmation -> {
                    showDiscardDialog = true
                }
                is PatternEditorSideEffect.ShowPublishDialog -> {
                    showPublishDialog = true
                }
                is PatternEditorSideEffect.ShowElementPicker -> {
                    showElementPicker = true
                }
            }
        }
    }

    PatternEditorScreen(
        state = uiState,
        onEvent = viewModel::handleEvent,
        snackbarHostState = snackbarHostState,
        showElementPicker = showElementPicker,
        onDismissElementPicker = { showElementPicker = false },
        showDiscardDialog = showDiscardDialog,
        onDismissDiscardDialog = { showDiscardDialog = false },
        showPublishDialog = showPublishDialog,
        onDismissPublishDialog = { showPublishDialog = false }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun PatternEditorScreen(
    state: PatternEditorState,
    onEvent: (PatternEditorEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    showElementPicker: Boolean,
    onDismissElementPicker: () -> Unit,
    showDiscardDialog: Boolean,
    onDismissDiscardDialog: () -> Unit,
    showPublishDialog: Boolean,
    onDismissPublishDialog: () -> Unit
) {
    val scaffoldState = LocalScaffoldState.current

    // Element Picker Dialog
    if (showElementPicker) {
        PatternElementPickerDialog(
            onDismiss = onDismissElementPicker,
            onElementTypeSelected = { elementType ->
                onEvent(PatternEditorEvent.AddElement(elementType.createDefaultElement()))
                onDismissElementPicker()
            }
        )
    }

    // Discard Changes Confirmation Dialog
    if (showDiscardDialog) {
        DiscardChangesDialog(
            onSave = {
                onEvent(PatternEditorEvent.SavePattern)
                onDismissDiscardDialog()
            },
            onDiscard = {
                onEvent(PatternEditorEvent.ConfirmDiscard)
                onDismissDiscardDialog()
            },
            onCancel = onDismissDiscardDialog
        )
    }

    // Publish Pattern Dialog
    if (showPublishDialog) {
        PublishPatternDialog(
            initialData = PublishPatternData(
                publicTitle = state.title,
                publicDescription = state.description
            ),
            onConfirm = { data ->
                onEvent(PatternEditorEvent.ConfirmPublish(data))
                onDismissPublishDialog()
            },
            onDismiss = onDismissPublishDialog
        )
    }

    // Error Snackbar
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error.toString(),
                duration = SnackbarDuration.Long
            )
            onEvent(PatternEditorEvent.DismissError)
        }
    }

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { 
                            Column {
                                Text(
                                    stringResource(
                                        if (state.isEditing) R.string.pattern_editor_title_edit 
                                        else R.string.pattern_editor_title_new
                                    )
                                )
                                if (state.hasUnsavedChanges) {
                                    Text(
                                        stringResource(R.string.pattern_editor_unsaved_indicator),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { onEvent(PatternEditorEvent.DiscardChanges) }) {
                                Icon(
                                    Icons.Default.Close, 
                                    contentDescription = stringResource(R.string.cd_close_editor)
                                )
                            }
                        },
                        actions = {
                            // Кнопка отправки на устройство
                            IconButton(
                                onClick = { onEvent(PatternEditorEvent.SendToDevice) },
                                enabled = state.spec != null && !state.isSaving
                            ) {
                                Icon(
                                    Icons.Default.Send, 
                                    contentDescription = stringResource(R.string.cd_send_to_device)
                                )
                            }
                            
                            IconButton(
                                onClick = { onEvent(PatternEditorEvent.SavePattern) },
                                enabled = !state.isSaving && state.hasUnsavedChanges
                            ) {
                                if (state.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Check, 
                                        contentDescription = stringResource(R.string.cd_save_pattern)
                                    )
                                }
                            }
                        }
                    )
                },
                floatingActionButton = {},
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                }
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
            item(key = "basic_info") {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title - компактный
                        Column {
                            AmuletTextField(
                                value = state.title,
                                onValueChange = { 
                                    if (it.length <= 100) {
                                        onEvent(PatternEditorEvent.UpdateTitle(it))
                                    }
                                },
                                label = stringResource(R.string.pattern_editor_title_label),
                                placeholder = stringResource(R.string.pattern_editor_title_hint),
                                modifier = Modifier.fillMaxWidth(),
                                errorText = state.validationErrors["title"],
                                singleLine = true
                            )
                            Text(
                                text = "${state.title.length}/100",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.title.length > 80) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }

                        // Description - компактный (2 строки вместо 3)
                        Column {
                            AmuletTextField(
                                value = state.description,
                                onValueChange = { 
                                    if (it.length <= 500) {
                                        onEvent(PatternEditorEvent.UpdateDescription(it))
                                    }
                                },
                                label = stringResource(R.string.pattern_editor_description_label),
                                placeholder = stringResource(R.string.pattern_editor_description_hint),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = false,
                                minLines = 2
                            )
                            Text(
                                text = "${state.description.length}/500",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.description.length > 450) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                    }
                }
            }

            // Настройки паттерна
            item(key = "settings") {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Тип паттерна - компактный заголовок
                        Text(
                            stringResource(R.string.pattern_editor_kind_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PatternKind.entries.forEach { kind ->
                                FilterChip(
                                    selected = state.kind == kind,
                                    onClick = { onEvent(PatternEditorEvent.UpdateKind(kind)) },
                                    label = {
                                        Text(
                                            stringResource(
                                                when (kind) {
                                                    PatternKind.LIGHT -> R.string.pattern_kind_light
                                                    PatternKind.HAPTIC -> R.string.pattern_kind_haptic
                                                    PatternKind.COMBO -> R.string.pattern_kind_combo
                                                }
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        HorizontalDivider()

                        // Зацикливание - компактная версия
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.pattern_editor_loop_label),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = state.loop,
                                onCheckedChange = { onEvent(PatternEditorEvent.UpdateLoop(it)) }
                            )
                        }
                    }
                }
            }

            // Заголовок элементов
            item(key = "elements_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.pattern_editor_elements_header) + ": ${state.elements.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { onEvent(PatternEditorEvent.ShowElementPicker) },
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Elements list
            if (state.elements.isEmpty()) {
                item(key = "empty_elements") {
                    EmptyElementsState(
                        onAddElement = { onEvent(PatternEditorEvent.ShowElementPicker) }
                    )
                }
            } else {
                itemsIndexed(
                    items = state.elements,
                    key = { index, element -> "$index-${element.hashCode()}" }
                ) { index, element ->
                    PatternElementEditor(
                        element = element,
                        index = index,
                        spec = state.spec,
                        isPlaying = state.isPlaying,
                        loop = state.previewLoop,
                        onPlayPause = { onEvent(PatternEditorEvent.TogglePlayPause) },
                        onToggleLoop = { onEvent(PatternEditorEvent.ToggleLoop) },
                        onUpdate = { onEvent(PatternEditorEvent.UpdateElement(index, it)) },
                        onRemove = { onEvent(PatternEditorEvent.RemoveElement(index)) },
                        onMoveUp = if (index > 0) {
                            { onEvent(PatternEditorEvent.MoveElement(index, index - 1)) }
                        } else null,
                        onMoveDown = if (index < state.elements.size - 1) {
                            { onEvent(PatternEditorEvent.MoveElement(index, index + 1)) }
                        } else null,
                        onAddElement = { elementType ->
                            onEvent(PatternEditorEvent.AddElement(elementType.createDefaultElement()))
                        },
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            ),
                            fadeOutSpec = tween(
                                durationMillis = 200,
                                easing = FastOutSlowInEasing
                            ),
                            placementSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        )
                    )
                }
            }

            // Bottom spacing
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun EmptyElementsState(
    onAddElement: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                stringResource(R.string.empty_elements_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                stringResource(R.string.empty_elements_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
            )
            Button(
                onClick = onAddElement,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.pattern_editor_add_element))
            }
        }
    }
}

@Composable
private fun DiscardChangesDialog(
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(stringResource(R.string.dialog_discard_changes_title))
        },
        text = {
            Text(stringResource(R.string.dialog_discard_changes_message))
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text(stringResource(R.string.dialog_discard_changes_save))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.dialog_discard_changes_cancel))
                }
                TextButton(
                    onClick = onDiscard,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.dialog_discard_changes_discard))
                }
            }
        }
    )
}
