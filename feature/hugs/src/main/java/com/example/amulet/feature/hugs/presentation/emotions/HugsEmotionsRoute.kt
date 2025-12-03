package com.example.amulet.feature.hugs.presentation.emotions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.hugs.R
import com.example.amulet.shared.core.AppError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HugsEmotionsRoute(
    onNavigateBack: () -> Unit = {},
    onOpenPatternEditor: (String?) -> Unit = {},
    viewModel: HugsEmotionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scaffoldState = LocalScaffoldState.current

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { Text(text = stringResource(R.string.hugs_emotions_title)) },
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
                is HugsEmotionsEffect.ShowError -> {
                    // пока просто оставляем обработку на карточку ошибки внизу экрана
                }
                is HugsEmotionsEffect.OpenPatternEditor -> {
                    onOpenPatternEditor(effect.patternId)
                }
            }
        }
    }

    HugsEmotionsScreen(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun HugsEmotionsScreen(
    state: HugsEmotionsState,
    onIntent: (HugsEmotionsIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Заголовок палитры с иконкой
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = stringResource(R.string.hugs_emotions_palette_title),
                style = MaterialTheme.typography.titleMedium
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.emotions, key = { it.id }) { emotion ->
                EmotionItem(emotion = emotion) {
                    onIntent(HugsEmotionsIntent.EditEmotion(emotion.id))
                }
            }
        }

        state.editingEmotion?.let { editing ->
            EmotionEditorCard(
                state = state,
                emotion = editing,
                onIntent = onIntent,
            )
        }

        state.error?.let { error ->
            EmotionsErrorCard(error = error)
        }
    }
}

@Composable
private fun EmotionItem(
    emotion: com.example.amulet.shared.domain.hugs.model.PairEmotion,
    onClick: () -> Unit,
) {
    val color = runCatching { Color(android.graphics.Color.parseColor(emotion.colorHex)) }
        .getOrElse { MaterialTheme.colorScheme.primary }

    AmuletCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка палитры с цветовым акцентом
            Icon(
                imageVector = Icons.Filled.Palette,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )

            // Информация об эмоции
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = emotion.name, style = MaterialTheme.typography.bodyLarge)
                emotion.patternId?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Brush,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.hugs_emotions_pattern_prefix, it.value),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Большой цветной круг справа
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        }
    }
}

@Composable
private fun EmotionEditorCard(
    state: HugsEmotionsState,
    emotion: com.example.amulet.shared.domain.hugs.model.PairEmotion,
    onIntent: (HugsEmotionsIntent) -> Unit,
) {
    val previewColor = runCatching { Color(android.graphics.Color.parseColor(emotion.colorHex)) }
        .getOrElse { MaterialTheme.colorScheme.primary }

    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Заголовок с иконкой
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.hugs_emotions_editor_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Большое превью цвета
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(previewColor)
                    .border(3.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                value = emotion.name,
                onValueChange = { onIntent(HugsEmotionsIntent.ChangeEditingName(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.hugs_emotions_name_label)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Label,
                        contentDescription = null
                    )
                }
            )

            OutlinedTextField(
                value = emotion.colorHex,
                onValueChange = { onIntent(HugsEmotionsIntent.ChangeEditingColor(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.hugs_emotions_color_label)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = null
                    )
                }
            )

            OutlinedButton(
                onClick = { onIntent(HugsEmotionsIntent.OpenPatternEditor(emotion.patternId?.value)) },
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

            // Кнопки действий
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onIntent(HugsEmotionsIntent.SaveEditing) },
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(R.string.hugs_emotions_save_button))
                }

                TextButton(
                    onClick = { onIntent(HugsEmotionsIntent.CancelEdit) },
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(R.string.hugs_emotions_cancel_button))
                }
            }
        }
    }
}

@Composable
private fun EmotionsErrorCard(error: AppError) {
    AmuletCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.hugs_emotions_error_saving),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = error.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
