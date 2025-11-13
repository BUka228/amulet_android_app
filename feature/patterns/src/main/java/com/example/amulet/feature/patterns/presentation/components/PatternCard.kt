package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.components.card.CardElevation
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.*

@Composable
fun PatternCard(
    pattern: Pattern,
    onClick: () -> Unit,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onShare: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    AmuletCard(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardElevation.Low
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        ) {
            // Основная информация (всегда видна)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Иконка типа паттерна с цветом
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { expanded = !expanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (pattern.kind) {
                            PatternKind.LIGHT -> Icons.Default.Lightbulb
                            PatternKind.HAPTIC -> Icons.Default.SettingsInputComponent
                            PatternKind.COMBO -> Icons.Default.Star
                        },
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = when (pattern.kind) {
                            PatternKind.LIGHT -> MaterialTheme.colorScheme.primary
                            PatternKind.HAPTIC -> MaterialTheme.colorScheme.secondary
                            PatternKind.COMBO -> MaterialTheme.colorScheme.tertiary
                        }
                    )
                }

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
                    // Кнопка раскрытия
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Свернуть" else "Развернуть",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

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

                    // Меню действий
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.cd_pattern_menu),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.pattern_card_edit)) },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.pattern_card_duplicate)) },
                                onClick = {
                                    showMenu = false
                                    onDuplicate()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                }
                            )
                            if (onShare != null) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.pattern_card_share)) },
                                    onClick = {
                                        showMenu = false
                                        onShare()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Share, contentDescription = null)
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.pattern_card_delete)) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                }
            }

            // Раскрывающаяся часть с дополнительной информацией
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    // Описание
                    pattern.description?.let { desc ->
                        if (desc.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Метаданные паттерна
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Тип паттерна
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (pattern.kind) {
                                        PatternKind.LIGHT -> Icons.Default.Lightbulb
                                        PatternKind.HAPTIC -> Icons.Default.SettingsInputComponent
                                        PatternKind.COMBO -> Icons.Default.Star
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = when (pattern.kind) {
                                        PatternKind.LIGHT -> MaterialTheme.colorScheme.primary
                                        PatternKind.HAPTIC -> MaterialTheme.colorScheme.secondary
                                        PatternKind.COMBO -> MaterialTheme.colorScheme.tertiary
                                    }
                                )
                                Text(
                                    text = stringResource(R.string.pattern_editor_kind_label),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = stringResource(
                                    when (pattern.kind) {
                                        PatternKind.LIGHT -> R.string.pattern_kind_light
                                        PatternKind.HAPTIC -> R.string.pattern_kind_haptic
                                        PatternKind.COMBO -> R.string.pattern_kind_combo
                                    }
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Зацикливание (иконка с цветом вместо текста)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (pattern.spec.loop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(R.string.pattern_editor_loop_label),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = if (pattern.spec.loop) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = if (pattern.spec.loop) "Зациклен" else "Не зациклен",
                                tint = if (pattern.spec.loop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Длительность (если не зациклен)
                        if (!pattern.spec.loop) {
                            val totalDuration = calculateTotalDuration(pattern)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = stringResource(R.string.pattern_preview_duration),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = formatDuration(totalDuration),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Количество использований (для публичных паттернов)
                        val usageCount = pattern.usageCount
                        if (pattern.public && usageCount != null && usageCount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = stringResource(R.string.pattern_card_downloads),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = usageCount.toString(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Автор (для публичных паттернов)
                        val ownerId = pattern.ownerId
                        if (pattern.public && ownerId != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = stringResource(R.string.pattern_preview_author, ""),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = ownerId.value,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Теги
                        if (pattern.tags.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tag,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Теги",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = pattern.tags.joinToString(", "),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
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
