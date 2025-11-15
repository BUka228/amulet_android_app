package com.example.amulet.feature.patterns.presentation.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.amulet.feature.patterns.R
import com.example.amulet.feature.patterns.presentation.components.CompactLivePreview
import com.example.amulet.shared.domain.patterns.model.*
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.patterns.presentation.editor.editors.BreathingEditor
import com.example.amulet.feature.patterns.presentation.editor.editors.PulseEditor
import com.example.amulet.feature.patterns.presentation.editor.editors.ChaseEditor
import com.example.amulet.feature.patterns.presentation.editor.editors.FillEditor
import com.example.amulet.feature.patterns.presentation.editor.editors.SpinnerEditor
import com.example.amulet.feature.patterns.presentation.editor.editors.ProgressEditor
import com.example.amulet.feature.patterns.presentation.editor.editors.SequenceEditor
import com.example.amulet.feature.patterns.presentation.editor.editors.TimelineEditor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternElementEditorRoute(
    index: Int,
    viewModel: PatternEditorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    var localElement by remember(uiState.elements, index) {
        mutableStateOf(uiState.elements.getOrNull(index))
    }

    // Обновляем локальную копию при смене элемента в состоянии
    LaunchedEffect(uiState.elements, index) {
        localElement = uiState.elements.getOrNull(index)
    }

    val scaffoldState = LocalScaffoldState.current

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = localElement?.let { getElementName(it) } ?: stringResource(R.string.pattern_editor_elements_header)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null)
                            }
                        }
                    )
                },
                floatingActionButton = {},
                snackbarHost = {}
            )
        }
    }

    if (localElement == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.empty_elements_title))
        }
        return
    }

    var current by remember(localElement) { mutableStateOf(localElement!!) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Компактное превью только с текущим элементом
        val previewSpec = uiState.spec?.copy(elements = listOf(current))
        CompactLivePreview(
            spec = previewSpec,
            isPlaying = uiState.isPlaying,
            loop = uiState.previewLoop,
            onPlayPause = { viewModel.handleEvent(PatternEditorEvent.TogglePlayPause) },
            onToggleLoop = { viewModel.handleEvent(PatternEditorEvent.ToggleLoop) },
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        // Зона редакторов с вертикальной прокруткой
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Редакторы по типам
            when (current) {
                is PatternElementBreathing -> BreathingEditor(current as PatternElementBreathing) { updated ->
                    current = updated as PatternElementBreathing
                    viewModel.handleEvent(PatternEditorEvent.UpdateElement(index, current))
                }
                is PatternElementPulse -> PulseEditor(current as PatternElementPulse) { updated ->
                    current = updated as PatternElementPulse
                    viewModel.handleEvent(PatternEditorEvent.UpdateElement(index, current))
                }
                is PatternElementChase -> ChaseEditor(current as PatternElementChase) { updated ->
                    current = updated as PatternElementChase
                    viewModel.handleEvent(PatternEditorEvent.UpdateElement(index, current))
                }
                is PatternElementFill -> FillEditor(current as PatternElementFill) { updated ->
                    current = updated as PatternElementFill
                    viewModel.handleEvent(PatternEditorEvent.UpdateElement(index, current))
                }
                is PatternElementSpinner -> SpinnerEditor(current as PatternElementSpinner) { updated ->
                    current = updated as PatternElementSpinner
                    viewModel.handleEvent(PatternEditorEvent.UpdateElement(index, current))
                }
                is PatternElementProgress -> ProgressEditor(current as PatternElementProgress) { updated ->
                    current = updated as PatternElementProgress
                    viewModel.handleEvent(PatternEditorEvent.UpdateElement(index, current))
                }
                is PatternElementSequence -> SequenceEditor(current as PatternElementSequence) { updated ->
                    current = updated as PatternElementSequence
                    viewModel.handleEvent(PatternEditorEvent.UpdateElement(index, current))
                }
                is PatternElementTimeline -> TimelineEditor(current as PatternElementTimeline) { updated ->
                    current = updated as PatternElementTimeline
                    viewModel.handleEvent(PatternEditorEvent.UpdateElement(index, current))
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun getElementName(element: PatternElement) = when (element) {
    is PatternElementBreathing -> stringResource(R.string.pattern_element_breathing)
    is PatternElementPulse -> stringResource(R.string.pattern_element_pulse)
    is PatternElementChase -> stringResource(R.string.pattern_element_chase)
    is PatternElementFill -> stringResource(R.string.pattern_element_fill)
    is PatternElementSpinner -> stringResource(R.string.pattern_element_spinner)
    is PatternElementProgress -> stringResource(R.string.pattern_element_progress)
    is PatternElementSequence -> stringResource(R.string.pattern_element_sequence)
    is PatternElementTimeline -> stringResource(R.string.pattern_element_timeline)
}
