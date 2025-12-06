package com.example.amulet.feature.patterns.presentation.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.components.textfield.AmuletTextField
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.patterns.R
import com.example.amulet.feature.patterns.presentation.components.PublishPatternData
import com.example.amulet.feature.patterns.presentation.components.PublishPatternDialog
import com.example.amulet.shared.domain.patterns.model.PatternKind
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PatternEditorRoute(
    viewModel: PatternEditorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPreview: (com.example.amulet.shared.domain.patterns.model.PatternSpec) -> Unit,
    onNavigateToTimelineEditor: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
            }
        }
    }

    PatternEditorScreen(
        state = uiState,
        onEvent = viewModel::handleEvent,
        snackbarHostState = snackbarHostState,
        showDiscardDialog = showDiscardDialog,
        onDismissDiscardDialog = { showDiscardDialog = false },
        showPublishDialog = showPublishDialog,
        onDismissPublishDialog = { showPublishDialog = false },
        onOpenTimelineEditor = onNavigateToTimelineEditor,
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
    showDiscardDialog: Boolean,
    onDismissDiscardDialog: () -> Unit,
    showPublishDialog: Boolean,
    onDismissPublishDialog: () -> Unit,
    onOpenTimelineEditor: () -> Unit,
) {
    val scaffoldState = LocalScaffoldState.current

    // Add Tag Dialog (inside screen scope)
    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagText by remember { mutableStateOf("") }
    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            title = { Text(text = stringResource(R.string.dialog_add_tag_title)) },
            text = {
                AmuletTextField(
                    value = newTagText,
                    onValueChange = { newTagText = it },
                    label = stringResource(R.string.dialog_add_tag_label),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTagText.isNotBlank()) {
                        onEvent(PatternEditorEvent.AddNewTag(newTagText.trim()))
                        newTagText = ""
                        showAddTagDialog = false
                    }
                }) {
                    Text(stringResource(R.string.dialog_add_tag_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false }) {
                    Text(stringResource(R.string.dialog_add_tag_cancel))
                }
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
                                    Icons.AutoMirrored.Filled.Send,
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
                AmuletCard(
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
                AmuletCard(
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

            // Теги
            item(key = "tags") {
                AmuletCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp,  vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.pattern_editor_tags_label),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { onEvent(PatternEditorEvent.ShowTagsSheet) }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.List,
                                        contentDescription = stringResource(R.string.bottom_sheet_tags_title)
                                    )
                                }
                                IconButton(onClick = { showAddTagDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(R.string.cd_add_tag)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (state.selectedTags.isNotEmpty()) {
                                            onEvent(PatternEditorEvent.SetPendingDeleteTags(state.selectedTags))
                                        }
                                    },
                                    enabled = state.selectedTags.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.cd_delete_selected_tags)
                                    )
                                }
                            }
                        }

                        val visibleLimit = 8
                        val sortedTags = remember(state.availableTags) { state.availableTags.sorted() }
                        val visible = sortedTags.take(visibleLimit)
                        val overflowCount = (sortedTags.size - visible.size).coerceAtLeast(0)

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            visible.forEach { tag ->
                                FilterChip(
                                    selected = state.selectedTags.contains(tag),
                                    onClick = { onEvent(PatternEditorEvent.ToggleTag(tag)) },
                                    label = { Text(tag) }
                                )
                            }
                            if (overflowCount > 0) {
                                FilterChip(
                                    selected = false,
                                    onClick = { onEvent(PatternEditorEvent.ShowTagsSheet) },
                                    label = { Text("+${overflowCount}") }
                                )
                            }
                        }
                    }
                }
            }

            // Таймлайн
            item(key = "timeline") {
                val timeline = state.timeline
                if (timeline != null) {
                    AmuletCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.pattern_element_timeline),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                IconButton(onClick = onOpenTimelineEditor) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = stringResource(R.string.pattern_editor_element_expand)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    AmuletCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.pattern_element_timeline),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(R.string.empty_elements_title),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Bottom spacing
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Delete selected tags confirmation
    if (state.pendingDeleteTags.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { onEvent(PatternEditorEvent.SetPendingDeleteTags(emptySet())) },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.dialog_delete_tags_title)) },
            text = {
                Text(stringResource(R.string.dialog_delete_tags_message, state.pendingDeleteTags.size))
            },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(PatternEditorEvent.DeleteSelectedTags)
                }) { Text(stringResource(R.string.dialog_delete_tags_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(PatternEditorEvent.SetPendingDeleteTags(emptySet())) }) {
                    Text(stringResource(R.string.dialog_delete_tags_cancel))
                }
            }
        )
    }

    // Bottom sheet with all tags
    if (state.showTagsSheet) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(PatternEditorEvent.HideTagsSheet) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.bottom_sheet_tags_title),
                    style = MaterialTheme.typography.titleMedium
                )
                AmuletTextField(
                    value = state.tagSearchQuery,
                    onValueChange = { onEvent(PatternEditorEvent.UpdateTagSearch(it)) },
                    label = stringResource(R.string.bottom_sheet_tags_search_label),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                val filtered = remember(state.availableTags, state.tagSearchQuery) {
                    val q = state.tagSearchQuery.trim().lowercase()
                    if (q.isEmpty()) state.availableTags else state.availableTags.filter { it.lowercase().contains(q) }
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filtered.forEach { tag ->
                        FilterChip(
                            selected = state.selectedTags.contains(tag),
                            onClick = { onEvent(PatternEditorEvent.ToggleTag(tag)) },
                            label = { Text(tag) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onEvent(PatternEditorEvent.HideTagsSheet) }) {
                        Text(stringResource(R.string.bottom_sheet_tags_close))
                    }
                }
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
