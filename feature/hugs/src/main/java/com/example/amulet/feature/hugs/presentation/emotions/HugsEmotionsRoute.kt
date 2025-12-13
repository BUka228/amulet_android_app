package com.example.amulet.feature.hugs.presentation.emotions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.hugs.R
import com.example.amulet.shared.core.AppError
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HugsEmotionsRoute(
    onNavigateBack: () -> Unit = {},
    onOpenEmotionEditor: (String?) -> Unit = {},
    viewModel: HugsEmotionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scaffoldState = LocalScaffoldState.current

    val selectionCount = state.selectedEmotionIds.size
    val isSelectionMode = selectionCount > 0

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (isSelectionMode) selectionCount.toString() else stringResource(R.string.hugs_emotions_title)
                            )
                        },
                        navigationIcon = {
                            if (isSelectionMode) {
                                IconButton(onClick = { viewModel.onIntent(HugsEmotionsIntent.ClearSelection) }) {
                                    Icon(imageVector = Icons.Filled.Close, contentDescription = null)
                                }
                            } else {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        actions = {
                            if (isSelectionMode) {
                                IconButton(
                                    onClick = { viewModel.onIntent(HugsEmotionsIntent.DeleteSelected) },
                                    enabled = !state.isDeleting,
                                ) {
                                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors()
                    )
                },
                floatingActionButton = {
                    if (!isSelectionMode) {
                        FloatingActionButton(
                            onClick = { onOpenEmotionEditor(null) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                        }
                    }
                }
            )
        }
    }

    HugsEmotionsScreen(
        state = state,
        onOpenEmotionEditor = onOpenEmotionEditor,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun HugsEmotionsScreen(
    state: HugsEmotionsState,
    onOpenEmotionEditor: (String?) -> Unit,
    onIntent: (HugsEmotionsIntent) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val isSelectionMode = state.selectedEmotionIds.isNotEmpty()
    val isEmpty = !state.isLoading && state.emotions.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isEmpty) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Text(
                        text = stringResource(R.string.hugs_emotions_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.hugs_emotions_empty_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    Button(onClick = { onOpenEmotionEditor(null) }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = stringResource(R.string.hugs_emotions_empty_cta))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.emotions, key = { it.id }) { emotion ->
                    val isSelected = state.selectedEmotionIds.contains(emotion.id)
                    val patternTitle = emotion.patternId?.let { pid ->
                        state.patternTitles[pid.value] ?: pid.value
                    }

                    EmotionItem(
                        emotion = emotion,
                        isSelected = isSelected,
                        patternTitle = patternTitle,
                        onClick = {
                            if (isSelectionMode) {
                                onIntent(HugsEmotionsIntent.ToggleSelection(emotion.id))
                            } else {
                                onOpenEmotionEditor(emotion.id)
                            }
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onIntent(HugsEmotionsIntent.ToggleSelection(emotion.id))
                        },
                    )
                }
            }
        }

        state.error?.let { error ->
            EmotionsErrorCard(error = error)
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun EmotionItem(
    emotion: com.example.amulet.shared.domain.hugs.model.PairEmotion,
    isSelected: Boolean,
    patternTitle: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val color = runCatching { Color(emotion.colorHex.toColorInt()) }
        .getOrElse { MaterialTheme.colorScheme.primary }

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val border = if (isSelected) {
        BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    AmuletCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        backgroundColor = backgroundColor,
        border = border,
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
                patternTitle?.let {
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
                            text = stringResource(R.string.hugs_emotions_pattern_prefix, it),
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
