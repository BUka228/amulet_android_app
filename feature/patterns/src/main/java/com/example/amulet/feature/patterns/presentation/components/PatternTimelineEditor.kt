package com.example.amulet.feature.patterns.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.*
import com.example.amulet.core.design.components.textfield.AmuletTextField
import androidx.core.graphics.toColorInt

private enum class DurationUnit { MS, S }
private enum class Tool { BRUSH, ERASER, FILL }

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun TimelineEditorContent(
    element: PatternElementTimeline,
    onUpdate: (PatternElement) -> Unit
) {
    var duration by remember(element) { mutableIntStateOf(element.durationMs) }
    var tick by remember(element) { mutableIntStateOf(element.tickMs) }

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
    var selectedLed by remember { mutableIntStateOf(0) }
    var selectedTick by remember { mutableIntStateOf(0) }

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

    // Tools & palette & history
    var tool by remember { mutableStateOf(Tool.BRUSH) }
    var currentColor by remember { mutableStateOf(gridColors.getOrNull(selectedLed)?.getOrNull(selectedTick) ?: ledColors[selectedLed]) }
    val presetColors = listOf("#FFFFFF", "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#00FFFF", "#FF00FF")
    val recentColors = remember { mutableStateListOf<String>() }
    val undoStack = remember { mutableStateListOf<Array<Array<String?>>>() }
    val redoStack = remember { mutableStateListOf<Array<Array<String?>>>() }
    var dragEnabled by remember { mutableStateOf(true) }

    fun copyGrid(src: Array<Array<String?>>): Array<Array<String?>> = Array(src.size) { i -> src[i].clone() }
    fun pushUndo() {
        undoStack.add(copyGrid(gridColors))
        if (undoStack.size > 50) undoStack.removeAt(0)
        redoStack.clear()
    }
    fun applyAndUpdate() {
        onUpdate(
            PatternElementTimeline(
                durationMs = duration,
                tickMs = tick,
                tracks = rebuildTracks()
            )
        )
    }
    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(copyGrid(gridColors))
            gridColors = undoStack.removeAt(undoStack.lastIndex)
            applyAndUpdate()
        }
    }
    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(copyGrid(gridColors))
            gridColors = redoStack.removeAt(redoStack.lastIndex)
            applyAndUpdate()
        }
    }
    var isPainting by remember { mutableStateOf(false) }
    var strokePushed by remember { mutableStateOf(false) }
    fun beginStroke() { isPainting = true; strokePushed = false }
    fun endStroke() { isPainting = false; applyAndUpdate() }

    fun paintCell(led: Int, t: Int, color: String?) {
        if (led !in 0 until ledsCount || t !in 0 until gridColors[led].size) return
        if (!strokePushed) { pushUndo(); strokePushed = true }
        val g = Array(ledsCount) { i -> gridColors[i].clone() }
        g[led][t] = color
        gridColors = g
    }
    fun applyBrush(led: Int, t: Int) = paintCell(led, t, currentColor)
    fun applyEraser(led: Int, t: Int) {
        if (led !in 0 until ledsCount || t !in 0 until gridColors[led].size) return
        paintCell(led, t, null)
    }
    fun applyFill(led: Int, t: Int) {
        if (led !in 0 until ledsCount || t !in 0 until gridColors[led].size) return
        if (!strokePushed) { pushUndo(); strokePushed = true }
        val row = gridColors[led]
        val target = row[t]
        var l = t
        var r = t
        while (l - 1 >= 0 && row[l - 1] == target) l--
        while (r + 1 < row.size && row[r + 1] == target) r++
        val g = Array(ledsCount) { i -> gridColors[i].clone() }
        for (i in l..r) g[led][i] = currentColor
        gridColors = g
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Toolbar: tools, palette, undo/redo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { tool = Tool.BRUSH },
                    label = { Text("Кисть") },
                    leadingIcon = if (tool == Tool.BRUSH) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
                AssistChip(
                    onClick = { tool = Tool.ERASER },
                    label = { Text("Ластик") },
                    leadingIcon = if (tool == Tool.ERASER) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
                AssistChip(
                    onClick = { tool = Tool.FILL },
                    label = { Text("Заливка") },
                    leadingIcon = if (tool == Tool.FILL) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
                AssistChip(
                    onClick = { dragEnabled = !dragEnabled },
                    label = { Text(if (dragEnabled) "Drag: Вкл" else "Drag: Выкл") },
                    leadingIcon = if (dragEnabled) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
                IconButton(onClick = { undo() }, enabled = undoStack.isNotEmpty()) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                }
                IconButton(onClick = { redo() }, enabled = redoStack.isNotEmpty()) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                }
            }

        }

        // Palette: Presets
        Text(text = "Палитра", style = MaterialTheme.typography.labelLarge)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            presetColors.forEach { col ->
                val bg = try { val c = android.graphics.Color.parseColor(col); androidx.compose.ui.graphics.Color(c) } catch (_: Throwable) { MaterialTheme.colorScheme.primary }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(bg, shape = CircleShape)
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = CircleShape)
                        .clickable {
                            currentColor = col
                            // также сразу красим выбранную ячейку кистью, если она активна
                            if (selectedLed in 0 until ledsCount && selectedTick in 0 until gridColors[selectedLed].size) {
                                applyBrush(selectedLed, selectedTick)
                            }
                        }
                )
            }
        }

        // Palette: Recents
        if (recentColors.isNotEmpty()) {
            Text(text = "Недавние", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                recentColors.forEach { col ->
                    val bg = try { val c = android.graphics.Color.parseColor(col); androidx.compose.ui.graphics.Color(c) } catch (_: Throwable) { MaterialTheme.colorScheme.primary }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(bg.copy(alpha = 0.9f), shape = CircleShape)
                            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = CircleShape)
                            .clickable { currentColor = col },
                        contentAlignment = Alignment.Center
                    ) {
                        // small dot indicates "recent"
                        Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.surface, shape = CircleShape))
                    }
                }
            }
        }

        TimelineGrid(
            ticks = ticksCount,
            ledCount = ledsCount,
            grid = gridColors,
            colors = ledColors,
            selected = selectedLed to selectedTick,
            dragEnabled = dragEnabled,
            onToggle = { led, t ->
                // single tap paint
                beginStroke()
                when (tool) {
                    Tool.BRUSH -> applyBrush(led, t)
                    Tool.ERASER -> applyEraser(led, t)
                    Tool.FILL -> applyFill(led, t)
                }
                endStroke()
                selectedLed = led
                selectedTick = t
            },
            onSelect = { led, t ->
                selectedLed = led
                selectedTick = t
            },
            onDragStart = { led, t ->
                beginStroke()
                when (tool) {
                    Tool.BRUSH -> applyBrush(led, t)
                    Tool.ERASER -> applyEraser(led, t)
                    Tool.FILL -> applyFill(led, t)
                }
                selectedLed = led
                selectedTick = t
            },
            onDragOver = { led, t ->
                when (tool) {
                    Tool.BRUSH -> applyBrush(led, t)
                    Tool.ERASER -> applyEraser(led, t)
                    Tool.FILL -> applyFill(led, t)
                }
                selectedLed = led
                selectedTick = t
            },
            onDragEnd = {
                endStroke()
            }
        )

        ColorPicker(
            color = gridColors.getOrNull(selectedLed)?.getOrNull(selectedTick) ?: currentColor,
            onColorChange = { c ->
                // обновляем цвет выбранной ячейки и запоминаем последний цвет диода
                currentColor = c
                recentColors.remove(c)
                recentColors.add(0, c)
                if (recentColors.size > 12) recentColors.removeLast()
                if (selectedLed in 0 until ledsCount && selectedTick in 0 until gridColors[selectedLed].size) {
                    beginStroke()
                    paintCell(selectedLed, selectedTick, c)
                    endStroke()
                }
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
    dragEnabled: Boolean,
    onToggle: (led: Int, tick: Int) -> Unit,
    onSelect: (led: Int, tick: Int) -> Unit,
    onDragStart: (led: Int, tick: Int) -> Unit,
    onDragOver: (led: Int, tick: Int) -> Unit,
    onDragEnd: () -> Unit
) {
    val scrollState = rememberScrollState()
    val cellSize = 24.dp
    val gap = 4.dp
    val baseModifier = Modifier
    val dragModifier = if (dragEnabled) {
        Modifier.pointerInput(ticks, ledCount) {
            awaitPointerEventScope {
                val cellSizePx = cellSize.toPx()
                val gapPx = gap.toPx()
                var dragging = false
                var lastLed = -1
                var lastTick = -1
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull() ?: continue
                    val x = change.position.x + scrollState.value
                    val y = change.position.y
                    val tick = (x / (cellSizePx + gapPx)).toInt().coerceIn(0, ticks - 1)
                    val led = (y / (cellSizePx + gapPx)).toInt().coerceIn(0, ledCount - 1)
                    if (!dragging && change.pressed && !change.previousPressed) {
                        dragging = true
                        lastLed = led
                        lastTick = tick
                        onDragStart(led, tick)
                    } else if (dragging && change.pressed) {
                        if (tick != lastTick || led != lastLed) {
                            onDragOver(led, tick)
                            lastTick = tick
                            lastLed = led
                        }
                    } else if (dragging && !change.pressed) {
                        dragging = false
                        onDragEnd()
                    }
                }
            }
        }
    } else Modifier
    Column(
        verticalArrangement = Arrangement.spacedBy(gap),
        modifier = baseModifier.then(dragModifier)
    ) {
        repeat(ledCount) { led ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(gap),
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
                            .size(cellSize)
                            .background(bg, shape = CircleShape)
                            .border(width = 1.dp, color = border, shape = CircleShape)
                            .clickable { onToggle(led, t) }
                    )
                }
            }
        }
    }
}
