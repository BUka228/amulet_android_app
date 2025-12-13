package com.example.amulet.feature.hugs.presentation.emotions.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.color.ColorPicker
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.components.textfield.AmuletTextField
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.hugs.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HugsEmotionEditorRoute(
    onNavigateBack: () -> Unit = {},
    onOpenPatternPicker: () -> Unit = {},
    selectedPatternId: String? = null,
    onConsumeSelectedPatternId: () -> Unit = {},
    viewModel: HugsEmotionEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scaffoldState = LocalScaffoldState.current

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { Text(text = stringResource(R.string.hugs_emotions_editor_title)) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors()
                    )
                },
                floatingActionButton = {}
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HugsEmotionEditorEffect.OpenPatternPicker -> onOpenPatternPicker()
                is HugsEmotionEditorEffect.ShowError -> {
                }
                HugsEmotionEditorEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    LaunchedEffect(selectedPatternId) {
        if (!selectedPatternId.isNullOrBlank()) {
            viewModel.onIntent(HugsEmotionEditorIntent.ChangePattern(selectedPatternId))
            onConsumeSelectedPatternId()
        }
    }

    HugsEmotionEditorScreen(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun HugsEmotionEditorScreen(
    state: HugsEmotionEditorState,
    onIntent: (HugsEmotionEditorIntent) -> Unit,
) {
    val emotion = state.emotion
    val selectedPatternTitle = state.selectedPatternTitle

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (emotion == null) return

        AmuletCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AmuletTextField(
                    value = emotion.name,
                    onValueChange = { onIntent(HugsEmotionEditorIntent.ChangeName(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.hugs_emotions_name_label),
                    singleLine = true,
                )

                ColorPicker(
                    color = emotion.colorHex,
                    onColorChange = { onIntent(HugsEmotionEditorIntent.ChangeColor(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.hugs_emotions_color_label),
                )

                if (!selectedPatternTitle.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.hugs_emotions_pattern_prefix, selectedPatternTitle),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                OutlinedButton(
                    onClick = { onIntent(HugsEmotionEditorIntent.OpenPatternPicker) },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Brush,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(R.string.hugs_emotions_select_pattern_button))
                }

                Button(
                    onClick = { onIntent(HugsEmotionEditorIntent.Save) },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(R.string.hugs_emotions_save_button))
                }
            }
        }
    }
}
