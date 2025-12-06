package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun RingSelector(
    selected: Set<Int>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    ledCount: Int = 8,
    size: Dp = 200.dp,
    ledRadius: Dp = 14.dp,
    activeColor: Color = Color(0xFF4CAF50),
    inactiveColor: Color = Color.Gray.copy(alpha = 0.2f)
) {
    Canvas(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                detectTap { offset, canvasSize ->
                    val idx = hitTestIndex(offset, canvasSize.width.toFloat(), canvasSize.height.toFloat(), ledCount)
                    if (idx != null) onToggle(idx)
                }
            }
    ) {
        drawRing(ledCount, ledRadius.toPx(), selected, activeColor, inactiveColor)
    }
}

private fun DrawScope.drawRing(
    ledCount: Int,
    ledRadiusPx: Float,
    selected: Set<Int>,
    activeColor: Color,
    inactiveColor: Color
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val ringRadius = (size.minDimension / 2f) * 0.7f

    for (i in 0 until ledCount) {
        val angle = (i * 2 * PI / ledCount) - (PI / 2)
        val x = center.x + (ringRadius * cos(angle)).toFloat()
        val y = center.y + (ringRadius * sin(angle)).toFloat()
        val color = if (selected.contains(i)) activeColor else inactiveColor
        drawCircle(color = color, radius = ledRadiusPx, center = Offset(x, y))
    }
}

private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectTap(
    onTap: (Offset, androidx.compose.ui.unit.IntSize) -> Unit
) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull() ?: continue
            if (change.pressed && change.previousPressed == false) {
                onTap(change.position, size)
                change.consume()
            }
        }
    }
}

private fun hitTestIndex(
    point: Offset,
    width: Float,
    height: Float,
    ledCount: Int
): Int? {
    val center = Offset(width / 2f, height / 2f)
    val dx = point.x - center.x
    val dy = point.y - center.y
    val distance = sqrt(dx * dx + dy * dy)
    val ringRadius = (minOf(width, height) / 2f) * 0.7f
    val inner = ringRadius * 0.8f
    val outer = ringRadius * 1.2f
    if (distance !in inner..outer) return null

    // angle from top (index 0), clockwise
    var angle = atan2(dy, dx).toDouble() + PI / 2
    if (angle < 0) angle += 2 * PI
    val sector = (angle / (2 * PI / ledCount)).toInt()
    return sector.coerceIn(0, ledCount - 1)
}
