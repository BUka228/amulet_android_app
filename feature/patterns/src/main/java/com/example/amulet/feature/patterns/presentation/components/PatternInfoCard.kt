package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.*

/**
 * Карточка с информацией о паттерне
 */
@Composable
fun PatternInfoCard(
    pattern: Pattern,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Название
            Text(
                text = pattern.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Описание
            pattern.description?.let { description ->
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Divider()
            
            // Метаданные
            Text(
                text = stringResource(R.string.pattern_preview_metadata),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Тип паттерна
                MetadataRow(
                    icon = when (pattern.kind) {
                        PatternKind.LIGHT -> Icons.Default.Lightbulb
                        PatternKind.HAPTIC -> Icons.Default.Vibration
                        PatternKind.COMBO -> Icons.Default.AutoAwesome
                    },
                    label = stringResource(R.string.pattern_editor_kind_label),
                    value = stringResource(
                        when (pattern.kind) {
                            PatternKind.LIGHT -> R.string.pattern_kind_light
                            PatternKind.HAPTIC -> R.string.pattern_kind_haptic
                            PatternKind.COMBO -> R.string.pattern_kind_combo
                        }
                    )
                )
                
                // Количество элементов
                MetadataRow(
                    icon = Icons.Default.Layers,
                    label = stringResource(R.string.pattern_editor_elements_header),
                    value = pluralStringResource(
                        R.plurals.pattern_elements_count,
                        pattern.spec.elements.size,
                        pattern.spec.elements.size
                    )
                )
                
                // Зацикливание
                MetadataRow(
                    icon = Icons.Default.Repeat,
                    label = stringResource(R.string.pattern_editor_loop_label),
                    value = if (pattern.spec.loop) {
                        stringResource(android.R.string.yes)
                    } else {
                        stringResource(android.R.string.no)
                    }
                )
                
                // Длительность (если не зациклен)
                if (!pattern.spec.loop) {
                    val totalDuration = calculateTotalDuration(pattern)
                    MetadataRow(
                        icon = Icons.Default.Timer,
                        label = stringResource(R.string.pattern_preview_duration),
                        value = formatDuration(totalDuration)
                    )
                }
            }
            
            // Автор (для публичных паттернов)
            val ownerId = pattern.ownerId
            if (pattern.public && ownerId != null) {
                Divider()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.pattern_preview_author, ownerId.value),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Вычисляет общую длительность паттерна в миллисекундах
 */
private fun calculateTotalDuration(pattern: Pattern): Long {
    return pattern.spec.elements.sumOf { element ->
        when (element) {
            is PatternElementBreathing -> element.durationMs.toLong()
            is PatternElementChase -> element.speedMs.toLong() * 8L
            is PatternElementFill -> element.durationMs.toLong()
            is PatternElementPulse -> element.speed.toLong() * element.repeats.toLong()
            is PatternElementProgress -> 1000L
            is PatternElementSequence -> {
                element.steps.sumOf { step ->
                    when (step) {
                        is SequenceStep.LedAction -> step.durationMs.toLong()
                        is SequenceStep.DelayAction -> step.durationMs.toLong()
                    }
                }
            }
            is PatternElementSpinner -> element.speedMs.toLong() * 8L
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
