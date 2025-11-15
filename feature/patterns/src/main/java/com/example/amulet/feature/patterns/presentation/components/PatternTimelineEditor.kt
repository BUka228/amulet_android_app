package com.example.amulet.feature.patterns.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.delay
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
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.*
import com.example.amulet.core.design.components.textfield.AmuletTextField
import androidx.core.graphics.toColorInt

private enum class DurationUnit { MS, S }
private enum class Tool { BRUSH, ERASER, FILL, EYEDROPPER }

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

    // Zoom and autoscroll settings
    var cellSizeDp by remember { mutableStateOf(24.dp) }
    var gapDp by remember { mutableStateOf(4.dp) }
    var autoThresholdPx by remember { mutableStateOf(48f) }
    var autoMinSpeedPx by remember { mutableStateOf(8f) }
    var autoMaxSpeedPx by remember { mutableStateOf(48f) }
    var autoAccel by remember { mutableStateOf(0.5f) }
    var autoEnabled by remember { mutableStateOf(true) }
    var showAdvanced by remember { mutableStateOf(false) }

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
                    label = { Text(stringResource(R.string.timeline_tool_brush)) },
                    leadingIcon = if (tool == Tool.BRUSH) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
                AssistChip(
                    onClick = { tool = Tool.ERASER },
                    label = { Text(stringResource(R.string.timeline_tool_eraser)) },
                    leadingIcon = if (tool == Tool.ERASER) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
                AssistChip(
                    onClick = { tool = Tool.FILL },
                    label = { Text(stringResource(R.string.timeline_tool_fill)) },
                    leadingIcon = if (tool == Tool.FILL) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
                AssistChip(
                    onClick = { dragEnabled = !dragEnabled },
                    label = { Text(if (dragEnabled) stringResource(R.string.timeline_drag_on) else stringResource(R.string.timeline_drag_off)) },
                    leadingIcon = if (dragEnabled) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
                IconButton(onClick = { undo() }, enabled = undoStack.isNotEmpty()) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(R.string.timeline_undo))
                }
                IconButton(onClick = { redo() }, enabled = redoStack.isNotEmpty()) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Redo, contentDescription = stringResource(R.string.timeline_redo))
                }
            }

        }

        // Palette: Presets + Recent (combined)
        var showAdvancedPicker by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.timeline_palette), style = MaterialTheme.typography.labelLarge)
            val isEyedropper = tool == Tool.EYEDROPPER
            val paletteTint = try {
                androidx.compose.ui.graphics.Color(currentColor.toColorInt())
            } catch (_: Throwable) {
                MaterialTheme.colorScheme.primary
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconToggleButton(
                    checked = isEyedropper,
                    onCheckedChange = { checked ->
                        tool = if (checked) Tool.EYEDROPPER else Tool.BRUSH
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Colorize,
                        contentDescription = stringResource(R.string.timeline_tool_eyedropper),
                        tint = if (isEyedropper) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showAdvancedPicker = true }) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = stringResource(R.string.color_picker_advanced),
                        tint = paletteTint
                    )
                }
            }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            maxLines = 3
        ) {
            val combined = buildList {
                presetColors.forEach { add(it to false) }
                recentColors.forEach { add(it to true) }
            }
            combined.forEach { (col, isRecent) ->
                val bg = try {
                    val c = col.toColorInt()
                    androidx.compose.ui.graphics.Color(c)
                } catch (_: Throwable) { MaterialTheme.colorScheme.primary }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isRecent) bg.copy(alpha = 0.9f) else bg, shape = CircleShape)
                        .border(
                            width = 1.dp,
                            color = if (isRecent) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                        .clickable {
                            currentColor = col
                            if (selectedLed in 0 until ledsCount && selectedTick in 0 until gridColors[selectedLed].size) {
                                applyBrush(selectedLed, selectedTick)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isRecent) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                        )
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
            cellSize = cellSizeDp,
            gap = gapDp,
            autoEnabled = autoEnabled,
            autoThresholdPx = autoThresholdPx,
            autoMinSpeedPx = autoMinSpeedPx,
            autoMaxSpeedPx = autoMaxSpeedPx,
            autoAccel = autoAccel,
            onToggle = { led, t ->
                // single tap paint
                if (tool == Tool.EYEDROPPER) {
                    val picked = gridColors.getOrNull(led)?.getOrNull(t)
                    if (picked != null) currentColor = picked
                } else {
                    beginStroke()
                    when (tool) {
                        Tool.BRUSH -> applyBrush(led, t)
                        Tool.ERASER -> applyEraser(led, t)
                        Tool.FILL -> applyFill(led, t)
                        Tool.EYEDROPPER -> Unit
                    }
                    endStroke()
                }
                selectedLed = led
                selectedTick = t
            },
            onSelect = { led, t ->
                selectedLed = led
                selectedTick = t
            },
            onDragStart = { led, t ->
                if (tool == Tool.EYEDROPPER) {
                    val picked = gridColors.getOrNull(led)?.getOrNull(t)
                    if (picked != null) currentColor = picked
                } else {
                    beginStroke()
                    when (tool) {
                        Tool.BRUSH -> applyBrush(led, t)
                        Tool.ERASER -> applyEraser(led, t)
                        Tool.FILL -> applyFill(led, t)
                        Tool.EYEDROPPER -> Unit
                    }
                }
                selectedLed = led
                selectedTick = t
            },
            onDragOver = { led, t ->
                if (tool == Tool.EYEDROPPER) {
                    val picked = gridColors.getOrNull(led)?.getOrNull(t)
                    if (picked != null) currentColor = picked
                } else {
                    when (tool) {
                        Tool.BRUSH -> applyBrush(led, t)
                        Tool.ERASER -> applyEraser(led, t)
                        Tool.FILL -> applyFill(led, t)
                        Tool.EYEDROPPER -> Unit
                    }
                }
                selectedLed = led
                selectedTick = t
            },
            onDragEnd = {
                if (tool != Tool.EYEDROPPER) endStroke()
            }
        )

        // Переключатель расширенных настроек
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            AssistChip(
                onClick = { showAdvanced = !showAdvanced },
                label = {
                    Text(if (showAdvanced) stringResource(R.string.timeline_settings_hide) else stringResource(R.string.timeline_settings_show))
                }
            )
            Spacer(Modifier.width(8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        AnimatedVisibility(
            visible = showAdvanced,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Масштаб сетки — компактные +/-
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.timeline_zoom), style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.timeline_cell_size))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { cellSizeDp = (cellSizeDp.value - 2f).coerceIn(16f, 40f).dp }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                            Text(text = String.format("%d dp", cellSizeDp.value.toInt()))
                            IconButton(onClick = { cellSizeDp = (cellSizeDp.value + 2f).coerceIn(16f, 40f).dp }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.timeline_gap))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { gapDp = (gapDp.value - 1f).coerceIn(0f, 8f).dp }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                            Text(text = String.format("%d dp", gapDp.value.toInt()))
                            IconButton(onClick = { gapDp = (gapDp.value + 1f).coerceIn(0f, 8f).dp }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
                        }
                    }
                }

                // Расширенные параметры автоскролла — заголовок с выключателем и компактные +/-
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.timeline_autoscroll), style = MaterialTheme.typography.labelLarge)
                        Switch(checked = autoEnabled, onCheckedChange = { autoEnabled = it })
                    }
                    AnimatedVisibility(
                        visible = autoEnabled,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = stringResource(R.string.timeline_autoscroll_threshold))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { autoThresholdPx = (autoThresholdPx - 4f).coerceIn(16f, 96f) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                                    Text(text = String.format("%d px", autoThresholdPx.toInt()))
                                    IconButton(onClick = { autoThresholdPx = (autoThresholdPx + 4f).coerceIn(16f, 96f) }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = stringResource(R.string.timeline_autoscroll_min_speed))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { autoMinSpeedPx = (autoMinSpeedPx - 2f).coerceIn(0f, autoMaxSpeedPx) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                                    Text(text = String.format("%d px/шаг", autoMinSpeedPx.toInt()))
                                    IconButton(onClick = { autoMinSpeedPx = (autoMinSpeedPx + 2f).coerceIn(0f, autoMaxSpeedPx) }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = stringResource(R.string.timeline_autoscroll_max_speed))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { autoMaxSpeedPx = (autoMaxSpeedPx - 2f).coerceIn(autoMinSpeedPx, 96f) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                                    Text(text = String.format("%d px/шаг", autoMaxSpeedPx.toInt()))
                                    IconButton(onClick = { autoMaxSpeedPx = (autoMaxSpeedPx + 2f).coerceIn(autoMinSpeedPx, 96f) }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = stringResource(R.string.timeline_autoscroll_acceleration))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { autoAccel = (autoAccel - 0.05f).coerceIn(0.1f, 1.0f) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                                    Text(text = String.format("%.2f", autoAccel))
                                    IconButton(onClick = { autoAccel = (autoAccel + 0.05f).coerceIn(0.1f, 1.0f) }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
                                }
                            }
                        }
                    }
                }
            }
        }

        AdvancedColorPickerSheet(
            open = showAdvancedPicker,
            initialColor = gridColors.getOrNull(selectedLed)?.getOrNull(selectedTick) ?: currentColor,
            onDismiss = { showAdvancedPicker = false },
            onPick = { c ->
                currentColor = c
                recentColors.remove(c)
                recentColors.add(0, c)
                if (recentColors.size > 12) recentColors.removeLast()
                if (selectedLed in 0 until ledsCount && selectedTick in 0 until gridColors[selectedLed].size) {
                    beginStroke()
                    paintCell(selectedLed, selectedTick, c)
                    endStroke()
                }
                showAdvancedPicker = false
            }
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
                        label = { Text(stringResource(R.string.duration_unit_ms)) },
                        leadingIcon = if (unit == DurationUnit.MS) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                    )
                    AssistChip(
                        onClick = { unit = DurationUnit.S },
                        label = { Text(stringResource(R.string.duration_unit_s)) },
                        leadingIcon = if (unit == DurationUnit.S) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                    )
                }
                Text(
                    text = if (unit == DurationUnit.MS) {
                        String.format("%d %s", duration, stringResource(R.string.duration_unit_ms))
                    } else {
                        String.format("%.2f %s", duration / 1000f, stringResource(R.string.duration_unit_s))
                    },
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
                            label = if (unit == DurationUnit.MS) stringResource(R.string.duration_unit_ms) else stringResource(R.string.duration_unit_s)
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
    cellSize: androidx.compose.ui.unit.Dp,
    gap: androidx.compose.ui.unit.Dp,
    autoEnabled: Boolean,
    autoThresholdPx: Float,
    autoMinSpeedPx: Float,
    autoMaxSpeedPx: Float,
    autoAccel: Float,
    onToggle: (led: Int, tick: Int) -> Unit,
    onSelect: (led: Int, tick: Int) -> Unit,
    onDragStart: (led: Int, tick: Int) -> Unit,
    onDragOver: (led: Int, tick: Int) -> Unit,
    onDragEnd: () -> Unit
) {
    val scrollState = rememberScrollState()
    var containerWidthPx by remember { mutableIntStateOf(0) }
    var autoDir by remember { mutableIntStateOf(0) } // -1 left, 1 right, 0 none
    var autoSpeed by remember { mutableStateOf(0f) } // px per frame
    var draggingState by remember { mutableStateOf(false) }
    LaunchedEffect(draggingState, autoDir, autoSpeed, autoEnabled) {
        while (draggingState && autoEnabled && autoDir != 0 && autoSpeed > 0f) {
            val step = (autoSpeed * autoDir)
            val target = (scrollState.value + step).toInt().coerceIn(0, scrollState.maxValue)
            scrollState.scrollTo(target)
            delay(16)
        }
    }
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
                        draggingState = true
                        lastLed = led
                        lastTick = tick
                        onDragStart(led, tick)
                        // consume to avoid Row.horizontalScroll taking over
                        change.consume()
                    } else if (dragging && change.pressed) {
                        if (tick != lastTick || led != lastLed) {
                            onDragOver(led, tick)
                            lastTick = tick
                            lastLed = led
                        }
                        val localX = change.position.x
                        val threshold = autoThresholdPx
                        val maxSpeed = autoMaxSpeedPx // px/frame
                        val minSpeed = autoMinSpeedPx
                        val rightEdge = containerWidthPx - threshold
                        if (autoEnabled) {
                            val (dir, dist) = when {
                                containerWidthPx > 0 && localX > rightEdge -> 1 to (localX - rightEdge)
                                localX < threshold -> -1 to (threshold - localX)
                                else -> 0 to 0f
                            }
                            autoDir = dir
                            autoSpeed = if (dir == 0) 0f else (minSpeed + dist * autoAccel).coerceAtMost(maxSpeed)
                        } else {
                            autoDir = 0
                            autoSpeed = 0f
                        }
                        // consume movement during active drag so scroll does not hijack it
                        change.consume()
                    } else if (dragging && !change.pressed) {
                        dragging = false
                        draggingState = false
                        autoDir = 0
                        autoSpeed = 0f
                        onDragEnd()
                    }
                }
            }
        }
    } else Modifier
    Column(
        verticalArrangement = Arrangement.spacedBy(gap),
        modifier = baseModifier
            .then(dragModifier)
    ) {
        repeat(ledCount) { led ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = (led + 1).toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(24.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    modifier = Modifier
                        .weight(1f)
                        // measure viewport width on the scrollable row itself for accurate right-edge detection
                        .onSizeChanged { containerWidthPx = it.width }
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
                                .clip(CircleShape)
                                .background(bg, shape = CircleShape)
                                .border(width = 1.dp, color = border, shape = CircleShape)
                                .clickable { onToggle(led, t) }
                        )
                    }
                }
            }
        }
    }
}
