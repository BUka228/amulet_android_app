package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.*
import com.example.amulet.core.design.components.textfield.AmuletTextField

private enum class DurationUnit { MS, S }

@Composable
fun TimelineEditorContent(
    element: PatternElementTimeline,
    onUpdate: (PatternElement) -> Unit
) {
    var duration by remember(element) { mutableStateOf(element.durationMs) }
    var tick by remember(element) { mutableStateOf(element.tickMs) }

    val ledsCount = 8
    val ticksCount = remember(duration, tick) { (duration / tick).coerceAtLeast(1) }

    val initialGridColors = remember(element, ticksCount) {
        Array(ledsCount) { arrayOfNulls<String>(ticksCount) }.also { grid ->
            element.tracks.forEach { track ->
                val indices: List<Int> = when (val t = track.target) {
                    is TargetLed -> listOf(t.index)
                    is TargetGroup -> t.indices
                    is TargetRing -> (0 until ledsCount).toList()
                }.filter { it in 0 until ledsCount }

                track.clips.forEach { clip ->
                    val startTick = (clip.startMs / tick).coerceAtLeast(0)
                    val endTick = ((clip.startMs + clip.durationMs - 1) / tick).coerceAtLeast(startTick)
                    indices.forEach { led ->
                        for (c in startTick..endTick.coerceAtMost(ticksCount - 1)) {
                            grid[led][c] = clip.color
                        }
                    }
                }
            }
        }
    }

    var gridColors by remember { mutableStateOf(initialGridColors) }

    val initialColors = remember(element) {
        MutableList(ledsCount) { idx ->
            val color = element.tracks
                .firstOrNull { it.target is TargetLed && (it.target as TargetLed).index == idx }
                ?.clips?.firstOrNull()?.color
            color ?: "#FFFFFF"
        }
    }

    var ledColors by remember { mutableStateOf(initialColors) }
    var selectedLed by remember { mutableStateOf(0) }
    var selectedTick by remember { mutableStateOf(0) }

    var unit by remember { mutableStateOf(DurationUnit.MS) }
    var editingDuration by remember { mutableStateOf(false) }
    var durationText by remember(duration, unit) {
        mutableStateOf(
            if (unit == DurationUnit.MS) duration.toString() else (duration / 1000f).toString()
        )
    }

    fun rebuildTracks(): List<TimelineTrack> {
        val tracks = mutableListOf<TimelineTrack>()
        for (led in 0 until ledsCount) {
            val row = gridColors[led]
            var c = 0
            val clips = mutableListOf<TimelineClip>()
            val maxTicks = row.size
            while (c < maxTicks) {
                val color = row[c]
                if (color != null) {
                    val start = c
                    var end = c
                    while (end + 1 < maxTicks && row[end + 1] == color) end++
                    val startMs = start * tick
                    val durationMs = (end - start + 1) * tick
                    clips.add(
                        TimelineClip(
                            startMs = startMs,
                            durationMs = durationMs,
                            color = color
                        )
                    )
                    c = end + 1
                } else c++
            }
            if (clips.isNotEmpty()) {
                tracks.add(
                    TimelineTrack(
                        target = TargetLed(led),
                        priority = 0,
                        mixMode = MixMode.OVERRIDE,
                        clips = clips
                    )
                )
            }
        }
        return tracks
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TimelineGrid(
            ticks = ticksCount,
            ledCount = ledsCount,
            grid = gridColors,
            colors = ledColors,
            selected = selectedLed to selectedTick,
            onToggle = { led, t ->
                val copy = Array(ledsCount) { i -> gridColors[i].clone() }
                if (t in 0 until copy[led].size) {
                    copy[led][t] = if (copy[led][t] == null) ledColors[led] else null
                    gridColors = copy
                }
                selectedLed = led
                selectedTick = t
                onUpdate(
                    PatternElementTimeline(
                        durationMs = duration,
                        tickMs = tick,
                        tracks = rebuildTracks()
                    )
                )
            },
            onSelect = { led, t ->
                selectedLed = led
                selectedTick = t
            }
        )

        ColorPicker(
            color = gridColors.getOrNull(selectedLed)?.getOrNull(selectedTick) ?: ledColors[selectedLed],
            onColorChange = { c ->
                // обновляем цвет выбранной ячейки и запоминаем последний цвет диода
                val gridCopy = Array(ledsCount) { i -> gridColors[i].clone() }
                if (selectedLed in 0 until ledsCount && selectedTick in 0 until gridCopy[selectedLed].size) {
                    gridCopy[selectedLed][selectedTick] = c
                    gridColors = gridCopy
                }
                val colorCopy = ledColors.toMutableList()
                colorCopy[selectedLed] = c
                ledColors = colorCopy
                onUpdate(
                    PatternElementTimeline(
                        durationMs = duration,
                        tickMs = tick,
                        tracks = rebuildTracks()
                    )
                )
            },
            label = stringResource(R.string.pattern_element_color_label)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.pattern_element_duration_label), style = MaterialTheme.typography.labelLarge)
                    AssistChip(
                        onClick = { unit = DurationUnit.MS },
                        label = { Text("мс") },
                        leadingIcon = if (unit == DurationUnit.MS) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                    )
                    AssistChip(
                        onClick = { unit = DurationUnit.S },
                        label = { Text("с") },
                        leadingIcon = if (unit == DurationUnit.S) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                    )
                }
                Text(
                    text = if (unit == DurationUnit.MS) "$duration мс" else String.format("%.2f с", duration / 1000f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { editingDuration = true }
                )
            }
            if (editingDuration) {
                AlertDialog(
                    onDismissRequest = { editingDuration = false },
                    title = { Text(text = stringResource(R.string.pattern_element_duration_label)) },
                    text = {
                        AmuletTextField(
                            value = durationText,
                            onValueChange = { txt ->
                                val filtered = txt.replace(',', '.').filter { it.isDigit() || it == '.' }
                                durationText = filtered
                            },
                            label = if (unit == DurationUnit.MS) "мс" else "с"
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val v = durationText.toFloatOrNull()
                            if (v != null) {
                                val ms = if (unit == DurationUnit.MS) v.toInt() else (v * 1000).toInt()
                                val nd = ms.coerceIn(200, 60000)
                                if (nd != duration) {
                                    duration = nd
                                    val newTicks = (duration / tick).coerceAtLeast(1)
                                    val newGrid = Array(ledsCount) { arrayOfNulls<String>(newTicks) }
                                    for (led in 0 until ledsCount) {
                                        for (t in 0 until minOf(gridColors[led].size, newTicks)) newGrid[led][t] = gridColors[led][t]
                                    }
                                    gridColors = newGrid
                                    selectedTick = selectedTick.coerceIn(0, newTicks - 1)
                                    onUpdate(
                                        PatternElementTimeline(
                                            durationMs = duration,
                                            tickMs = tick,
                                            tracks = rebuildTracks()
                                        )
                                    )
                                }
                            }
                            editingDuration = false
                        }) { Text(text = stringResource(android.R.string.ok)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingDuration = false }) { Text(text = stringResource(android.R.string.cancel)) }
                    }
                )
            }
            Slider(
                value = duration.toFloat(),
                onValueChange = {
                    val nd = it.toInt().coerceIn(200, 60000)
                    if (nd != duration) {
                        duration = nd
                        val newTicks = (duration / tick).coerceAtLeast(1)
                        if (newTicks != ticksCount) {
                            val newGrid = Array(ledsCount) { arrayOfNulls<String>(newTicks) }
                            for (led in 0 until ledsCount) {
                                for (t in 0 until minOf(gridColors[led].size, newTicks)) {
                                    newGrid[led][t] = gridColors[led][t]
                                }
                            }
                            gridColors = newGrid
                            selectedTick = selectedTick.coerceIn(0, newTicks - 1)
                        }
                        onUpdate(
                            PatternElementTimeline(
                                durationMs = duration,
                                tickMs = tick,
                                tracks = rebuildTracks()
                            )
                        )
                    }
                },
                valueRange = 200f..60000f,
                steps = 100
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.pattern_element_speed_label), style = MaterialTheme.typography.labelLarge)
                Text(text = stringResource(R.string.time_format_ms, tick), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value = tick.toFloat(),
                onValueChange = {
                    val nd = it.toInt().coerceIn(10, 1000)
                    if (nd != tick) {
                        tick = nd
                        val newTicks = (duration / tick).coerceAtLeast(1)
                        val newGrid = Array(ledsCount) { arrayOfNulls<String>(newTicks) }
                        for (led in 0 until ledsCount) {
                            // соберем интервалы цвета в старой сетке в мс
                            val intervals = mutableListOf<Triple<Int, Int, String>>()
                            var t0 = -1
                            var curColor: String? = null
                            for (t in 0 until gridColors[led].size) {
                                val color = gridColors[led][t]
                                if (color != null && t0 == -1) {
                                    t0 = t; curColor = color
                                }
                                val atEnd = t == gridColors[led].lastIndex
                                if ((color == null || color != curColor || atEnd) && t0 != -1) {
                                    val end = if (atEnd && color != null && color == curColor) t else t - 1
                                    val startMs = t0 * (element.tickMs)
                                    val endMs = (end + 1) * (element.tickMs)
                                    intervals.add(Triple(startMs, endMs, curColor!!))
                                    t0 = -1
                                }
                            }
                            intervals.forEach { (s, e, col) ->
                                val sTick = (s / tick).coerceAtLeast(0)
                                val eTick = ((e - 1) / tick).coerceAtLeast(sTick)
                                for (tt in sTick..eTick.coerceAtMost(newTicks - 1)) newGrid[led][tt] = col
                            }
                        }
                        gridColors = newGrid
                        selectedTick = selectedTick.coerceIn(0, newTicks - 1)
                        onUpdate(
                            PatternElementTimeline(
                                durationMs = duration,
                                tickMs = tick,
                                tracks = rebuildTracks()
                            )
                        )
                    }
                },
                valueRange = 10f..1000f,
                steps = 98
            )
        }

        // Tick grid already shown above
    }
}

@Composable
private fun TimelineGrid(
    ticks: Int,
    ledCount: Int,
    grid: Array<Array<String?>>,
    colors: List<String>,
    selected: Pair<Int, Int>,
    onToggle: (led: Int, tick: Int) -> Unit,
    onSelect: (led: Int, tick: Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(ledCount) { led ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                repeat(ticks) { t ->
                    val colorStr = if (t in 0 until grid[led].size) grid[led][t] else null
                    val bg = if (colorStr != null) {
                        try {
                            val c = android.graphics.Color.parseColor(colorStr)
                            androidx.compose.ui.graphics.Color(c)
                        } catch (_: Throwable) {
                            MaterialTheme.colorScheme.primary
                        }
                    } else MaterialTheme.colorScheme.surfaceVariant
                    val border = if (selected.first == led && selected.second == t) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(28.dp)
                            .background(bg, shape = MaterialTheme.shapes.extraSmall)
                            .border(width = 1.dp, color = border, shape = MaterialTheme.shapes.extraSmall)
                            .clickable { onToggle(led, t) }
                    )
                }
            }
        }
    }
}
