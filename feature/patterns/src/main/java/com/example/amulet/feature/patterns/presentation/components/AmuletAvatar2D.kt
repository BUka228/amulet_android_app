package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.amulet.shared.domain.patterns.model.MixMode
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.model.PatternTimeline
import com.example.amulet.shared.domain.patterns.model.TargetGroup
import com.example.amulet.shared.domain.patterns.model.TargetLed
import com.example.amulet.shared.domain.patterns.model.TargetRing
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.isActive

/**
 * 2D аватар амулета с 8 LED диодами, расположенными по кругу.
 * Поддерживает анимацию паттернов в реальном времени.
 */
@Composable
fun AmuletAvatar2D(
    spec: PatternSpec?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    ledRadius: Dp = 16.dp
) {
    val ledCount = 8
    val timeline = spec?.timeline
    val duration = timeline?.durationMs?.coerceAtLeast(1) ?: 1
    val loop = spec?.loop ?: false

    val progress = remember { Animatable(0f) }
    val ambient = rememberInfiniteTransition(label = "avatar-ambient")
    val haloAlpha = ambient.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo-alpha"
    )
    val orbitAngle = ambient.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit-angle"
    )
    val pulse = ambient.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Анимация таймлайна через Animatable + tween, чтобы избежать рывков от ручного таймера
    LaunchedEffect(timeline, isPlaying, loop) {
        if (timeline == null || !isPlaying || timeline.tracks.isEmpty()) {
            progress.snapTo(0f)
            return@LaunchedEffect
        }

        if (loop) {
            while (isActive) {
                progress.snapTo(0f)
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = duration, easing = LinearEasing)
                )
            }
        } else {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = duration, easing = LinearEasing)
            )
        }
    }

    val ledColors: List<Color> = if (timeline != null && isPlaying && timeline.tracks.isNotEmpty()) {
        val tMs = (progress.value * duration).coerceIn(0f, duration.toFloat())
        computeTimelineRing(timeline, tMs)
    } else {
        List(ledCount) { Color(0xFF1F2937).copy(alpha = 0.28f) }
    }

    Canvas(modifier = modifier.size(size)) {
        val canvasSize = this.size
        val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
        val ringRadius = (canvasSize.minDimension / 2f) * 0.68f
        val ledRadiusPx = ledRadius.toPx()
        val baseRadius = canvasSize.minDimension / 2f
        val haloBrush = Brush.radialGradient(
            colors = listOf(Color(0xFF0B1021), Color(0xFF0F172A), Color(0xFF0B1021)),
            center = center,
            radius = baseRadius
        )
        val ringBrush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFF5EEAD4),
                Color(0xFF60A5FA),
                Color(0xFFA78BFA),
                Color(0xFF5EEAD4)
            ),
            center = center
        )

        drawCircle(brush = haloBrush, radius = baseRadius, center = center, alpha = 0.9f)
        drawCircle(
            color = Color.White.copy(alpha = haloAlpha.value),
            radius = baseRadius * 0.82f,
            center = center,
            style = Stroke(width = baseRadius * 0.02f)
        )
        rotate(degrees = orbitAngle.value, pivot = center) {
            drawArc(
                brush = ringBrush,
                startAngle = -110f,
                sweepAngle = 160f,
                useCenter = false,
                style = Stroke(width = baseRadius * 0.06f, cap = StrokeCap.Round),
                alpha = 0.9f
            )
        }
        drawArc(
            color = Color.White.copy(alpha = 0.35f),
            startAngle = -90f,
            sweepAngle = 360f * progress.value,
            useCenter = false,
            style = Stroke(width = baseRadius * 0.04f, cap = StrokeCap.Round)
        )

        // Рисуем каждый LED
        for (i in 0 until ledCount) {
            val angle = (i * 2 * PI / ledCount) - (PI / 2) // Начинаем сверху
            val ledX = center.x + (ringRadius * cos(angle)).toFloat()
            val ledY = center.y + (ringRadius * sin(angle)).toFloat()
            
            val ledColor = ledColors.getOrNull(i) ?: Color(0xFF1F2937).copy(alpha = 0.28f)
            val auraRadius = ledRadiusPx * (1.6f + (pulse.value - 1f))

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ledColor.copy(alpha = 0.0f), ledColor.copy(alpha = 0.08f), ledColor.copy(alpha = 0.4f)),
                    center = Offset(ledX, ledY),
                    radius = auraRadius * 1.3f
                ),
                radius = auraRadius * 1.3f,
                center = Offset(ledX, ledY)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ledColor.copy(alpha = 0.15f), ledColor.copy(alpha = 0.8f)),
                    center = Offset(ledX, ledY),
                    radius = ledRadiusPx * 1.15f
                ),
                radius = ledRadiusPx * 1.15f,
                center = Offset(ledX, ledY)
            )

            drawCircle(
                color = ledColor.copy(alpha = 0.95f),
                radius = ledRadiusPx * 0.9f,
                center = Offset(ledX, ledY)
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.28f * ledColor.alpha),
                radius = ledRadiusPx * 0.28f,
                center = Offset(ledX - ledRadiusPx * 0.25f, ledY - ledRadiusPx * 0.25f)
            )
        }
    }
}

private data class Contribution(val priority: Int, val mixMode: MixMode, val color: Color)

private fun computeTimelineRing(timeline: PatternTimeline, tMs: Float): List<Color> {
    val leds = 8
    val contributions = Array(leds) { mutableListOf<Contribution>() }

    timeline.tracks.forEach { track ->
        track.clips.forEach { clip ->
            val start = clip.startMs.toFloat()
            val end = start + clip.durationMs
            if (tMs >= start && tMs <= end) {
                val base = parseColor(clip.color)
                val rel = (tMs - start).coerceAtLeast(0f)
                val fadeInLin = if (clip.fadeInMs > 0) (rel / clip.fadeInMs).coerceIn(0f, 1f) else 1f
                val fadeIn = applyEasing(clip.easing, fadeInLin)
                val relOut = (end - tMs).coerceAtLeast(0f)
                val fadeOutLin = if (clip.fadeOutMs > 0) (relOut / clip.fadeOutMs).coerceIn(0f, 1f) else 1f
                val fadeOut = applyEasing(clip.easing, fadeOutLin)
                val factor = (fadeIn * fadeOut).coerceIn(0f, 1f)
                if (factor > 0f) {
                    val col = scaleColorIntensity(base, factor)
                    when (val target = track.target) {
                        is TargetLed -> if (target.index in 0 until leds) contributions[target.index].add(Contribution(track.priority, track.mixMode, col))
                        is TargetGroup -> target.indices.forEach { idx -> if (idx in 0 until leds) contributions[idx].add(Contribution(track.priority, track.mixMode, col)) }
                        is TargetRing -> (0 until leds).forEach { idx -> contributions[idx].add(Contribution(track.priority, track.mixMode, col)) }
                    }
                }
            }
        }
    }

    return contributions.map { list ->
        if (list.isEmpty()) Color(0f, 0f, 0f, 0.05f) else mixColors(list)
    }
}

private fun mixColors(items: List<Contribution>): Color {
    val sorted = items.sortedBy { it.priority }
    var acc = Color(0f, 0f, 0f, 0f)
    for (c in sorted) {
        acc = when (c.mixMode) {
            // OVERRIDE: более высокий приоритетный клип накладывается поверх нижележащего
            // с учётом альфы (fade-in/out), чтобы получить плавный кросс-фейд, а не резкий скачок.
            MixMode.OVERRIDE -> compositeOver(acc, c.color)
            MixMode.ADDITIVE -> addColor(acc, c.color)
        }
    }
    return acc.copy(alpha = acc.alpha.coerceIn(0f, 1f))
}

private fun compositeOver(dst: Color, src: Color): Color {
    val srcA = src.alpha.coerceIn(0f, 1f)
    val dstA = dst.alpha.coerceIn(0f, 1f)
    val outA = srcA + dstA * (1f - srcA)
    if (outA <= 0f) return Color(0f, 0f, 0f, 0f)

    val outR = (src.red * srcA + dst.red * dstA * (1f - srcA)) / outA
    val outG = (src.green * srcA + dst.green * dstA * (1f - srcA)) / outA
    val outB = (src.blue * srcA + dst.blue * dstA * (1f - srcA)) / outA

    return Color(outR, outG, outB, outA)
}

private fun addColor(a: Color, b: Color): Color {
    val ar = (a.red * 255f).toInt(); val ag = (a.green * 255f).toInt(); val ab = (a.blue * 255f).toInt()
    val br = (b.red * 255f).toInt(); val bg = (b.green * 255f).toInt(); val bb = (b.blue * 255f).toInt()
    val rr = (ar + br).coerceAtMost(255)
    val rg = (ag + bg).coerceAtMost(255)
    val rb = (ab + bb).coerceAtMost(255)
    val ra = (a.alpha + b.alpha).coerceIn(0f, 1f)
    return Color(rr, rg, rb).copy(alpha = ra)
}

private fun scaleColorIntensity(color: Color, factor: Float): Color {
    val f = factor.coerceIn(0f, 1f)
    return Color(
        red = color.red * f,
        green = color.green * f,
        blue = color.blue * f,
        alpha = color.alpha * f
    )
}

/**
 * Парсит HEX цвет в Color
 */
private fun parseColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val rgb = cleanHex.toLong(16)
        Color(
            red = ((rgb shr 16) and 0xFF) / 255f,
            green = ((rgb shr 8) and 0xFF) / 255f,
            blue = (rgb and 0xFF) / 255f
        )
    } catch (e: Exception) {
        Color.White
    }
}

// Easing helper for timeline preview (uses model's Easing, not Compose's)
private fun applyEasing(
    e: com.example.amulet.shared.domain.patterns.model.Easing,
    x: Float
): Float = when (e) {
    com.example.amulet.shared.domain.patterns.model.Easing.LINEAR -> x
}
