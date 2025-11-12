package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternElementEditor(
    element: PatternElement,
    index: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onUpdate: (PatternElement) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var showDetails by remember { mutableStateOf(isSelected) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(isSelected) {
        showDetails = isSelected
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(stringResource(R.string.dialog_delete_element_title))
            },
            text = {
                Text(stringResource(R.string.dialog_delete_element_message))
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemove()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.dialog_delete_element_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.dialog_delete_element_cancel))
                }
            }
        )
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            onSelect()
            showDetails = !showDetails
        }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Компактный заголовок элемента
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        getElementIcon(element),
                        contentDescription = stringResource(R.string.cd_element_icon),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${index + 1}. ${getElementName(element)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = getElementDescription(element),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    // Компактные кнопки перемещения
                    IconButton(
                        onClick = { onMoveUp?.invoke() },
                        enabled = onMoveUp != null,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = stringResource(R.string.cd_move_element_up),
                            modifier = Modifier.size(18.dp),
                            tint = if (onMoveUp != null) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                    
                    IconButton(
                        onClick = { onMoveDown?.invoke() },
                        enabled = onMoveDown != null,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.cd_move_element_down),
                            modifier = Modifier.size(18.dp),
                            tint = if (onMoveDown != null) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }

                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.cd_delete_element),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    IconButton(
                        onClick = { showDetails = !showDetails },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showDetails) {
                                stringResource(R.string.cd_collapse_element)
                            } else {
                                stringResource(R.string.cd_expand_element)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Детали элемента (раскрывающиеся с анимацией)
            AnimatedVisibility(
                visible = showDetails,
                enter = expandVertically(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseInOutCubic
                    ),
                    expandFrom = Alignment.Top
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseInOutCubic
                    )
                ),
                exit = shrinkVertically(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseInOutCubic
                    ),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseInOutCubic
                    )
                )
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    when (element) {
                        is PatternElementBreathing -> BreathingEditor(element, onUpdate)
                        is PatternElementPulse -> PulseEditor(element, onUpdate)
                        is PatternElementChase -> ChaseEditor(element, onUpdate)
                        is PatternElementFill -> FillEditor(element, onUpdate)
                        is PatternElementSpinner -> SpinnerEditor(element, onUpdate)
                        is PatternElementProgress -> ProgressEditor(element, onUpdate)
                        is PatternElementSequence -> SequenceEditor(element, onUpdate)
                    }
                }
            }
        }
    }
}

@Composable
private fun BreathingEditor(
    element: PatternElementBreathing,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) },
            label = stringResource(R.string.pattern_element_color_label)
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_duration_label),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(R.string.time_format_ms, element.durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = element.durationMs.toFloat(),
                onValueChange = { onUpdate(element.copy(durationMs = it.toInt())) },
                valueRange = 100f..10000f,
                steps = 98
            )
        }
    }
}

@Composable
private fun PulseEditor(
    element: PatternElementPulse,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) },
            label = stringResource(R.string.pattern_element_color_label)
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_speed_label),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(R.string.time_format_ms, element.speed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = element.speed.toFloat(),
                onValueChange = { onUpdate(element.copy(speed = it.toInt())) },
                valueRange = 100f..2000f,
                steps = 18
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_repeats_label),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = element.repeats.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = element.repeats.toFloat(),
                onValueChange = { onUpdate(element.copy(repeats = it.toInt())) },
                valueRange = 1f..10f,
                steps = 8
            )
        }
    }
}

@Composable
private fun ChaseEditor(
    element: PatternElementChase,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) },
            label = stringResource(R.string.pattern_element_color_label)
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.pattern_element_direction_label),
                style = MaterialTheme.typography.labelMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = element.direction == ChaseDirection.CLOCKWISE,
                    onClick = { onUpdate(element.copy(direction = ChaseDirection.CLOCKWISE)) },
                    label = { Text(stringResource(R.string.pattern_element_direction_cw)) },
                    leadingIcon = if (element.direction == ChaseDirection.CLOCKWISE) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = element.direction == ChaseDirection.COUNTER_CLOCKWISE,
                    onClick = { onUpdate(element.copy(direction = ChaseDirection.COUNTER_CLOCKWISE)) },
                    label = { Text(stringResource(R.string.pattern_element_direction_ccw)) },
                    leadingIcon = if (element.direction == ChaseDirection.COUNTER_CLOCKWISE) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_speed_label),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(R.string.time_format_ms, element.speedMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = element.speedMs.toFloat(),
                onValueChange = { onUpdate(element.copy(speedMs = it.toInt())) },
                valueRange = 50f..1000f,
                steps = 18
            )
        }
    }
}

@Composable
private fun FillEditor(
    element: PatternElementFill,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) },
            label = stringResource(R.string.pattern_element_color_label)
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_duration_label),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(R.string.time_format_ms, element.durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = element.durationMs.toFloat(),
                onValueChange = { onUpdate(element.copy(durationMs = it.toInt())) },
                valueRange = 100f..5000f,
                steps = 48
            )
        }
    }
}

@Composable
private fun SpinnerEditor(
    element: PatternElementSpinner,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ColorPicker(
            color = element.colors.getOrNull(0) ?: "#FFFFFF",
            onColorChange = { 
                val newColors = element.colors.toMutableList()
                if (newColors.isEmpty()) {
                    newColors.add(it)
                } else {
                    newColors[0] = it
                }
                onUpdate(element.copy(colors = newColors))
            },
            label = stringResource(R.string.pattern_element_color_primary)
        )

        ColorPicker(
            color = element.colors.getOrNull(1) ?: "#000000",
            onColorChange = { 
                val newColors = element.colors.toMutableList()
                if (newColors.size > 1) {
                    newColors[1] = it
                } else {
                    while (newColors.size < 2) {
                        newColors.add(it)
                    }
                }
                onUpdate(element.copy(colors = newColors))
            },
            label = stringResource(R.string.pattern_element_color_secondary)
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_speed_label),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(R.string.time_format_ms, element.speedMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = element.speedMs.toFloat(),
                onValueChange = { onUpdate(element.copy(speedMs = it.toInt())) },
                valueRange = 50f..1000f,
                steps = 18
            )
        }
    }
}

@Composable
private fun ProgressEditor(
    element: PatternElementProgress,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) },
            label = stringResource(R.string.pattern_element_color_label)
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_active_leds_label),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = element.activeLeds.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = element.activeLeds.toFloat(),
                onValueChange = { onUpdate(element.copy(activeLeds = it.toInt())) },
                valueRange = 1f..8f,
                steps = 6
            )
        }
    }
}

@Composable
private fun SequenceEditor(
    element: PatternElementSequence,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = stringResource(R.string.pattern_element_sequence_editor_wip),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.pattern_element_sequence_steps, element.steps.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = stringResource(R.string.pattern_element_sequence_editor_wip),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// Вспомогательные функции

private fun getElementIcon(element: PatternElement) = when (element) {
    is PatternElementBreathing -> Icons.Default.Air
    is PatternElementPulse -> Icons.Default.FiberManualRecord
    is PatternElementChase -> Icons.Default.Autorenew
    is PatternElementFill -> Icons.Default.FormatColorFill
    is PatternElementSpinner -> Icons.Default.Sync
    is PatternElementProgress -> Icons.Default.LinearScale
    is PatternElementSequence -> Icons.AutoMirrored.Filled.List
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
}

private fun getElementDescription(element: PatternElement) = when (element) {
    is PatternElementBreathing -> "${element.color}, ${element.durationMs}мс"
    is PatternElementPulse -> "${element.color}, ${element.speed}мс × ${element.repeats}"
    is PatternElementChase -> "${element.color}, ${element.speedMs}мс"
    is PatternElementFill -> "${element.color}, ${element.durationMs}мс"
    is PatternElementSpinner -> "${element.colors.size} цвета, ${element.speedMs}мс"
    is PatternElementProgress -> "${element.color}, ${element.activeLeds} диодов"
    is PatternElementSequence -> "${element.steps.size} шагов"
}
