package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * BottomSheet с детальной информацией о паттерне
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PatternDetailsBottomSheet(
    pattern: Pattern,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Заголовок
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pattern.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_close_dialog),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            HorizontalDivider()
            
            // Основная информация
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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

                // Метаданные
                Text(
                    text = stringResource(R.string.pattern_preview_metadata),
                    style = MaterialTheme.typography.titleMedium,
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
                    
                    // Видимость
                    MetadataRow(
                        icon = if (pattern.public) Icons.Default.Public else Icons.Default.Lock,
                        label = stringResource(R.string.pattern_details_visibility_label),
                        value = stringResource(
                            if (pattern.public) R.string.pattern_details_public 
                            else R.string.pattern_details_private
                        )
                    )
                    
                    // Версия железа
                    MetadataRow(
                        icon = Icons.Default.Memory,
                        label = stringResource(R.string.pattern_details_hardware_version_label),
                        value = pattern.hardwareVersion.toString()
                    )
                    
                    // Тип спецификации
                    MetadataRow(
                        icon = Icons.Default.Code,
                        label = stringResource(R.string.pattern_details_spec_type_label),
                        value = pattern.spec.type
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
                        val totalDuration = calculatePatternDuration(pattern)
                        MetadataRow(
                            icon = Icons.Default.Timer,
                            label = stringResource(R.string.pattern_preview_duration),
                            value = formatPatternDuration(totalDuration)
                        )
                    }
                    
                    // Статус модерации
                    pattern.reviewStatus?.let { status ->
                        MetadataRow(
                            icon = Icons.Default.Verified,
                            label = stringResource(R.string.pattern_details_review_status_label),
                            value = when (status) {
                                ReviewStatus.PENDING -> "Pending"
                                ReviewStatus.APPROVED -> "Approved"
                                ReviewStatus.REJECTED -> "Rejected"
                            }
                        )
                    }
                    
                    // Количество использований
                    pattern.usageCount?.let { count ->
                        MetadataRow(
                            icon = Icons.Default.PlayArrow,
                            label = stringResource(R.string.pattern_details_usage_count_label),
                            value = count.toString()
                        )
                    }
                    
                    // Даты создания и обновления
                    pattern.createdAt?.let { timestamp ->
                        MetadataRow(
                            icon = Icons.Default.Schedule,
                            label = stringResource(R.string.pattern_details_created_at_label),
                            value = formatTimestamp(timestamp)
                        )
                    }
                    
                    pattern.updatedAt?.let { timestamp ->
                        MetadataRow(
                            icon = Icons.Default.Update,
                            label = stringResource(R.string.pattern_details_updated_at_label),
                            value = formatTimestamp(timestamp)
                        )
                    }
                }
                
                // Теги
                if (pattern.tags.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.pattern_details_tags_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        pattern.tags.forEach { tag ->
                            AssistChip(
                                onClick = { },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
                
                // Элементы паттерна
                if (pattern.spec.elements.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.pattern_details_elements_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pattern.spec.elements.forEachIndexed { index, element ->
                            ElementSummaryCard(
                                element = element,
                                index = index + 1
                            )
                        }
                    }
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

@Composable
private fun ElementSummaryCard(
    element: PatternElement,
    index: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Icon(
                imageVector = when (element) {
                    is PatternElementBreathing -> Icons.Default.Air
                    is PatternElementPulse -> Icons.Default.FlashOn
                    is PatternElementChase -> Icons.Default.RotateRight
                    is PatternElementFill -> Icons.Default.LinearScale
                    is PatternElementSpinner -> Icons.Default.Refresh
                    is PatternElementProgress -> Icons.Default.BarChart
                    is PatternElementSequence -> Icons.Default.List
                    is PatternElementTimeline -> Icons.Default.Timeline
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = when (element) {
                    is PatternElementBreathing -> stringResource(R.string.pattern_element_breathing)
                    is PatternElementPulse -> stringResource(R.string.pattern_element_pulse)
                    is PatternElementChase -> stringResource(R.string.pattern_element_chase)
                    is PatternElementFill -> stringResource(R.string.pattern_element_fill)
                    is PatternElementSpinner -> stringResource(R.string.pattern_element_spinner)
                    is PatternElementProgress -> stringResource(R.string.pattern_element_progress)
                    is PatternElementSequence -> stringResource(R.string.pattern_element_sequence)
                    is PatternElementTimeline -> stringResource(R.string.pattern_element_timeline)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
