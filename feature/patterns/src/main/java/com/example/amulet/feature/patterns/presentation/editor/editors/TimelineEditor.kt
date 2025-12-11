package com.example.amulet.feature.patterns.presentation.editor.editors

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.textfield.AmuletTextField
import com.example.amulet.feature.patterns.R
import com.example.amulet.feature.patterns.presentation.components.AdvancedColorPickerSheet
import com.example.amulet.shared.domain.patterns.model.Easing
import com.example.amulet.shared.domain.patterns.model.PatternTimeline
import kotlinx.coroutines.delay

private enum class DurationUnit { MS, S }

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun TimelineEditor(
    timeline: PatternTimeline,
    tickMs: Int,
    markersMs: List<Int>,
    onMarkersChange: (List<Int>) -> Unit,
    onUpdate: (PatternTimeline) -> Unit
) {
    val vm: TimelineEditorViewModel = hiltViewModel()
    LaunchedEffect(timeline, tickMs) { vm.initialize(timeline, tickMs, onUpdate) }
    val sNullable by vm.state.collectAsStateWithLifecycle()
    val s = sNullable ?: return
    val ledsCount = s.ledsCount
    val ticksCount = s.ticksCount
    var unit by remember { mutableStateOf(DurationUnit.MS) }
    var editingDuration by remember { mutableStateOf(false) }
    var durationText by remember(s.durationMs, unit) {
        mutableStateOf(
            if (unit == DurationUnit.MS) s.durationMs.toString() else (s.durationMs / 1000f).toString()
        )
    }
    

    // Tools & palette & history
    val tool = s.tool
    val currentColor = s.currentColor
    val presetColors = listOf("#FFFFFF", "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#00FFFF", "#FF00FF")
    val recentColors = remember { mutableStateListOf<String>() }
    val dragEnabled = s.dragEnabled

    // Zoom and autoscroll settings are driven by VM state
    val cellSizeDp = s.cellSizeDp.dp
    val gapDp = s.gapDp.dp
    val autoThresholdPx = s.autoThresholdPx
    val autoMinSpeedPx = s.autoMinSpeedPx
    val autoMaxSpeedPx = s.autoMaxSpeedPx
    val autoAccel = s.autoAccel
    val autoEnabled = s.autoEnabled
    val showAdvanced = s.showAdvanced

    val scrollState = rememberScrollState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.verticalScroll(scrollState)) {
        // Toolbar: tools, palette, undo/redo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { vm.onAction(TimelineAction.SetTool(Tool.BRUSH)) },
                    label = { Text(stringResource(R.string.timeline_tool_brush)) },
                    leadingIcon = if (tool == Tool.BRUSH) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
                AssistChip(
                    onClick = { vm.onAction(TimelineAction.SetTool(Tool.ERASER)) },
                    label = { Text(stringResource(R.string.timeline_tool_eraser)) },
                    leadingIcon = if (tool == Tool.ERASER) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
                AssistChip(
                    onClick = { vm.onAction(TimelineAction.SetTool(Tool.FILL)) },
                    label = { Text(stringResource(R.string.timeline_tool_fill)) },
                    leadingIcon = if (tool == Tool.FILL) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
                AssistChip(
                    onClick = { vm.onAction(TimelineAction.ToggleDrag(!dragEnabled)) },
                    label = { Text(if (dragEnabled) stringResource(R.string.timeline_drag_on) else stringResource(R.string.timeline_drag_off)) },
                    leadingIcon = if (dragEnabled) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                )
                IconButton(onClick = { vm.onAction(TimelineAction.Undo) }, enabled = s.canUndo) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(R.string.timeline_undo))
                }
                IconButton(onClick = { vm.onAction(TimelineAction.Redo) }, enabled = s.canRedo) {
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
                        vm.onAction(TimelineAction.SetTool(if (checked) Tool.EYEDROPPER else Tool.BRUSH))
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
                            vm.onAction(TimelineAction.SetColor(col))
                            val led = s.selectedLed
                            val t = s.selectedTick
                            if (led in 0 until ledsCount && t in 0 until s.gridColors[led].size) {
                                vm.onAction(TimelineAction.Toggle(led, t))
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
            grid = s.gridColors,
            colors = s.ledColors,
            selected = s.selectedLed to s.selectedTick,
            dragEnabled = s.dragEnabled,
            cellSize = cellSizeDp,
            gap = gapDp,
            autoEnabled = autoEnabled,
            autoThresholdPx = autoThresholdPx,
            autoMinSpeedPx = autoMinSpeedPx,
            autoMaxSpeedPx = autoMaxSpeedPx,
            autoAccel = autoAccel,
            onToggle = { led, t -> vm.onAction(TimelineAction.Toggle(led, t)) },
            onSelect = { led, t -> vm.onAction(TimelineAction.Select(led, t)) },
            onDragStart = { led, t -> vm.onAction(TimelineAction.DragStart(led, t)) },
            onDragOver = { led, t -> vm.onAction(TimelineAction.DragOver(led, t)) },
            onDragEnd = { vm.onAction(TimelineAction.DragEnd) },
            markersMs = markersMs,
            tickMs = tickMs,
            onMarkersChange = onMarkersChange
        )
        // Clip parameters editor (always visible; controls disabled if no colored cell selected)
        val selectedRow = s.gridColors.getOrNull(s.selectedLed)
        val colorAtSel = selectedRow?.getOrNull(s.selectedTick)
        var startTickIdx = -1
        var clipDurationMs = 0
        if (colorAtSel != null && selectedRow != null) {
            var l = s.selectedTick
            var r = s.selectedTick
            while (l - 1 >= 0 && selectedRow[l - 1] == colorAtSel) l--
            while (r + 1 < selectedRow.size && selectedRow[r + 1] == colorAtSel) r++
            startTickIdx = l
            clipDurationMs = (r - l + 1) * s.tickMs
        }
        val curFadeIn = if (startTickIdx >= 0) (s.fadeInGrid[s.selectedLed][startTickIdx] ?: 0).coerceIn(0, clipDurationMs) else 0
        val curFadeOut = if (startTickIdx >= 0) (s.fadeOutGrid[s.selectedLed][startTickIdx] ?: 0).coerceIn(0, clipDurationMs) else 0
        val curEasing = if (startTickIdx >= 0) (s.easingGrid[s.selectedLed][startTickIdx] ?: Easing.LINEAR) else Easing.LINEAR

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.timeline_clip_params), style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.timeline_fade_in))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (startTickIdx >= 0) {
                            val v = (curFadeIn - s.tickMs).coerceAtLeast(0)
                            vm.onAction(TimelineAction.SetFadeIn(s.selectedLed, startTickIdx, v))
                        }
                    }, enabled = startTickIdx >= 0) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                    Text(text = String.format("%d ms", curFadeIn))
                    IconButton(onClick = {
                        if (startTickIdx >= 0) {
                            val v = (curFadeIn + s.tickMs).coerceAtMost(clipDurationMs)
                            vm.onAction(TimelineAction.SetFadeIn(s.selectedLed, startTickIdx, v))
                        }
                    }, enabled = startTickIdx >= 0) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.timeline_fade_out))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (startTickIdx >= 0) {
                            val v = (curFadeOut - s.tickMs).coerceAtLeast(0)
                            vm.onAction(TimelineAction.SetFadeOut(s.selectedLed, startTickIdx, v))
                        }
                    }, enabled = startTickIdx >= 0) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                    Text(text = String.format("%d ms", curFadeOut))
                    IconButton(onClick = {
                        if (startTickIdx >= 0) {
                            val v = (curFadeOut + s.tickMs).coerceAtMost(clipDurationMs)
                            vm.onAction(TimelineAction.SetFadeOut(s.selectedLed, startTickIdx, v))
                        }
                    }, enabled = startTickIdx >= 0) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.timeline_easing))
                var expanded by remember { mutableStateOf(false) }
                Box {
                    AssistChip(onClick = { expanded = true }, label = { Text(curEasing.name) }, enabled = startTickIdx >= 0)
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        Easing.values().forEach { opt ->
                            DropdownMenuItem(text = { Text(opt.name) }, onClick = {
                                if (startTickIdx >= 0) vm.onAction(TimelineAction.SetEasing(s.selectedLed, startTickIdx, opt))
                                expanded = false
                            })
                        }
                    }
                }
            }
        }

        // Переключатель расширенных настроек
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            AssistChip(
                onClick = { vm.onAction(TimelineAction.SetAdvanced(!showAdvanced)) },
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
                            IconButton(onClick = { vm.onAction(TimelineAction.SetCellSize((s.cellSizeDp - 2f).coerceIn(16f, 40f))) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                            Text(text = String.format("%d dp", s.cellSizeDp.toInt()))
                            IconButton(onClick = { vm.onAction(TimelineAction.SetCellSize((s.cellSizeDp + 2f).coerceIn(16f, 40f))) }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.timeline_gap))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { vm.onAction(TimelineAction.SetGap((s.gapDp - 1f).coerceIn(0f, 8f))) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                            Text(text = String.format("%d dp", s.gapDp.toInt()))
                            IconButton(onClick = { vm.onAction(TimelineAction.SetGap((s.gapDp + 1f).coerceIn(0f, 8f))) }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
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
                        Switch(checked = autoEnabled, onCheckedChange = { vm.onAction(TimelineAction.SetAutoEnabled(it)) })
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
                                    IconButton(onClick = { vm.onAction(TimelineAction.SetAutoThreshold((s.autoThresholdPx - 4f).coerceIn(16f, 96f))) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                                    Text(text = String.format("%d px", s.autoThresholdPx.toInt()))
                                    IconButton(onClick = { vm.onAction(TimelineAction.SetAutoThreshold((s.autoThresholdPx + 4f).coerceIn(16f, 96f))) }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = stringResource(R.string.timeline_autoscroll_min_speed))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { vm.onAction(TimelineAction.SetAutoMinSpeed((s.autoMinSpeedPx - 2f).coerceIn(0f, s.autoMaxSpeedPx))) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                                    Text(text = String.format("%d px/шаг", s.autoMinSpeedPx.toInt()))
                                    IconButton(onClick = { vm.onAction(TimelineAction.SetAutoMinSpeed((s.autoMinSpeedPx + 2f).coerceIn(0f, s.autoMaxSpeedPx))) }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = stringResource(R.string.timeline_autoscroll_max_speed))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { vm.onAction(TimelineAction.SetAutoMaxSpeed((s.autoMaxSpeedPx - 2f).coerceIn(s.autoMinSpeedPx, 96f))) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                                    Text(text = String.format("%d px/шаг", s.autoMaxSpeedPx.toInt()))
                                    IconButton(onClick = { vm.onAction(TimelineAction.SetAutoMaxSpeed((s.autoMaxSpeedPx + 2f).coerceIn(s.autoMinSpeedPx, 96f))) }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = stringResource(R.string.timeline_autoscroll_acceleration))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { vm.onAction(TimelineAction.SetAutoAccel((s.autoAccel - 0.05f).coerceIn(0.1f, 1.0f))) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                                    Text(text = String.format("%.2f", s.autoAccel))
                                    IconButton(onClick = { vm.onAction(TimelineAction.SetAutoAccel((s.autoAccel + 0.05f).coerceIn(0.1f, 1.0f))) }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
                                }
                            }
                        }
                    }
                }
            }
        }

        AdvancedColorPickerSheet(
            open = showAdvancedPicker,
            initialColor = s.gridColors.getOrNull(s.selectedLed)?.getOrNull(s.selectedTick) ?: currentColor,
            onDismiss = { showAdvancedPicker = false },
            onPick = { c: String ->
                vm.onAction(TimelineAction.SetColor(c))
                recentColors.remove(c)
                recentColors.add(0, c)
                if (recentColors.size > 12) recentColors.removeLast()
                val led = s.selectedLed; val t = s.selectedTick
                if (led in 0 until ledsCount && t in 0 until s.gridColors[led].size) vm.onAction(TimelineAction.Toggle(led, t))
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
                        String.format("%d %s", s.durationMs, stringResource(R.string.duration_unit_ms))
                    } else {
                        String.format("%.2f %s", s.durationMs / 1000f, stringResource(R.string.duration_unit_s))
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
                                vm.onAction(TimelineAction.SetDuration(ms))
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
                value = s.durationMs.toFloat(),
                onValueChange = { vm.onAction(TimelineAction.SetDuration(it.toInt())) },
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
                Text(text = stringResource(R.string.time_format_ms, s.tickMs), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value = s.tickMs.toFloat(),
                onValueChange = { vm.onAction(TimelineAction.SetTick(it.toInt())) },
                valueRange = 10f..1000f,
                steps = 100
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
    onDragEnd: () -> Unit,
    markersMs: List<Int>,
    tickMs: Int,
    onMarkersChange: (List<Int>) -> Unit,
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
    ) {
        Box(
            modifier = dragModifier
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
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

        // Marker toggles under each tick column
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(gap),
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
            ) {
                repeat(ticks) { t ->
                    val ms = t * tickMs
                    val hasMarker = markersMs.contains(ms)
                    val bg = if (hasMarker) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                    val borderColor = if (hasMarker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(cellSize)
                            .clip(CircleShape)
                            .background(bg, shape = CircleShape)
                            .border(width = 1.dp, color = borderColor, shape = CircleShape)
                            .clickable {
                                val updated = if (hasMarker) {
                                    markersMs.filterNot { it == ms }
                                } else {
                                    (markersMs + ms).distinct()
                                }.sorted()
                                onMarkersChange(updated)
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = stringResource(R.string.pattern_timeline_add_marker),
                            tint = if (hasMarker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
