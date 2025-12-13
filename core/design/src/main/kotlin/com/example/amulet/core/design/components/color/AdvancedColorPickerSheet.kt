package com.example.amulet.core.design.components.color

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.amulet.core.design.R
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedColorPickerSheet(
    open: Boolean,
    initialColor: String,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    if (!open) return

    var hsv by remember(initialColor) { mutableStateOf(hexToHSV(initialColor)) }
    val preview = remember(hsv) { Color.hsv(hsv[0], hsv[1], hsv[2]) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = stringResource(R.string.design_advanced_color_picker_title), style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(preview)
                        .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                )
                Text(text = preview.toHexString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }

            SVBox(
                hue = hsv[0],
                s = hsv[1],
                v = hsv[2],
                onChange = { s, v -> hsv = floatArrayOf(hsv[0], s, v) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.design_color_picker_hue), style = MaterialTheme.typography.labelSmall)
                Slider(
                    value = hsv[0],
                    onValueChange = { hsv = floatArrayOf(it, hsv[1], hsv[2]) },
                    valueRange = 0f..360f,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.design_color_picker_saturation), style = MaterialTheme.typography.labelSmall)
                Slider(
                    value = hsv[1],
                    onValueChange = { hsv = floatArrayOf(hsv[0], it, hsv[2]) },
                    valueRange = 0f..1f,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.design_color_picker_value), style = MaterialTheme.typography.labelSmall)
                Slider(
                    value = hsv[2],
                    onValueChange = { hsv = floatArrayOf(hsv[0], hsv[1], it) },
                    valueRange = 0f..1f,
                )
            }

            var hex by remember(preview) { mutableStateOf(preview.toHexString()) }
            HexColorInput(color = hex, onColorChange = { c ->
                hex = c
                hsv = hexToHSV(c)
            })

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { onPick(preview.toHexString()); onDismiss() }) { Text(stringResource(android.R.string.ok)) }
            }
        }
    }
}

@Composable
private fun SVBox(
    hue: Float,
    s: Float,
    v: Float,
    onChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var boxSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val hueColor = remember(hue) { Color.hsv(hue, 1f, 1f) }

    Box(
        modifier = modifier
            .pointerInput(hue) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        val pos = change.position
                        if (boxSize.width > 0 && boxSize.height > 0) {
                            val ns = (pos.x / boxSize.width).coerceIn(0f, 1f)
                            val nv = (1f - pos.y / boxSize.height).coerceIn(0f, 1f)
                            onChange(ns, nv)
                            change.consume()
                        }
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .onSizeChanged { boxSize = androidx.compose.ui.geometry.Size(it.width.toFloat(), it.height.toFloat()) }
        ) {
            drawRect(brush = Brush.horizontalGradient(listOf(Color.White, hueColor)))
            drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        }

        val thumbSizePx = 16f
        val cx = boxSize.width * s
        val cy = boxSize.height * (1f - v)
        val ox = (cx - thumbSizePx / 2f).coerceIn(0f, (boxSize.width - thumbSizePx).coerceAtLeast(0f))
        val oy = (cy - thumbSizePx / 2f).coerceIn(0f, (boxSize.height - thumbSizePx).coerceAtLeast(0f))
        Box(
            modifier = Modifier
                .offset { IntOffset(ox.roundToInt(), oy.roundToInt()) }
                .size(16.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color.Black, CircleShape)
        )
    }
}

private fun Color.toHexString(): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return String.format("#%02X%02X%02X", r, g, b)
}

private fun hexToHSV(hex: String): FloatArray {
    return try {
        val color = android.graphics.Color.parseColor(hex)
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color, hsv)
        hsv
    } catch (_: Throwable) {
        floatArrayOf(0f, 0f, 1f)
    }
}
