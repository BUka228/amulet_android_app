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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                valueRange = 500f..6000f,
                steps = 110
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_tick_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = stringResource(R.string.time_format_ms, element.tickMs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = element.tickMs.toFloat(),
                onValueChange = { onUpdate(element.copy(tickMs = it.toInt())) },
                valueRange = 10f..1000f,
                steps = 99
            )
        }

        ElevatedButton(onClick = {
            val newTrack = TimelineTrack(
                target = TargetLed(0),
                priority = 0,
                mixMode = MixMode.OVERRIDE,
                clips = emptyList()
            )
            onUpdate(element.copy(tracks = element.tracks + newTrack))
        }) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(text = stringResource(R.string.timeline_add_track))
        }

        // Toolbar: zoom/snap/tick
        var zoom by remember { mutableStateOf(0.25f) }
        var snap by remember { mutableStateOf(true) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.timeline_zoom), style = MaterialTheme.typography.labelLarge)
            Slider(
                value = zoom,
                onValueChange = { zoom = it },
                valueRange = 0.05f..1.0f,
                steps = 10,
                modifier = Modifier.weight(1f)
            )
            AssistChip(
                onClick = { snap = !snap },
                label = { Text(stringResource(R.string.timeline_snap_to_grid)) },
                leadingIcon = {
                    if (snap) Icon(Icons.Default.Check, contentDescription = null) else null
                }
            )
            Text(stringResource(R.string.timeline_tick_ms))
            Text(stringResource(R.string.time_format_ms, element.tickMs))
        }

        // Timeline Canvas (drag to move/resize clips)
        var selectedClip by remember { mutableStateOf<Pair<Int, Int>?>(null) }
        TimelineCanvas(
            element = element,
            onChange = { updated -> onUpdate(updated) },
            modifier = Modifier
                .fillMaxWidth(),
            pixelsPerMs = zoom,
            snapToGrid = snap,
            tickMs = element.tickMs,
            selected = selectedClip,
            onSelect = { selectedClip = it },
            onContextRequest = { selectedClip = it }
        )

        // Compact Clip Inspector
        selectedClip?.let { (tIdx, cIdx) ->
            val track = element.tracks.getOrNull(tIdx)
            val clip = track?.clips?.getOrNull(cIdx)
            if (track != null && clip != null) {
                ElevatedCard {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.timeline_clips), style = MaterialTheme.typography.titleSmall)

                        // Color
                        ColorPicker(
                            color = clip.color,
                            onColorChange = { color ->
                                val tracks = element.tracks.toMutableList()
                                val clips = track.clips.toMutableList()
                                clips[cIdx] = clip.copy(color = color)
                                tracks[tIdx] = track.copy(clips = clips)
                                onUpdate(element.copy(tracks = tracks))
                            },
                            label = stringResource(R.string.pattern_element_color_label)
                        )

                        // Start
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.timeline_clip_start), style = MaterialTheme.typography.labelLarge)
                                Text(stringResource(R.string.time_format_ms, clip.startMs))
                            }
                            Slider(
                                value = clip.startMs.toFloat(),
                                onValueChange = { v ->
                                    // вычисляем границы по соседям
                                    val others = track.clips.filterIndexed { idx, _ -> idx != cIdx }.sortedBy { it.startMs }
                                    val prev = others.lastOrNull { it.startMs + it.durationMs <= clip.startMs }
                                    val next = others.firstOrNull { it.startMs >= clip.startMs }
                                    val minStart = (prev?.let { it.startMs + it.durationMs } ?: 0)
                                    val maxStart = (next?.startMs ?: element.durationMs) - clip.durationMs
                                    var newStart = v.toInt().coerceIn(minStart, maxStart)
                                    if (snap) {
                                        val q = (newStart.toFloat() / element.tickMs).roundToInt()
                                        newStart = (q * element.tickMs).coerceIn(minStart, maxStart)
                                    }
                                    val tracks = element.tracks.toMutableList()
                                    val clips = track.clips.toMutableList()
                                    clips[cIdx] = clip.copy(startMs = newStart)
                                    tracks[tIdx] = track.copy(clips = clips)
                                    onUpdate(element.copy(tracks = tracks))
                                },
                                valueRange = 0f..element.durationMs.toFloat(),
                                steps = 50
                            )
                        }

                        // Duration
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.timeline_clip_duration), style = MaterialTheme.typography.labelLarge)
                                Text(stringResource(R.string.time_format_ms, clip.durationMs))
                            }
                            Slider(
                                value = clip.durationMs.toFloat(),
                                onValueChange = { v ->
                                    val others = track.clips.filterIndexed { idx, _ -> idx != cIdx }.sortedBy { it.startMs }
                                    val next = others.firstOrNull { it.startMs >= clip.startMs }
                                    val maxEnd = (next?.startMs ?: element.durationMs)
                                    val maxDur = (maxEnd - clip.startMs).coerceAtLeast(element.tickMs)
                                    var newDur = v.toInt().coerceIn(element.tickMs, maxDur)
                                    if (snap) {
                                        val q = (newDur.toFloat() / element.tickMs).roundToInt()
                                        newDur = (q * element.tickMs).coerceIn(element.tickMs, maxDur)
                                    }
                                    val tracks = element.tracks.toMutableList()
                                    val clips = track.clips.toMutableList()
                                    clips[cIdx] = clip.copy(durationMs = newDur)
                                    tracks[tIdx] = track.copy(clips = clips)
                                    onUpdate(element.copy(tracks = tracks))
                                },
                                valueRange = element.tickMs.toFloat()..element.durationMs.toFloat(),
                                steps = 50
                            )
                        }

                        // Fades
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.timeline_clip_fade_in), style = MaterialTheme.typography.labelLarge)
                                Text(stringResource(R.string.time_format_ms, clip.fadeInMs))
                            }
                            Slider(
                                value = clip.fadeInMs.toFloat(),
                                onValueChange = { v ->
                                    val newVal = v.toInt().coerceIn(0, clip.durationMs)
                                    val tracks = element.tracks.toMutableList()
                                    val clips = track.clips.toMutableList()
                                    clips[cIdx] = clip.copy(fadeInMs = newVal)
                                    tracks[tIdx] = track.copy(clips = clips)
                                    onUpdate(element.copy(tracks = tracks))
                                },
                                valueRange = 0f..clip.durationMs.toFloat(),
                                steps = 50
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.timeline_clip_fade_out), style = MaterialTheme.typography.labelLarge)
                                Text(stringResource(R.string.time_format_ms, clip.fadeOutMs))
                            }
                            Slider(
                                value = clip.fadeOutMs.toFloat(),
                                onValueChange = { v ->
                                    val newVal = v.toInt().coerceIn(0, clip.durationMs)
                                    val tracks = element.tracks.toMutableList()
                                    val clips = track.clips.toMutableList()
                                    clips[cIdx] = clip.copy(fadeOutMs = newVal)
                                    tracks[tIdx] = track.copy(clips = clips)
                                    onUpdate(element.copy(tracks = tracks))
                                },
                                valueRange = 0f..clip.durationMs.toFloat(),
                                steps = 50
                            )
                        }

                        // Actions: Duplicate / Delete
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = {
                                val step = element.tickMs.coerceAtLeast(10)
                                val baseStart = (clip.startMs + clip.durationMs + step)
                                val clipsSorted = track.clips.sortedBy { it.startMs }
                                var newStart = baseStart
                                val duration = clip.durationMs
                                // находим ближайшее место без пересечений
                                while (clipsSorted.any { other -> !(newStart + duration <= other.startMs || newStart >= other.startMs + other.durationMs) }) {
                                    newStart += step
                                    if (newStart + duration > element.durationMs) break
                                }
                                if (newStart + duration <= element.durationMs) {
                                    val tracks = element.tracks.toMutableList()
                                    val newClips = track.clips.toMutableList()
                                    newClips.add(clip.copy(startMs = newStart))
                                    tracks[tIdx] = track.copy(clips = newClips.sortedBy { it.startMs })
                                    onUpdate(element.copy(tracks = tracks))
                                }
                            }) {
                                Text(stringResource(R.string.timeline_duplicate_clip))
                            }
                            TextButton(onClick = {
                                val tracks = element.tracks.toMutableList()
                                val newClips = track.clips.toMutableList()
                                newClips.removeAt(cIdx)
                                tracks[tIdx] = track.copy(clips = newClips)
                                onUpdate(element.copy(tracks = tracks))
                            }) {
                                Text(stringResource(R.string.timeline_remove_clip))
                            }
                        }
                    }
                }
            }
        }

        element.tracks.forEachIndexed { trackIndex, track ->
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.timeline_target), style = MaterialTheme.typography.labelLarge)
                        IconButton(onClick = {
                            val updated = element.tracks.toMutableList().also { it.removeAt(trackIndex) }
                            onUpdate(element.copy(tracks = updated))
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.timeline_remove_track))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = track.target is TargetLed,
                            onClick = {
                                val updated = element.tracks.toMutableList()
                                updated[trackIndex] = track.copy(target = TargetLed(0))
                                onUpdate(element.copy(tracks = updated))
                            },
                            label = { Text(stringResource(R.string.timeline_target_led)) }
                        )
                        FilterChip(
                            selected = track.target is TargetGroup,
                            onClick = {
                                val updated = element.tracks.toMutableList()
                                updated[trackIndex] = track.copy(target = TargetGroup(emptyList()))
                                onUpdate(element.copy(tracks = updated))
                            },
                            label = { Text(stringResource(R.string.timeline_target_group)) }
                        )
                        FilterChip(
                            selected = track.target is TargetRing,
                            onClick = {
                                val updated = element.tracks.toMutableList()
                                updated[trackIndex] = track.copy(target = TargetRing)
                                onUpdate(element.copy(tracks = updated))
                            },
                            label = { Text(stringResource(R.string.timeline_target_ring)) }
                        )
                    }

                    when (val target = track.target) {
                        is TargetLed -> {
                            RingSelector(
                                selected = setOf(target.index),
                                onToggle = { idx ->
                                    val updated = element.tracks.toMutableList()
                                    updated[trackIndex] = track.copy(target = TargetLed(idx))
                                    onUpdate(element.copy(tracks = updated))
                                },
                                modifier = Modifier.size(160.dp)
                            )
                        }
                        is TargetGroup -> {
                            RingSelector(
                                selected = target.indices.toSet(),
                                onToggle = { idx ->
                                    val set = target.indices.toMutableSet()
                                    if (!set.add(idx)) set.remove(idx)
                                    val updated = element.tracks.toMutableList()
                                    updated[trackIndex] = track.copy(target = TargetGroup(set.sorted()))
                                    onUpdate(element.copy(tracks = updated))
                                },
                                modifier = Modifier.size(160.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                AssistChip(onClick = {
                                    val updated = element.tracks.toMutableList()
                                    updated[trackIndex] = track.copy(target = TargetGroup((0..7).toList()))
                                    onUpdate(element.copy(tracks = updated))
                                }, label = { Text(stringResource(R.string.timeline_preset_all)) })
                                AssistChip(onClick = {
                                    val updated = element.tracks.toMutableList()
                                    updated[trackIndex] = track.copy(target = TargetGroup(emptyList()))
                                    onUpdate(element.copy(tracks = updated))
                                }, label = { Text(stringResource(R.string.timeline_preset_none)) })
                                AssistChip(onClick = {
                                    val evens = (0..7).filter { it % 2 == 0 }
                                    val updated = element.tracks.toMutableList()
                                    updated[trackIndex] = track.copy(target = TargetGroup(evens))
                                    onUpdate(element.copy(tracks = updated))
                                }, label = { Text(stringResource(R.string.timeline_preset_even)) })
                                AssistChip(onClick = {
                                    val odds = (0..7).filter { it % 2 != 0 }
                                    val updated = element.tracks.toMutableList()
                                    updated[trackIndex] = track.copy(target = TargetGroup(odds))
                                    onUpdate(element.copy(tracks = updated))
                                }, label = { Text(stringResource(R.string.timeline_preset_odd)) })
                            }
                        }
                        is TargetRing -> {
                            // Визуально показываем кольцо без интерактива
                            RingSelector(
                                selected = (0..7).toSet(),
                                onToggle = {},
                                modifier = Modifier.size(160.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                AssistChip(onClick = { /* already all */ }, label = { Text(stringResource(R.string.timeline_preset_all)) })
                                AssistChip(onClick = {
                                    val updated = element.tracks.toMutableList()
                                    updated[trackIndex] = track.copy(target = TargetGroup(emptyList()))
                                    onUpdate(element.copy(tracks = updated))
                                }, label = { Text(stringResource(R.string.timeline_preset_none)) })
                                AssistChip(onClick = {
                                    val evens = (0..7).filter { it % 2 == 0 }
                                    val updated = element.tracks.toMutableList()
                                    updated[trackIndex] = track.copy(target = TargetGroup(evens))
                                    onUpdate(element.copy(tracks = updated))
                                }, label = { Text(stringResource(R.string.timeline_preset_even)) })
                                AssistChip(onClick = {
                                    val odds = (0..7).filter { it % 2 != 0 }
                                    val updated = element.tracks.toMutableList()
                                    updated[trackIndex] = track.copy(target = TargetGroup(odds))
                                    onUpdate(element.copy(tracks = updated))
                                }, label = { Text(stringResource(R.string.timeline_preset_odd)) })
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.timeline_priority), style = MaterialTheme.typography.labelLarge)
                        Slider(
                            value = track.priority.toFloat(),
                            onValueChange = {
                                val updated = element.tracks.toMutableList()
                                updated[trackIndex] = track.copy(priority = it.toInt())
                                onUpdate(element.copy(tracks = updated))
                            },
                            valueRange = 0f..10f,
                            steps = 9,
                            modifier = Modifier.weight(1f)
                        )
                        Text(track.priority.toString(), color = MaterialTheme.colorScheme.primary)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = track.mixMode == MixMode.OVERRIDE,
                            onClick = {
                                val updated = element.tracks.toMutableList()
                                updated[trackIndex] = track.copy(mixMode = MixMode.OVERRIDE)
                                onUpdate(element.copy(tracks = updated))
                            },
                            label = { Text(stringResource(R.string.timeline_mix_override)) }
                        )
                        FilterChip(
                            selected = track.mixMode == MixMode.ADDITIVE,
                            onClick = {
                                val updated = element.tracks.toMutableList()
                                updated[trackIndex] = track.copy(mixMode = MixMode.ADDITIVE)
                                onUpdate(element.copy(tracks = updated))
                            },
                            label = { Text(stringResource(R.string.timeline_mix_additive)) }
                        )
                    }

                    Text(stringResource(R.string.timeline_clips), style = MaterialTheme.typography.titleSmall)

                    ElevatedButton(onClick = {
                        val clip = TimelineClip(startMs = 0, durationMs = 300, color = "#FF0000")
                        val updated = element.tracks.toMutableList()
                        updated[trackIndex] = track.copy(clips = track.clips + clip)
                        onUpdate(element.copy(tracks = updated))
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.timeline_add_clip))
                    }

                    track.clips.forEachIndexed { cIndex, clip ->
                        ElevatedCard {
                            Column(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(stringResource(R.string.timeline_clip_start), style = MaterialTheme.typography.labelLarge)
                                    Text(stringResource(R.string.time_format_ms, clip.startMs), color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = clip.startMs.toFloat(),
                                    onValueChange = {
                                        val updatedTracks = element.tracks.toMutableList()
                                        val trackClips = track.clips.toMutableList()
                                        trackClips[cIndex] = clip.copy(startMs = it.toInt().coerceIn(0, element.durationMs))
                                        updatedTracks[trackIndex] = track.copy(clips = trackClips)
                                        onUpdate(element.copy(tracks = updatedTracks))
                                    },
                                    valueRange = 0f..element.durationMs.toFloat(),
                                    steps = 50
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(stringResource(R.string.timeline_clip_duration), style = MaterialTheme.typography.labelLarge)
                                    Text(stringResource(R.string.time_format_ms, clip.durationMs), color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = clip.durationMs.toFloat(),
                                    onValueChange = {
                                        val updatedTracks = element.tracks.toMutableList()
                                        val trackClips = track.clips.toMutableList()
                                        trackClips[cIndex] = clip.copy(durationMs = it.toInt().coerceAtLeast(100))
                                        updatedTracks[trackIndex] = track.copy(clips = trackClips)
                                        onUpdate(element.copy(tracks = updatedTracks))
                                    },
                                    valueRange = 100f..element.durationMs.toFloat(),
                                    steps = 50
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(stringResource(R.string.timeline_clip_fade_in), style = MaterialTheme.typography.labelLarge)
                                    Text(stringResource(R.string.time_format_ms, clip.fadeInMs), color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = clip.fadeInMs.toFloat(),
                                    onValueChange = {
                                        val updatedTracks = element.tracks.toMutableList()
                                        val trackClips = track.clips.toMutableList()
                                        trackClips[cIndex] = clip.copy(fadeInMs = it.toInt().coerceIn(0, clip.durationMs))
                                        updatedTracks[trackIndex] = track.copy(clips = trackClips)
                                        onUpdate(element.copy(tracks = updatedTracks))
                                    },
                                    valueRange = 0f..clip.durationMs.toFloat(),
                                    steps = 50
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(stringResource(R.string.timeline_clip_fade_out), style = MaterialTheme.typography.labelLarge)
                                    Text(stringResource(R.string.time_format_ms, clip.fadeOutMs), color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = clip.fadeOutMs.toFloat(),
                                    onValueChange = {
                                        val updatedTracks = element.tracks.toMutableList()
                                        val trackClips = track.clips.toMutableList()
                                        trackClips[cIndex] = clip.copy(fadeOutMs = it.toInt().coerceIn(0, clip.durationMs))
                                        updatedTracks[trackIndex] = track.copy(clips = trackClips)
                                        onUpdate(element.copy(tracks = updatedTracks))
                                    },
                                    valueRange = 0f..clip.durationMs.toFloat(),
                                    steps = 50
                                )

                                ColorPicker(
                                    color = clip.color,
                                    onColorChange = {
                                        val updatedTracks = element.tracks.toMutableList()
                                        val trackClips = track.clips.toMutableList()
                                        trackClips[cIndex] = clip.copy(color = it)
                                        updatedTracks[trackIndex] = track.copy(clips = trackClips)
                                        onUpdate(element.copy(tracks = updatedTracks))
                                    },
                                    label = stringResource(R.string.pattern_element_color_label)
                                )

                                TextButton(onClick = {
                                    val updatedTracks = element.tracks.toMutableList()
                                    val trackClips = track.clips.toMutableList()
                                    trackClips.removeAt(cIndex)
                                    updatedTracks[trackIndex] = track.copy(clips = trackClips)
                                    onUpdate(element.copy(tracks = updatedTracks))
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.timeline_remove_clip))
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.timeline_remove_clip))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
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
