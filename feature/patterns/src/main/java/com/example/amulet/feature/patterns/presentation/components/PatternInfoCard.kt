package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternKind

/**
 * Карточка с информацией о паттерне
 */
@Composable
fun PatternInfoCard(
    pattern: Pattern,
    modifier: Modifier = Modifier
) {
    AmuletCard(
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

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

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
                    val totalDuration = pattern.spec.timeline.durationMs.toLong()
                    MetadataRow(
                        icon = Icons.Default.Timer,
                        label = stringResource(R.string.pattern_preview_duration),
                        value = formatDuration(totalDuration)
                    )
                }

                // Версия и железо
                MetadataRow(
                    icon = Icons.Default.Build,
                    label = stringResource(R.string.pattern_preview_hardware_version),
                    value = pattern.hardwareVersion.toString()
                )
                MetadataRow(
                    icon = Icons.Default.Info,
                    label = stringResource(R.string.pattern_preview_version),
                    value = pattern.version.toString()
                )

                // Использования (если есть)
                pattern.usageCount?.let { usage ->
                    MetadataRow(
                        icon = Icons.Default.TrendingUp,
                        label = stringResource(R.string.pattern_preview_usage),
                        value = usage.toString()
                    )
                }
            }
            
            // Теги
            if (pattern.tags.isNotEmpty()) {
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val maxVisible = 6
                    val visible = pattern.tags.take(maxVisible)
                    visible.forEach { tag ->
                        AssistChip(
                            onClick = {},
                            label = { Text(tag) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Tag,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                    val extra = pattern.tags.size - visible.size
                    if (extra > 0) {
                        AssistChip(
                            onClick = {},
                            label = { Text("+${'$'}extra") }
                        )
                    }
                }
            }

            // Даты создания/обновления
            if (pattern.createdAt != null || pattern.updatedAt != null) {
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pattern.createdAt?.let { ts ->
                        MetadataRow(
                            icon = Icons.Default.Schedule,
                            label = stringResource(R.string.pattern_preview_created_at),
                            value = formatDateTime(ts) ?: "—"
                        )
                    }
                    pattern.updatedAt?.let { ts ->
                        MetadataRow(
                            icon = Icons.Default.Update,
                            label = stringResource(R.string.pattern_preview_updated_at),
                            value = formatDateTime(ts) ?: "—"
                        )
                    }
                }
            }
            
            // Автор (для публичных паттернов)
            val ownerId = pattern.ownerId
            if (pattern.public && ownerId != null) {
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
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

private fun formatDateTime(ts: Long): String? {
    return try {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date(ts))
    } catch (_: Throwable) {
        null
    }
}
