package com.example.amulet.feature.patterns.presentation.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import com.example.amulet.shared.domain.patterns.model.PatternKind
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PatternEditorRoute(
    viewModel: PatternEditorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPreview: () -> Unit
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
                is PatternEditorSideEffect.NavigateToPreview -> onNavigateToPreview()
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PatternEditorScreen(
    state: PatternEditorState,
    onEvent: (PatternEditorEvent) -> Unit,
    onNavigateBack: () -> Unit,
    showElementPicker: Boolean,
    onDismissElementPicker: () -> Unit
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

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { 
                            Text(
                                stringResource(
                                    if (state.isEditing) R.string.pattern_editor_title_edit 
                                    else R.string.pattern_editor_title_new
                                )
                            ) 
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
                            IconButton(onClick = { onEvent(PatternEditorEvent.PreviewPattern) }) {
                                Icon(
                                    Icons.Default.PlayArrow, 
                                    contentDescription = stringResource(R.string.cd_preview_pattern)
                                )
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
                                    Icon(
                                        Icons.Default.Check, 
                                        contentDescription = stringResource(R.string.cd_save_pattern)
                                    )
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
                ElevatedCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AmuletTextField(
                            value = state.title,
                            onValueChange = { onEvent(PatternEditorEvent.UpdateTitle(it)) },
                            label = stringResource(R.string.pattern_editor_title_label),
                            placeholder = stringResource(R.string.pattern_editor_title_hint),
                            modifier = Modifier.fillMaxWidth(),
                            errorText = state.validationErrors["title"],
                            singleLine = true
                        )

                        AmuletTextField(
                            value = state.description,
                            onValueChange = { onEvent(PatternEditorEvent.UpdateDescription(it)) },
                            label = stringResource(R.string.pattern_editor_description_label),
                            placeholder = stringResource(R.string.pattern_editor_description_hint),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false
                        )

                        // Тип паттерна
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                stringResource(R.string.pattern_editor_kind_label),
                                style = MaterialTheme.typography.titleSmall
                            )
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
                        }

                        // Зацикливание
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    stringResource(R.string.pattern_editor_loop_label),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    stringResource(R.string.pattern_editor_loop_description),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                        stringResource(R.string.pattern_editor_elements_header) + " (${state.elements.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    FilledTonalButton(
                        onClick = { onEvent(PatternEditorEvent.ShowElementPicker) }
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = stringResource(R.string.cd_add_element)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.pattern_editor_add_element))
                    }
                }
            }

            itemsIndexed(
                items = state.elements,
                key = { index, element -> "$index-${element.hashCode()}" }
            ) { index, element ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    ) + scaleIn(
                        initialScale = 0.9f,
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    ),
                    exit = fadeOut(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = FastOutSlowInEasing
                        )
                    ) + scaleOut(
                        targetScale = 0.9f,
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = FastOutSlowInEasing
                        )
                    ),
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
                ) {
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
            }

            if (state.elements.isEmpty()) {
                item {
                    EmptyElementsState()
                }
            }
        }
    }
}

@Composable
private fun EmptyElementsState() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
