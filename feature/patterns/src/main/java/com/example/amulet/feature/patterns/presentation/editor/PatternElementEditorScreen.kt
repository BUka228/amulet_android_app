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
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.patterns.R
import com.example.amulet.feature.patterns.presentation.components.CompactLivePreview
import com.example.amulet.feature.patterns.presentation.editor.editors.TimelineEditor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternElementEditorRoute(
    viewModel: PatternEditorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scaffoldState = LocalScaffoldState.current

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.pattern_element_timeline)
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
    val timeline = uiState.timeline

    if (timeline == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.empty_elements_title))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CompactLivePreview(
            spec = uiState.spec,
            isPlaying = uiState.isPlaying,
            loop = uiState.previewLoop,
            onPlayPause = { viewModel.handleEvent(PatternEditorEvent.TogglePlayPause) },
            onToggleLoop = { viewModel.handleEvent(PatternEditorEvent.ToggleLoop) },
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        // Полноэкранный редактор таймлайна
        TimelineEditor(
            timeline = timeline,
            tickMs = uiState.timelineTickMs,
            markersMs = uiState.markersMs,
            onMarkersChange = { markers ->
                viewModel.handleEvent(PatternEditorEvent.UpdateMarkers(markers))
            },
            onUpdate = { updated ->
                viewModel.handleEvent(PatternEditorEvent.UpdateTimeline(updated))
            }
        )
    }
}
