package com.example.amulet.feature.practices.presentation.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.components.textfield.AmuletTextField
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.practices.R
import com.example.amulet.shared.domain.practices.model.PracticeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeEditorRoute(
    practiceId: String?,
    onNavigateBack: () -> Unit,
    viewModel: PracticeEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scaffoldState = LocalScaffoldState.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PracticeEditorEffect.NavigateBack -> onNavigateBack()
                is PracticeEditorEffect.ShowMessage -> snackbarHostState.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    androidx.compose.runtime.SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(
                                    if (state.practiceId == null) R.string.practice_editor_title_new
                                    else R.string.practice_editor_title_edit,
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.handleEvent(PracticeEditorEvent.OnBackClick) }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = null)
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { viewModel.handleEvent(PracticeEditorEvent.Save) },
                                enabled = !state.isSaving
                            ) {
                                Icon(Icons.Filled.Save, contentDescription = null)
                            }
                        }
                    )
                },
                floatingActionButton = {},
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            )
        }
    }

    if (state.isLoading) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
        }
    } else {
        PracticeEditorScreen(state = state, onEvent = viewModel::handleEvent)
    }

    if (state.isPatternSheetVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.handleEvent(PracticeEditorEvent.TogglePatternSheet) },
            sheetState = sheetState,
        ) {
            PatternPickerSheetContent(state = state, onEvent = viewModel::handleEvent)
        }
    }
}

@Composable
private fun DetailsCard(
    state: PracticeEditorState,
    onEvent: (PracticeEditorEvent) -> Unit,
) {
    val editor = state.editorPractice
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(id = R.string.practice_details_about),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AmuletTextField(
                value = editor.about,
                onValueChange = { onEvent(PracticeEditorEvent.UpdateAbout(it)) },
                label = stringResource(id = R.string.practice_details_about),
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(id = R.string.practice_details_how_it_goes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AmuletTextField(
                value = editor.howItGoes,
                onValueChange = { onEvent(PracticeEditorEvent.UpdateHowItGoes(it)) },
                label = stringResource(id = R.string.practice_details_how_it_goes),
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(id = R.string.practice_details_safety),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AmuletTextField(
                value = editor.safetyNotes,
                onValueChange = { onEvent(PracticeEditorEvent.UpdateSafetyNotes(it)) },
                label = stringResource(id = R.string.practice_details_safety),
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PracticeEditorScreen(
    state: PracticeEditorState,
    onEvent: (PracticeEditorEvent) -> Unit,
) {
    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { onEvent(PracticeEditorEvent.DismissError) },
            confirmButton = {
                TextButton(onClick = { onEvent(PracticeEditorEvent.DismissError) }) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            title = { Text(text = stringResource(id = R.string.practices_error_generic)) },
            text = { Text(text = state.error.toString()) },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item("basic") {
            BasicInfoCard(state = state, onEvent = onEvent)
        }

        item("details") {
            DetailsCard(state = state, onEvent = onEvent)
        }

        item("pattern") {
            BasePatternAndSegmentsCard(state = state, onEvent = onEvent)
        }

        item("steps_header") {
            Text(
                text = stringResource(id = R.string.practice_editor_steps_title),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        items(state.editorPractice.steps.sortedBy { it.order }, key = { it.order }) { step ->
            StepCard(
                step = step,
                isSelected = state.selectedStepOrder == step.order,
                onEvent = onEvent,
            )
        }

        item("steps_actions") {
            RowActions(onEvent = onEvent)
        }
    }
}

@Composable
private fun PatternPickerSheetContent(
    state: PracticeEditorState,
    onEvent: (PracticeEditorEvent) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val patterns = state.availablePatterns
        .filter { pattern ->
            query.isBlank() ||
                pattern.title.contains(query, ignoreCase = true) ||
                pattern.tags.any { it.contains(query, ignoreCase = true) }
        }
        .sortedBy { it.title.lowercase() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(id = R.string.practice_editor_pattern_sheet_title),
            style = MaterialTheme.typography.titleMedium,
        )

        AmuletTextField(
            value = query,
            onValueChange = { query = it },
            label = stringResource(id = R.string.practice_editor_pattern_search_hint),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        if (patterns.isEmpty()) {
            Text(
                text = stringResource(id = R.string.practice_editor_pattern_sheet_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(patterns, key = { it.id.value }) { pattern ->
                    AmuletCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onEvent(PracticeEditorEvent.SetBasePattern(pattern.id))
                            },
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = pattern.title,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            if (pattern.tags.isNotEmpty()) {
                                Text(
                                    text = pattern.tags.joinToString(separator = " · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BasicInfoCard(
    state: PracticeEditorState,
    onEvent: (PracticeEditorEvent) -> Unit,
) {
    val editor = state.editorPractice
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AmuletTextField(
                value = editor.title,
                onValueChange = { onEvent(PracticeEditorEvent.UpdateTitle(it)) },
                label = stringResource(id = R.string.practice_editor_title_label),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.practice_editor_type_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val types = PracticeType.entries
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    types.forEach { type ->
                        val selected = editor.type == type
                        FilterChip(
                            selected = selected,
                            onClick = { onEvent(PracticeEditorEvent.UpdateType(type)) },
                            label = {
                                Text(
                                    text = stringResource(
                                        when (type) {
                                            PracticeType.BREATH -> R.string.practice_type_breath
                                            PracticeType.MEDITATION -> R.string.practice_type_meditation
                                            PracticeType.SOUND -> R.string.practice_type_sound
                                        }
                                    )
                                )
                            },
                        )
                    }
                }
            }

            AmuletTextField(
                value = editor.targetDurationSec?.toString().orEmpty(),
                onValueChange = { text ->
                    val value = text.toIntOrNull()
                    onEvent(PracticeEditorEvent.UpdateTargetDuration(value))
                },
                label = stringResource(id = R.string.practice_editor_duration_label),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun BasePatternAndSegmentsCard(
    state: PracticeEditorState,
    onEvent: (PracticeEditorEvent) -> Unit,
) {
    val editor = state.editorPractice
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(id = R.string.practice_editor_base_pattern_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = run {
                    val fromSegments = state.basePatternWithSegments?.base?.title
                    val fromList = state.availablePatterns
                        .firstOrNull { it.id == editor.basePatternId }
                        ?.title
                    fromSegments
                        ?: fromList
                        ?: stringResource(id = R.string.practice_editor_base_pattern_placeholder)
                },
                style = MaterialTheme.typography.bodyMedium,
            )

            val segmentsCount = state.basePatternWithSegments?.segments?.size ?: 0
            val hasBasePattern = editor.basePatternId != null || state.basePatternWithSegments != null
            if (hasBasePattern) {
                val color = if (segmentsCount > 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
                Text(
                    text = stringResource(id = R.string.practice_editor_segments_count, segmentsCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { onEvent(PracticeEditorEvent.TogglePatternSheet) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(id = R.string.practice_editor_choose_pattern))
                }
            }
        }
    }
}

@Composable
private fun StepCard(
    step: EditorStep,
    isSelected: Boolean,
    onEvent: (PracticeEditorEvent) -> Unit,
) {
    AmuletCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEvent(PracticeEditorEvent.SelectStep(step.order)) },
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(id = R.string.practice_editor_step_title_format, step.order + 1),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                TextButton(onClick = { onEvent(PracticeEditorEvent.MergeStepWithNext(step.order)) }) {
                    Text(text = stringResource(id = R.string.practice_editor_step_merge_next))
                }
            }

            AmuletTextField(
                value = step.title,
                onValueChange = { onEvent(PracticeEditorEvent.UpdateStepTitle(step.order, it)) },
                label = stringResource(id = R.string.practice_editor_step_title_label),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            AmuletTextField(
                value = step.description,
                onValueChange = { onEvent(PracticeEditorEvent.UpdateStepDescription(step.order, it)) },
                label = stringResource(id = R.string.practice_editor_step_description_label),
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )

            AmuletTextField(
                value = step.durationSec?.toString().orEmpty(),
                onValueChange = { text ->
                    val value = text.toIntOrNull()
                    onEvent(PracticeEditorEvent.UpdateStepDuration(step.order, value))
                },
                label = stringResource(id = R.string.practice_editor_step_duration_label),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            when (val binding = step.binding) {
                StepBinding.None -> {
                    Text(
                        text = stringResource(id = R.string.practice_editor_step_binding_none),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                is StepBinding.SinglePattern -> {
                    Text(
                        text = stringResource(id = R.string.practice_editor_step_binding_single_pattern, binding.patternId.value, binding.repeatCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                is StepBinding.SegmentGroup -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(id = R.string.practice_editor_step_repeat_label, binding.repeatCount),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { onEvent(PracticeEditorEvent.UpdateSegmentGroupRepeat(step.order, (binding.repeatCount - 1).coerceAtLeast(1))) }) {
                            Icon(Icons.Filled.Remove, contentDescription = null)
                        }
                        IconButton(onClick = { onEvent(PracticeEditorEvent.UpdateSegmentGroupRepeat(step.order, binding.repeatCount + 1)) }) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { onEvent(PracticeEditorEvent.DeleteStep(step.order)) }) {
                    Text(text = stringResource(id = R.string.practice_editor_step_delete))
                }
            }
        }
    }
}

@Composable
private fun RowActions(onEvent: (PracticeEditorEvent) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { onEvent(PracticeEditorEvent.AddStep) }, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(id = R.string.practice_editor_add_step))
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
