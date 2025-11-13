package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.amulet.shared.domain.patterns.model.*
import kotlin.math.roundToInt

private enum class DragMode { NONE, MOVE, RESIZE_LEFT, RESIZE_RIGHT }

@Composable
fun TimelineCanvas(
    element: PatternElementTimeline,
    onChange: (PatternElementTimeline) -> Unit,
    modifier: Modifier = Modifier,
    pixelsPerMs: Float = 0.2f,
    snapToGrid: Boolean = true,
    tickMs: Int = element.tickMs,
    selected: Pair<Int, Int>? = null,
    onSelect: (Pair<Int, Int>?) -> Unit = {},
    onContextRequest: (Pair<Int, Int>) -> Unit = {},
    trackHeight: Dp = 36.dp,
    laneGap: Dp = 8.dp
) {
    var internalSelected by remember { mutableStateOf<Pair<Int, Int>?>(selected) }
    LaunchedEffect(selected) { internalSelected = selected }
    var dragMode by remember { mutableStateOf(DragMode.NONE) }
    val lanes = element.tracks.size
    val density = androidx.compose.ui.platform.LocalDensity.current
    val trackHeightPx = with(density) { trackHeight.toPx() }
    val laneGapPx = with(density) { laneGap.toPx() }
    val heightPx = ((lanes * (trackHeightPx + laneGapPx)).toInt()).coerceAtLeast(1)

    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier
        .fillMaxWidth()
        .height(with(density) { heightPx.toDp() })
        .pointerInput(element, pixelsPerMs, trackHeightPx, laneGapPx) {
            detectTapGestures(
                onLongPress = { pos ->
                    val hit = hitTestClip(element, pos, pixelsPerMs, trackHeightPx, laneGapPx)
                    if (hit != null) {
                        internalSelected = hit
                        onSelect(hit)
                        onContextRequest(hit)
                    }
                }
            )
        }
        .pointerInput(element, pixelsPerMs, trackHeight, laneGap, snapToGrid, tickMs) {
            detectDragGestures(
                onDragStart = { pos ->
                    val hit = hitTestClip(element, pos, pixelsPerMs, trackHeightPx, laneGapPx)
                    internalSelected = hit
                    onSelect(hit)
                    dragMode = DragMode.NONE
                    hit?.let { (tIdx, cIdx) ->
                        val clip = element.tracks[tIdx].clips[cIdx]
                        val x = clip.startMs * pixelsPerMs
                        val w = clip.durationMs * pixelsPerMs
                        val localY = tIdx * (trackHeightPx + laneGapPx)
                        val leftHandle = x
                        val rightHandle = x + w
                        val near = 12f
                        dragMode = when {
                            pos.y in localY..(localY + trackHeightPx) && kotlin.math.abs(pos.x - leftHandle) <= near -> DragMode.RESIZE_LEFT
                            pos.y in localY..(localY + trackHeightPx) && kotlin.math.abs(pos.x - rightHandle) <= near -> DragMode.RESIZE_RIGHT
                            else -> DragMode.MOVE
                        }
                    }
                },
                onDrag = { change, drag ->
                    val sel = internalSelected ?: return@detectDragGestures
                    val trackIndex = sel.first
                    val clipIndex = sel.second
                    val dxMs = (drag.x / pixelsPerMs).roundToInt()
                    val updatedTracks = element.tracks.toMutableList()
                    val clip = updatedTracks[trackIndex].clips[clipIndex]
                    val sorted = updatedTracks[trackIndex].clips.sortedBy { it.startMs }
                    val prev = sorted.filter { it !== clip }.filter { it.startMs <= clip.startMs }.maxByOrNull { it.startMs }
                    val next = sorted.filter { it !== clip }.filter { it.startMs >= clip.startMs }.minByOrNull { it.startMs }
                    var newStart = clip.startMs
                    var newDuration = clip.durationMs
                    when (dragMode) {
                        DragMode.MOVE -> {
                            newStart = (clip.startMs + dxMs)
                            if (snapToGrid && tickMs > 0) newStart = ((newStart.toFloat() / tickMs).roundToInt() * tickMs)
                            val minStart = (prev?.let { it.startMs + it.durationMs } ?: 0)
                            val maxStart = (next?.startMs ?: element.durationMs) - clip.durationMs
                            newStart = newStart.coerceIn(minStart, maxStart.coerceAtLeast(minStart))
                        }
                        DragMode.RESIZE_LEFT -> {
                            newStart = (clip.startMs + dxMs)
                            if (snapToGrid && tickMs > 0) newStart = ((newStart.toFloat() / tickMs).roundToInt() * tickMs)
                            val maxLeft = (clip.startMs + clip.durationMs - (tickMs.coerceAtLeast(10)))
                            val minStart = (prev?.let { it.startMs + it.durationMs } ?: 0)
                            newStart = newStart.coerceIn(minStart, maxLeft)
                            newDuration = (clip.startMs + clip.durationMs - newStart).coerceAtLeast(tickMs)
                        }
                        DragMode.RESIZE_RIGHT -> {
                            var newEnd = clip.startMs + clip.durationMs + dxMs
                            if (snapToGrid && tickMs > 0) newEnd = ((newEnd.toFloat() / tickMs).roundToInt() * tickMs)
                            val maxEnd = (next?.startMs ?: element.durationMs)
                            val minEnd = clip.startMs + (tickMs.coerceAtLeast(10))
                            newEnd = newEnd.coerceIn(minEnd, maxEnd)
                            newDuration = (newEnd - clip.startMs).coerceAtLeast(tickMs)
                        }
                        DragMode.NONE -> {}
                    }
                    newStart = newStart.coerceIn(0, element.durationMs - 1)
                    val bounded = clip.copy(startMs = newStart, durationMs = newDuration)
                    updatedTracks[trackIndex] = updatedTracks[trackIndex].copy(
                        clips = updatedTracks[trackIndex].clips.toMutableList().also { it[clipIndex] = bounded }
                    )
                    onChange(element.copy(tracks = updatedTracks))
                    change.consume()
                },
                onDragEnd = { dragMode = DragMode.NONE }
            )
        }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawGrid(element, pixelsPerMs, gridColor)
            element.tracks.forEachIndexed { tIdx, track ->
                drawTrack(tIdx, trackHeightPx, laneGapPx, trackColor)
                track.clips.forEachIndexed { cIdx, clip ->
                    val isSel = internalSelected?.first == tIdx && internalSelected?.second == cIdx
                    drawClip(tIdx, clip, pixelsPerMs, trackHeightPx, laneGapPx, isSel)
                    if (isSel) drawHandles(tIdx, clip, pixelsPerMs, trackHeightPx, laneGapPx)
                }
            }
        }
    }
}

private fun DrawScope.drawGrid(
    element: PatternElementTimeline,
    pixelsPerMs: Float,
    gridColor: Color
) {
    val h = size.height
    val step = element.tickMs
    val totalPx = element.durationMs * pixelsPerMs
    var x = 0f
    while (x <= totalPx) {
        drawLine(gridColor, start = Offset(x, 0f), end = Offset(x, h), strokeWidth = 1f)
        x += step * pixelsPerMs
    }
}

private fun DrawScope.drawTrack(
    index: Int,
    trackHeightPx: Float,
    laneGapPx: Float,
    trackColor: Color
) {
    val y = index * (trackHeightPx + laneGapPx)
    drawRect(trackColor, topLeft = Offset(0f, y), size = androidx.compose.ui.geometry.Size(size.width, trackHeightPx))
}

private fun DrawScope.drawClip(
    trackIndex: Int,
    clip: TimelineClip,
    pixelsPerMs: Float,
    trackHeightPx: Float,
    laneGapPx: Float,
    selected: Boolean
) {
    val y = trackIndex * (trackHeightPx + laneGapPx)
    val x = clip.startMs * pixelsPerMs
    val w = clip.durationMs * pixelsPerMs
    val color = parseHexColor(clip.color)
    val rectColor = if (selected) color.copy(alpha = 0.9f) else color.copy(alpha = 0.7f)
    drawRect(rectColor, topLeft = Offset(x, y), size = androidx.compose.ui.geometry.Size(w, trackHeightPx))
}

private fun DrawScope.drawHandles(
    trackIndex: Int,
    clip: TimelineClip,
    pixelsPerMs: Float,
    trackHeightPx: Float,
    laneGapPx: Float
) {
    val y = trackIndex * (trackHeightPx + laneGapPx)
    val x = clip.startMs * pixelsPerMs
    val w = clip.durationMs * pixelsPerMs
    val handleW = 6f
    val color = Color.White.copy(alpha = 0.9f)
    drawRect(color, topLeft = Offset(x - handleW / 2, y), size = androidx.compose.ui.geometry.Size(handleW, trackHeightPx))
    drawRect(color, topLeft = Offset(x + w - handleW / 2, y), size = androidx.compose.ui.geometry.Size(handleW, trackHeightPx))
}

private fun hitTestClip(
    element: PatternElementTimeline,
    pos: Offset,
    pixelsPerMs: Float,
    trackHeightPx: Float,
    laneGapPx: Float
): Pair<Int, Int>? {
    element.tracks.forEachIndexed { tIdx, track ->
        val top = tIdx * (trackHeightPx + laneGapPx)
        val bottom = top + trackHeightPx
        if (pos.y in top..bottom) {
            track.clips.forEachIndexed { cIdx, clip ->
                val x = clip.startMs * pixelsPerMs
                val w = clip.durationMs * pixelsPerMs
                if (pos.x in x..(x + w)) return tIdx to cIdx
            }
        }
    }
    return null
}

private fun parseHexColor(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val r = clean.substring(0, 2).toInt(16)
        val g = clean.substring(2, 4).toInt(16)
        val b = clean.substring(4, 6).toInt(16)
        Color(r, g, b)
    } catch (e: Exception) {
        Color.Gray
    }
}
