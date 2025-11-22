package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.components.card.CardElevation
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PatternCard(
    modifier: Modifier = Modifier,
    pattern: Pattern,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: (() -> Unit)? = null,
    onShowDetails: () -> Unit = {},
    onTagClick: (String) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDeleteConfirm = true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEdit()
                }
                else -> {}
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val bgColor = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.errorContainer
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(bgColor)
            ) {
                // Delete (right swipe)
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = stringResource(R.string.pattern_card_delete),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Edit (left swipe)
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.pattern_card_edit),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    ) {
        AmuletCard(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = CardElevation.Low
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()

        ) {
            // Основная информация (всегда видна)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Иконка типа паттерна с цветом
                Icon(
                    imageVector = when (pattern.kind) {
                        PatternKind.LIGHT -> Icons.Default.Lightbulb
                        PatternKind.HAPTIC -> Icons.Default.SettingsInputComponent
                        PatternKind.COMBO -> Icons.Default.Star
                    },
                    contentDescription = stringResource(
                        when (pattern.kind) {
                            PatternKind.LIGHT -> R.string.pattern_kind_light
                            PatternKind.HAPTIC -> R.string.pattern_kind_haptic
                            PatternKind.COMBO -> R.string.pattern_kind_combo
                        }
                    ),
                    modifier = Modifier.size(24.dp),
                    tint = when (pattern.kind) {
                        PatternKind.LIGHT -> MaterialTheme.colorScheme.primary
                        PatternKind.HAPTIC -> MaterialTheme.colorScheme.secondary
                        PatternKind.COMBO -> MaterialTheme.colorScheme.tertiary
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Название и количество элементов
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = pattern.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = pluralStringResource(
                            R.plurals.pattern_elements_count,
                            pattern.spec.elements.size,
                            pattern.spec.elements.size
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Кнопки действий
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Кнопка предпросмотра
                    IconButton(
                        onClick = onPreview,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.cd_pattern_preview),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Кнопка деталей (открывает BottomSheet)
                    IconButton(
                        onClick = onShowDetails,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.pattern_card_details),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            HorizontalDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Зацикливание (иконка с цветом вместо текста)
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (pattern.spec.loop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Длительность (если не зациклен)
                if (!pattern.spec.loop) {
                    val totalDuration = calculateTotalDuration(pattern)
                    Row {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = formatDuration(totalDuration),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))


                // Теги
                if (pattern.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        maxLines = 1
                    ) {
                        pattern.tags.forEach { tag ->
                            AssistChip(
                                onClick = { onTagClick(tag) },
                                label = { Text(tag) },
                            )
                        }
                    }
                }
            }
            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteConfirm = false
                            onDelete()
                        }) {
                            Text(stringResource(R.string.dialog_delete_pattern_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text(stringResource(R.string.dialog_delete_pattern_cancel))
                        }
                    },
                    icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    title = { Text(stringResource(R.string.dialog_delete_pattern_title)) },
                    text = { Text(stringResource(R.string.dialog_delete_pattern_message)) }
                )
            }
        }
        }
    }
}

/**
 * Вычисляет общую длительность паттерна в миллисекундах
 */
private fun calculateTotalDuration(pattern: Pattern): Long {
    return pattern.spec.elements.fold(0L) { acc, element ->
        acc + when (element) {
            is PatternElementBreathing -> element.durationMs.toLong()
            is PatternElementChase -> element.speedMs.toLong() * 8L
            is PatternElementFill -> element.durationMs.toLong()
            is PatternElementPulse -> element.speed.toLong() * element.repeats.toLong()
            is PatternElementProgress -> 1000L
            is PatternElementSequence -> {
                element.steps.fold(0L) { sAcc, step ->
                    sAcc + when (step) {
                        is SequenceStep.LedAction -> step.durationMs.toLong()
                        is SequenceStep.DelayAction -> step.durationMs.toLong()
                    }
                }
            }
            is PatternElementSpinner -> element.speedMs.toLong() * 8L
            is PatternElementTimeline -> element.durationMs.toLong()
        }
    }
}

/**
 * Форматирует длительность в читаемый вид
 */
@Composable
private fun formatDuration(durationMs: Long): String {
    return when {
        durationMs < 1000 -> stringResource(R.string.time_format_ms, durationMs.toInt())
        durationMs < 60000 -> stringResource(R.string.time_format_seconds, durationMs / 1000f)
        else -> {
            val minutes = (durationMs / 60000).toInt()
            stringResource(R.string.time_format_minutes, minutes)
        }
    }
}
