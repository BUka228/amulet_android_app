package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.*
import com.example.amulet.feature.patterns.presentation.components.CompactLivePreview
import com.example.amulet.core.design.components.textfield.AmuletTextField

import kotlin.math.roundToInt

/**
 * BottomSheet для редактирования элемента паттерна.
 * Отображает все настройки элемента в удобном формате.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternElementEditorBottomSheet(
    element: PatternElement,
    spec: PatternSpec?,
    isPlaying: Boolean,
    loop: Boolean,
    onPlayPause: () -> Unit,
    onToggleLoop: () -> Unit,
    onDismiss: () -> Unit,
    onUpdate: (PatternElement) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    // Состояние элемента, которое будет обновляться при изменении настроек
    var currentElement by remember { mutableStateOf(element) }
    
    // При изменении исходного элемента, обновляем текущий элемент
    LaunchedEffect(element) {
        currentElement = element
    }
    
    ModalBottomSheet(
        onDismissRequest = {
            // Применяем изменения при закрытии
            onUpdate(currentElement)
            onDismiss()
        },
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
                    text = getElementName(element),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = {
                    // Применяем изменения при закрытии через кнопку
                    onUpdate(currentElement)
                    onDismiss()
                }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_close_dialog),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            HorizontalDivider()
            
            // Компактное превью паттерна
            // Создаем временный spec ТОЛЬКО с текущим элементом для превью
            val previewSpec = spec?.copy(elements = listOf(currentElement))
            
            CompactLivePreview(
                spec = previewSpec,
                isPlaying = isPlaying,
                loop = loop,
                onPlayPause = onPlayPause,
                onToggleLoop = onToggleLoop,
                modifier = Modifier.fillMaxWidth()
            )
            
            HorizontalDivider()
            
            // Основные настройки элемента
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (currentElement) {
                    is PatternElementBreathing -> BreathingEditor(currentElement as PatternElementBreathing) { updated ->
                        currentElement = updated
                    }
                    is PatternElementPulse -> PulseEditor(currentElement as PatternElementPulse) { updated ->
                        currentElement = updated
                    }
                    is PatternElementChase -> ChaseEditor(currentElement as PatternElementChase) { updated ->
                        currentElement = updated
                    }
                    is PatternElementFill -> FillEditor(currentElement as PatternElementFill) { updated ->
                        currentElement = updated
                    }
                    is PatternElementSpinner -> SpinnerEditor(currentElement as PatternElementSpinner) { updated ->
                        currentElement = updated
                    }
                    is PatternElementProgress -> ProgressEditor(currentElement as PatternElementProgress) { updated ->
                        currentElement = updated
                    }
                    is PatternElementSequence -> SequenceEditor(currentElement as PatternElementSequence) { updated ->
                        currentElement = updated
                    }
                    is PatternElementTimeline -> TimelineEditor(currentElement as PatternElementTimeline) { updated ->
                        currentElement = updated
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) },
            label = stringResource(R.string.pattern_element_color_label)
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_duration_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = stringResource(R.string.time_format_ms, element.durationMs),
                    style = MaterialTheme.typography.bodyMedium,
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) },
            label = stringResource(R.string.pattern_element_color_label)
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_speed_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = stringResource(R.string.time_format_ms, element.speed),
                    style = MaterialTheme.typography.bodyMedium,
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

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_repeats_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = element.repeats.toString(),
                    style = MaterialTheme.typography.bodyMedium,
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) },
            label = stringResource(R.string.pattern_element_color_label)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.pattern_element_direction_label),
                style = MaterialTheme.typography.labelLarge
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
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_speed_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = stringResource(R.string.time_format_ms, element.speedMs),
                    style = MaterialTheme.typography.bodyMedium,
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) },
            label = stringResource(R.string.pattern_element_color_label)
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_duration_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = stringResource(R.string.time_format_ms, element.durationMs),
                    style = MaterialTheme.typography.bodyMedium,
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_speed_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = stringResource(R.string.time_format_ms, element.speedMs),
                    style = MaterialTheme.typography.bodyMedium,
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) },
            label = stringResource(R.string.pattern_element_color_label)
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_active_leds_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = element.activeLeds.toString(),
                    style = MaterialTheme.typography.bodyMedium,
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.sequence_steps_title, element.steps.size),
            style = MaterialTheme.typography.titleSmall
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            element.steps.forEachIndexed { idx, step ->
                ElevatedCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (step) {
                                    is SequenceStep.LedAction -> stringResource(R.string.sequence_step_led)
                                    is SequenceStep.DelayAction -> stringResource(R.string.sequence_step_delay)
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(enabled = idx > 0, onClick = {
                                    val list = element.steps.toMutableList()
                                    val tmp = list[idx - 1]
                                    list[idx - 1] = list[idx]
                                    list[idx] = tmp
                                    onUpdate(PatternElementSequence(list))
                                }) { Icon(Icons.Default.ArrowUpward, contentDescription = null) }
                                IconButton(enabled = idx < element.steps.lastIndex, onClick = {
                                    val list = element.steps.toMutableList()
                                    val tmp = list[idx + 1]
                                    list[idx + 1] = list[idx]
                                    list[idx] = tmp
                                    onUpdate(PatternElementSequence(list))
                                }) { Icon(Icons.Default.ArrowDownward, contentDescription = null) }
                                IconButton(onClick = {
                                    val list = element.steps.toMutableList()
                                    list.add(idx + 1, step)
                                    onUpdate(PatternElementSequence(list))
                                }) { Icon(Icons.Default.ContentCopy, contentDescription = null) }
                                IconButton(onClick = {
                                    val list = element.steps.toMutableList()
                                    list.removeAt(idx)
                                    onUpdate(PatternElementSequence(list))
                                }) { Icon(Icons.Default.Delete, contentDescription = null) }
                            }
                        }

                        when (step) {
                            is SequenceStep.LedAction -> {
                                var led by remember(step) { mutableStateOf(step.ledIndex) }
                                var color by remember(step) { mutableStateOf(step.color) }
                                var dur by remember(step) { mutableStateOf(step.durationMs) }
                                var durText by remember(dur) { mutableStateOf(dur.toString()) }

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(stringResource(R.string.sequence_led_index), style = MaterialTheme.typography.bodyMedium)
                                        Text(led.toString())
                                    }
                                    Slider(value = led.toFloat(), onValueChange = {
                                        led = it.toInt().coerceIn(0, 7)
                                        val list = element.steps.toMutableList()
                                        list[idx] = step.copy(ledIndex = led, color = color, durationMs = dur)
                                        onUpdate(PatternElementSequence(list))
                                    }, valueRange = 0f..7f, steps = 6)

                                    ColorPicker(
                                        color = color,
                                        onColorChange = { v ->
                                            color = v
                                            val list = element.steps.toMutableList()
                                            list[idx] = step.copy(ledIndex = led, color = color, durationMs = dur)
                                            onUpdate(PatternElementSequence(list))
                                        },
                                        label = stringResource(R.string.sequence_color_hex)
                                    )

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(stringResource(R.string.sequence_duration_ms), style = MaterialTheme.typography.bodyMedium)
                                        Text(dur.toString())
                                    }
                                    Slider(value = dur.toFloat(), onValueChange = {
                                        val nd = it.toInt().coerceIn(100, 6000)
                                        val list = element.steps.toMutableList()
                                        dur = nd
                                        durText = nd.toString()
                                        list[idx] = step.copy(ledIndex = led, color = color, durationMs = dur)
                                        onUpdate(PatternElementSequence(list))
                                    }, valueRange = 100f..6000f, steps = 50)

                                    AmuletTextField(
                                        value = durText,
                                        onValueChange = { txt ->
                                            val filtered = txt.filter { it.isDigit() }.take(5)
                                            durText = filtered
                                            val parsed = filtered.toIntOrNull()
                                            if (parsed != null) {
                                                val nd = parsed.coerceIn(100, 6000)
                                                if (nd != dur) {
                                                    dur = nd
                                                    val list = element.steps.toMutableList()
                                                    list[idx] = step.copy(ledIndex = led, color = color, durationMs = dur)
                                                    onUpdate(PatternElementSequence(list))
                                                }
                                            }
                                        },
                                        label = stringResource(R.string.sequence_duration_ms)
                                    )
                                }
                            }
                            is SequenceStep.DelayAction -> {
                                var dur by remember(step) { mutableStateOf(step.durationMs) }
                                var durText by remember(dur) { mutableStateOf(dur.toString()) }
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(stringResource(R.string.sequence_delay_ms), style = MaterialTheme.typography.bodyMedium)
                                        Text(dur.toString())
                                    }
                                    Slider(value = dur.toFloat(), onValueChange = {
                                        val nd = it.toInt().coerceIn(100, 6000)
                                        dur = nd
                                        durText = nd.toString()
                                        val list = element.steps.toMutableList()
                                        list[idx] = SequenceStep.DelayAction(dur)
                                        onUpdate(PatternElementSequence(list))
                                    }, valueRange = 100f..6000f, steps = 50)

                                    AmuletTextField(
                                        value = durText,
                                        onValueChange = { txt ->
                                            val filtered = txt.filter { it.isDigit() }.take(5)
                                            durText = filtered
                                            val parsed = filtered.toIntOrNull()
                                            if (parsed != null) {
                                                val nd = parsed.coerceIn(100, 6000)
                                                if (nd != dur) {
                                                    dur = nd
                                                    val list = element.steps.toMutableList()
                                                    list[idx] = SequenceStep.DelayAction(dur)
                                                    onUpdate(PatternElementSequence(list))
                                                }
                                            }
                                        },
                                        label = stringResource(R.string.sequence_delay_ms)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val list = element.steps.toMutableList()
                list.add(SequenceStep.LedAction(0, "#FFFFFF", 200))
                onUpdate(PatternElementSequence(list))
            }) { Text(stringResource(R.string.sequence_add_led)) }
            OutlinedButton(onClick = {
                val list = element.steps.toMutableList()
                list.add(SequenceStep.DelayAction(200))
                onUpdate(PatternElementSequence(list))
            }) { Text(stringResource(R.string.sequence_add_delay)) }
        }
    }
}

@Composable
private fun TimelineEditor(
    element: PatternElementTimeline,
    onUpdate: (PatternElement) -> Unit
) {
    TimelineEditorContent(element = element, onUpdate = onUpdate)
}

// Вспомогательные функции

@Composable
private fun getElementName(element: PatternElement) = when (element) {
    is PatternElementBreathing -> stringResource(R.string.pattern_element_breathing)
    is PatternElementPulse -> stringResource(R.string.pattern_element_pulse)
    is PatternElementChase -> stringResource(R.string.pattern_element_chase)
    is PatternElementFill -> stringResource(R.string.pattern_element_fill)
    is PatternElementSpinner -> stringResource(R.string.pattern_element_spinner)
    is PatternElementProgress -> stringResource(R.string.pattern_element_progress)
    is PatternElementSequence -> stringResource(R.string.pattern_element_sequence)
    is PatternElementTimeline -> stringResource(R.string.pattern_element_timeline)
}
