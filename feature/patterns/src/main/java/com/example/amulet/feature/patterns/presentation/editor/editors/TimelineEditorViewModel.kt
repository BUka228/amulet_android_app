package com.example.amulet.feature.patterns.presentation.editor.editors

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.amulet.shared.domain.patterns.model.*

@Immutable
data class TimelineEditorState(
    val durationMs: Int,
    val tickMs: Int,
    val ledsCount: Int = 8,
    val gridColors: Array<Array<String?>>, // [led][tick] -> color hex or null
    val fadeInGrid: Array<Array<Int?>>,    // per clip start tick
    val fadeOutGrid: Array<Array<Int?>>,   // per clip start tick
    val easingGrid: Array<Array<Easing?>>, // per clip start tick
    val ledColors: List<String>,
    val selectedLed: Int,
    val selectedTick: Int,
    val tool: Tool = Tool.BRUSH,
    val currentColor: String,
    val dragEnabled: Boolean = true,
    val cellSizeDp: Float = 24f,
    val gapDp: Float = 4f,
    val autoEnabled: Boolean = true,
    val autoThresholdPx: Float = 48f,
    val autoMinSpeedPx: Float = 8f,
    val autoMaxSpeedPx: Float = 48f,
    val autoAccel: Float = 0.5f,
    val showAdvanced: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
) {
    val ticksCount: Int = (durationMs / tickMs).coerceAtLeast(1)
}

enum class Tool { BRUSH, ERASER, FILL, EYEDROPPER }

sealed interface TimelineAction {
    data class Select(val led: Int, val tick: Int) : TimelineAction
    data class Toggle(val led: Int, val tick: Int) : TimelineAction
    data class DragStart(val led: Int, val tick: Int) : TimelineAction
    data class DragOver(val led: Int, val tick: Int) : TimelineAction
    data object DragEnd : TimelineAction
    data class SetTool(val tool: Tool) : TimelineAction
    data class SetColor(val color: String) : TimelineAction
    data object Undo : TimelineAction
    data object Redo : TimelineAction
    data class ToggleDrag(val enabled: Boolean) : TimelineAction
    data class SetAdvanced(val show: Boolean) : TimelineAction
    data class SetCellSize(val dp: Float) : TimelineAction
    data class SetGap(val dp: Float) : TimelineAction
    data class SetAutoEnabled(val enabled: Boolean) : TimelineAction
    data class SetAutoThreshold(val px: Float) : TimelineAction
    data class SetAutoMinSpeed(val px: Float) : TimelineAction
    data class SetAutoMaxSpeed(val px: Float) : TimelineAction
    data class SetAutoAccel(val value: Float) : TimelineAction
    data class SetFadeIn(val led: Int, val startTick: Int, val value: Int) : TimelineAction
    data class SetFadeOut(val led: Int, val startTick: Int, val value: Int) : TimelineAction
    data class SetEasing(val led: Int, val startTick: Int, val value: Easing) : TimelineAction
    data class SetDuration(val ms: Int) : TimelineAction
    data class SetTick(val ms: Int) : TimelineAction
}

@HiltViewModel
class TimelineEditorViewModel @Inject constructor() : ViewModel() {
    private val _state: MutableStateFlow<TimelineEditorState?> = MutableStateFlow(null)
    val state: StateFlow<TimelineEditorState?> get() = _state.asStateFlow()

    private var onUpdateCallback: ((PatternElement) -> Unit)? = null
    private var initialized by mutableStateOf(false)

    private val undoStack = ArrayDeque<Array<Array<String?>>>()
    private val redoStack = ArrayDeque<Array<Array<String?>>>()

    private var isPainting by mutableStateOf(false)
    private var strokePushed by mutableStateOf(false)

    fun initialize(element: PatternElementTimeline, onUpdate: (PatternElement) -> Unit) {
        if (initialized) return
        onUpdateCallback = onUpdate
        val ledsCount = 8
        val duration = element.durationMs
        val tick = element.tickMs
        val ticksCount = (duration / tick).coerceAtLeast(1)

        val grid = Array(ledsCount) { arrayOfNulls<String>(ticksCount) }
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
        val fadeIn = Array(ledsCount) { arrayOfNulls<Int>(ticksCount) }
        val fadeOut = Array(ledsCount) { arrayOfNulls<Int>(ticksCount) }
        val easing = Array(ledsCount) { arrayOfNulls<Easing>(ticksCount) }
        element.tracks.forEach { track ->
            val indices: List<Int> = when (val t = track.target) {
                is TargetLed -> listOf(t.index)
                is TargetGroup -> t.indices
                is TargetRing -> (0 until ledsCount).toList()
            }.filter { it in 0 until ledsCount }
            track.clips.forEach { clip ->
                val startTick = (clip.startMs / tick).coerceAtLeast(0)
                indices.forEach { led ->
                    if (startTick in 0 until ticksCount) {
                        fadeIn[led][startTick] = clip.fadeInMs
                        fadeOut[led][startTick] = clip.fadeOutMs
                        easing[led][startTick] = clip.easing
                    }
                }
            }
        }
        val initialColors = MutableList(ledsCount) { idx ->
            val color = element.tracks
                .firstOrNull { it.target is TargetLed && (it.target as TargetLed).index == idx }
                ?.clips?.firstOrNull()?.color
            color ?: "#FFFFFF"
        }
        val selectedLed = 0
        val selectedTick = 0
        val currentColor = grid.getOrNull(selectedLed)?.getOrNull(selectedTick) ?: initialColors[selectedLed]
        _state.value = TimelineEditorState(
                durationMs = duration,
                tickMs = tick,
                gridColors = grid,
                fadeInGrid = fadeIn,
                fadeOutGrid = fadeOut,
                easingGrid = easing,
                ledColors = initialColors,
                selectedLed = selectedLed,
                selectedTick = selectedTick,
                currentColor = currentColor,
            )
        initialized = true
    }

    fun onAction(action: TimelineAction) {
        when (action) {
            is TimelineAction.Select -> setSelection(action.led, action.tick)
            is TimelineAction.Toggle -> handleToggle(action.led, action.tick)
            is TimelineAction.DragStart -> beginStroke(action.led, action.tick)
            is TimelineAction.DragOver -> dragOver(action.led, action.tick)
            is TimelineAction.DragEnd -> endStroke()
            is TimelineAction.SetTool -> _state.update { it.copy(tool = action.tool) }
            is TimelineAction.SetColor -> _state.update { it.copy(currentColor = action.color) }
            is TimelineAction.Undo -> undo()
            is TimelineAction.Redo -> redo()
            is TimelineAction.ToggleDrag -> _state.update { it.copy(dragEnabled = action.enabled) }
            is TimelineAction.SetAdvanced -> _state.update { it.copy(showAdvanced = action.show) }
            is TimelineAction.SetCellSize -> _state.update { it.copy(cellSizeDp = action.dp) }
            is TimelineAction.SetGap -> _state.update { it.copy(gapDp = action.dp) }
            is TimelineAction.SetAutoEnabled -> _state.update { it.copy(autoEnabled = action.enabled) }
            is TimelineAction.SetAutoThreshold -> _state.update { it.copy(autoThresholdPx = action.px) }
            is TimelineAction.SetAutoMinSpeed -> _state.update { it.copy(autoMinSpeedPx = action.px) }
            is TimelineAction.SetAutoMaxSpeed -> _state.update { it.copy(autoMaxSpeedPx = action.px) }
            is TimelineAction.SetAutoAccel -> _state.update { it.copy(autoAccel = action.value) }
            is TimelineAction.SetFadeIn -> setFadeIn(action.led, action.startTick, action.value)
            is TimelineAction.SetFadeOut -> setFadeOut(action.led, action.startTick, action.value)
            is TimelineAction.SetEasing -> setEasing(action.led, action.startTick, action.value)
            is TimelineAction.SetDuration -> setDuration(action.ms)
            is TimelineAction.SetTick -> setTick(action.ms)
        }
    }

    private fun setSelection(led: Int, tick: Int) {
        _state.update { it!!.copy(selectedLed = led, selectedTick = tick) }
    }

    private fun handleToggle(led: Int, tick: Int) {
        val s = state.value ?: return
        if (s.tool == Tool.EYEDROPPER) {
            val picked = s.gridColors.getOrNull(led)?.getOrNull(tick)
            if (picked != null) _state.update { it!!.copy(currentColor = picked, selectedLed = led, selectedTick = tick) }
            return
        }
        beginStrokeInternal()
        when (s.tool) {
            Tool.BRUSH -> paintCell(led, tick, s.currentColor)
            Tool.ERASER -> paintCell(led, tick, null)
            Tool.FILL -> applyFill(led, tick)
            Tool.EYEDROPPER -> Unit
        }
        endStrokeInternal()
        setSelection(led, tick)
    }

    private fun beginStroke(led: Int, tick: Int) {
        val s = state.value ?: return
        if (s.tool == Tool.EYEDROPPER) {
            val picked = s.gridColors.getOrNull(led)?.getOrNull(tick)
            if (picked != null) _state.update { it!!.copy(currentColor = picked) }
        } else {
            beginStrokeInternal()
            when (s.tool) {
                Tool.BRUSH -> paintCell(led, tick, s.currentColor)
                Tool.ERASER -> paintCell(led, tick, null)
                Tool.FILL -> applyFill(led, tick)
                Tool.EYEDROPPER -> Unit
            }
        }
        setSelection(led, tick)
    }

    private fun dragOver(led: Int, tick: Int) {
        val s = state.value ?: return
        if (s.tool == Tool.EYEDROPPER) {
            val picked = s.gridColors.getOrNull(led)?.getOrNull(tick)
            if (picked != null) _state.update { it!!.copy(currentColor = picked) }
        } else {
            when (s.tool) {
                Tool.BRUSH -> paintCell(led, tick, s.currentColor)
                Tool.ERASER -> paintCell(led, tick, null)
                Tool.FILL -> applyFill(led, tick)
                Tool.EYEDROPPER -> Unit
            }
        }
        setSelection(led, tick)
    }

    private fun endStroke() {
        val s = state.value ?: return
        if (s.tool != Tool.EYEDROPPER) endStrokeInternal()
    }

    private fun beginStrokeInternal() { isPainting = true; strokePushed = false }
    private fun endStrokeInternal() { isPainting = false; applyAndUpdate() }

    private fun copyGrid(src: Array<Array<String?>>): Array<Array<String?>> = Array(src.size) { i -> src[i].clone() }

    private fun pushUndo() {
        val s = state.value ?: return
        undoStack.addLast(copyGrid(s.gridColors))
        while (undoStack.size > 50) undoStack.removeFirst()
        redoStack.clear()
        _state.update { it!!.copy(canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty()) }
    }

    private fun paintCell(led: Int, tick: Int, color: String?) {
        val s = state.value ?: return
        if (led !in 0 until s.ledsCount || tick !in 0 until s.gridColors[led].size) return
        if (!strokePushed) { pushUndo(); strokePushed = true }
        val g = Array(s.ledsCount) { i -> s.gridColors[i].clone() }
        g[led][tick] = color
        _state.update { it!!.copy(gridColors = g) }
    }

    private fun applyFill(led: Int, tick: Int) {
        val s = state.value ?: return
        if (led !in 0 until s.ledsCount || tick !in 0 until s.gridColors[led].size) return
        if (!strokePushed) { pushUndo(); strokePushed = true }
        val row = s.gridColors[led]
        val target = row[tick]
        var l = tick
        var r = tick
        while (l - 1 >= 0 && row[l - 1] == target) l--
        while (r + 1 < row.size && row[r + 1] == target) r++
        val g = Array(s.ledsCount) { i -> s.gridColors[i].clone() }
        for (i in l..r) g[led][i] = s.currentColor
        _state.update { it!!.copy(gridColors = g) }
    }

    private fun undo() {
        if (undoStack.isNotEmpty()) {
            val s = state.value ?: return
            val current = s.gridColors
            val prev = undoStack.removeLast()
            redoStack.addLast(copyGrid(current))
            _state.update { it!!.copy(gridColors = prev, canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty()) }
            applyAndUpdate()
        }
    }

    private fun redo() {
        if (redoStack.isNotEmpty()) {
            val s = state.value ?: return
            val current = s.gridColors
            val next = redoStack.removeLast()
            undoStack.addLast(copyGrid(current))
            _state.update { it!!.copy(gridColors = next, canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty()) }
            applyAndUpdate()
        }
    }

    private fun rebuildTracks(): List<TimelineTrack> {
        val s = state.value ?: return emptyList()
        val tracks = mutableListOf<TimelineTrack>()
        for (led in 0 until s.ledsCount) {
            val row = s.gridColors[led]
            var c = 0
            val clips = mutableListOf<TimelineClip>()
            val maxTicks = row.size
            while (c < maxTicks) {
                val color = row[c]
                if (color != null) {
                    val start = c
                    var end = c
                    while (end + 1 < maxTicks && row[end + 1] == color) end++
                    val startMs = start * s.tickMs
                    val durationMs = (end - start + 1) * s.tickMs
                    val fi = s.fadeInGrid.getOrNull(led)?.getOrNull(start) ?: 0
                    val fo = s.fadeOutGrid.getOrNull(led)?.getOrNull(start) ?: 0
                    val ez = s.easingGrid.getOrNull(led)?.getOrNull(start) ?: Easing.LINEAR
                    clips.add(
                        TimelineClip(
                            startMs = startMs,
                            durationMs = durationMs,
                            color = color,
                            fadeInMs = fi,
                            fadeOutMs = fo,
                            easing = ez
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

    private fun applyAndUpdate() {
        val s = state.value ?: return
        onUpdateCallback?.invoke(
            PatternElementTimeline(
                durationMs = s.durationMs,
                tickMs = s.tickMs,
                tracks = rebuildTracks()
            )
        )
    }

    private fun setFadeIn(led: Int, startTick: Int, value: Int) {
        val s = state.value ?: return
        val copy = s.fadeInGrid.copyOf()
        copy[led] = copy[led].clone().also { it[startTick] = value }
        _state.update { it!!.copy(fadeInGrid = copy) }
        applyAndUpdate()
    }

    private fun setFadeOut(led: Int, startTick: Int, value: Int) {
        val s = state.value ?: return
        val copy = s.fadeOutGrid.copyOf()
        copy[led] = copy[led].clone().also { it[startTick] = value }
        _state.update { it!!.copy(fadeOutGrid = copy) }
        applyAndUpdate()
    }

    private fun setEasing(led: Int, startTick: Int, value: Easing) {
        val s = state.value ?: return
        val copy = s.easingGrid.copyOf()
        copy[led] = copy[led].clone().also { it[startTick] = value }
        _state.update { it!!.copy(easingGrid = copy) }
        applyAndUpdate()
    }

    private fun setDuration(ms: Int) {
        val s = state.value ?: return
        val nd = ms.coerceIn(200, 60000)
        if (nd == s.durationMs) return
        val newTicks = (nd / s.tickMs).coerceAtLeast(1)
        val newGrid = Array(s.ledsCount) { arrayOfNulls<String>(newTicks) }
        for (led in 0 until s.ledsCount) {
            for (t in 0 until minOf(s.gridColors[led].size, newTicks)) newGrid[led][t] = s.gridColors[led][t]
        }
        val newFadeIn = Array(s.ledsCount) { arrayOfNulls<Int>(newTicks) }
        val newFadeOut = Array(s.ledsCount) { arrayOfNulls<Int>(newTicks) }
        val newEasing = Array(s.ledsCount) { arrayOfNulls<Easing>(newTicks) }
        for (led in 0 until s.ledsCount) {
            for (t in 0 until minOf(s.fadeInGrid[led].size, newTicks)) newFadeIn[led][t] = s.fadeInGrid[led][t]
            for (t in 0 until minOf(s.fadeOutGrid[led].size, newTicks)) newFadeOut[led][t] = s.fadeOutGrid[led][t]
            for (t in 0 until minOf(s.easingGrid[led].size, newTicks)) newEasing[led][t] = s.easingGrid[led][t]
        }
        _state.update { it!!.copy(
            durationMs = nd,
            gridColors = newGrid,
            fadeInGrid = newFadeIn,
            fadeOutGrid = newFadeOut,
            easingGrid = newEasing,
            selectedTick = it.selectedTick.coerceIn(0, newTicks - 1)
        ) }
        applyAndUpdate()
    }

    private fun setTick(ms: Int) {
        val s = state.value ?: return
        val nd = ms.coerceIn(10, 1000)
        if (nd == s.tickMs) return
        val newTicks = (s.durationMs / nd).coerceAtLeast(1)
        val newGrid = Array(s.ledsCount) { arrayOfNulls<String>(newTicks) }
        val newFadeIn = Array(s.ledsCount) { arrayOfNulls<Int>(newTicks) }
        val newFadeOut = Array(s.ledsCount) { arrayOfNulls<Int>(newTicks) }
        val newEasing = Array(s.ledsCount) { arrayOfNulls<Easing>(newTicks) }
        for (led in 0 until s.ledsCount) {
            var runStart = -1
            var runColor: String? = null
            val lastIndex = s.gridColors[led].lastIndex
            for (t in 0..lastIndex) {
                val color = s.gridColors[led][t]
                if (color != null && runStart == -1) {
                    runStart = t
                    runColor = color
                }
                val isEnd = t == lastIndex
                val boundary = (color == null || color != runColor || isEnd)
                if (runStart != -1 && boundary) {
                    val endTickOld = if (isEnd && color != null && color == runColor) t else t - 1
                    val startMs = runStart * s.tickMs
                    val endMs = (endTickOld + 1) * s.tickMs
                    val sTick = (startMs / nd).coerceAtLeast(0)
                    val eTick = ((endMs - 1) / nd).coerceAtLeast(sTick)
                    val sTickClamped = sTick.coerceAtMost(newTicks - 1)
                    val eTickClamped = eTick.coerceAtMost(newTicks - 1)
                    for (tt in sTickClamped..eTickClamped) newGrid[led][tt] = runColor
                    // перенос параметров клипа с позиции его старта
                    val fi = s.fadeInGrid.getOrNull(led)?.getOrNull(runStart)
                    val fo = s.fadeOutGrid.getOrNull(led)?.getOrNull(runStart)
                    val ez = s.easingGrid.getOrNull(led)?.getOrNull(runStart)
                    if (fi != null) newFadeIn[led][sTickClamped] = fi
                    if (fo != null) newFadeOut[led][sTickClamped] = fo
                    if (ez != null) newEasing[led][sTickClamped] = ez
                    runStart = -1
                    runColor = null
                }
            }
        }
        _state.update { it!!.copy(
            tickMs = nd,
            gridColors = newGrid,
            fadeInGrid = newFadeIn,
            fadeOutGrid = newFadeOut,
            easingGrid = newEasing,
            selectedTick = it.selectedTick.coerceIn(0, newTicks - 1)
        ) }
        applyAndUpdate()
    }
}

// Local extension to update MutableStateFlow holding nullable state
private inline fun <T> MutableStateFlow<T?>.update(transform: (T) -> T) {
    val v = value ?: return
    value = transform(v)
}
