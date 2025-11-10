package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.amulet.shared.domain.patterns.model.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 2D визуализатор паттернов.
 * Отображает кольцо из 8 светодиодов с анимацией.
 */
@Composable
fun PatternVisualizer(
    pattern: Pattern,
    modifier: Modifier = Modifier,
    isAnimated: Boolean = false
) {
    val spec = pattern.spec
    
    if (isAnimated && spec.elements.isNotEmpty()) {
        AnimatedPatternVisualizer(
            spec = spec,
            modifier = modifier
        )
    } else {
        StaticPatternVisualizer(
            spec = spec,
            modifier = modifier
        )
    }
}

@Composable
private fun StaticPatternVisualizer(
    spec: PatternSpec,
    modifier: Modifier = Modifier
) {
    // Показываем статичную превью первого элемента
    val primaryColor = spec.elements.firstOrNull()?.let { element ->
        when (element) {
            is PatternElementBreathing -> parseColor(element.color)
            is PatternElementPulse -> parseColor(element.color)
            is PatternElementChase -> parseColor(element.color)
            is PatternElementFill -> parseColor(element.color)
            is PatternElementProgress -> parseColor(element.color)
            is PatternElementSpinner -> parseColor(element.colors.firstOrNull() ?: "#FFFFFF")
            is PatternElementSequence -> {
                element.steps.filterIsInstance<SequenceStep.LedAction>()
                    .firstOrNull()?.let { parseColor(it.color) }
            }
        }
    } ?: Color.Gray

    Canvas(modifier = modifier.fillMaxSize()) {
        drawLedRing(
            ledStates = List(8) { primaryColor },
            alpha = 0.7f
        )
    }
}

@Composable
private fun AnimatedPatternVisualizer(
    spec: PatternSpec,
    modifier: Modifier = Modifier
) {
    var currentElementIndex by remember { mutableStateOf(0) }
    val infiniteTransition = rememberInfiniteTransition(label = "pattern")
    
    // Простая анимация для демонстрации
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val element = spec.elements.getOrNull(currentElementIndex)
        
        when (element) {
            is PatternElementBreathing -> {
                drawBreathingEffect(
                    color = parseColor(element.color),
                    progress = animationProgress
                )
            }
            is PatternElementPulse -> {
                drawPulseEffect(
                    color = parseColor(element.color),
                    progress = animationProgress
                )
            }
            is PatternElementChase -> {
                drawChaseEffect(
                    color = parseColor(element.color),
                    progress = animationProgress,
                    clockwise = element.direction == ChaseDirection.CLOCKWISE
                )
            }
            is PatternElementFill -> {
                drawFillEffect(
                    color = parseColor(element.color),
                    progress = animationProgress
                )
            }
            is PatternElementSpinner -> {
                drawSpinnerEffect(
                    colors = element.colors.map { parseColor(it) },
                    progress = animationProgress
                )
            }
            is PatternElementProgress -> {
                drawProgressEffect(
                    color = parseColor(element.color),
                    activeLeds = element.activeLeds
                )
            }
            is PatternElementSequence -> {
                drawSequenceEffect(
                    steps = element.steps,
                    progress = animationProgress
                )
            }
            null -> {
                drawLedRing(ledStates = List(8) { Color.Gray }, alpha = 0.3f)
            }
        }
    }
}

// Эффекты рисования

private fun DrawScope.drawLedRing(
    ledStates: List<Color>,
    alpha: Float = 1f
) {
    val center = Offset(size.width / 2, size.height / 2)
    val ringRadius = size.minDimension / 3
    val ledRadius = size.minDimension / 16

    ledStates.forEachIndexed { index, color ->
        val angle = (index * 2 * PI / 8) - PI / 2 // Начинаем сверху
        val x = center.x + (ringRadius * cos(angle)).toFloat()
        val y = center.y + (ringRadius * sin(angle)).toFloat()

        drawCircle(
            color = color,
            radius = ledRadius,
            center = Offset(x, y),
            alpha = alpha
        )
    }
}

private fun DrawScope.drawBreathingEffect(
    color: Color,
    progress: Float
) {
    // Синусоидальная пульсация
    val alpha = (sin(progress * 2 * PI).toFloat() + 1) / 2
    drawLedRing(
        ledStates = List(8) { color },
        alpha = alpha
    )
}

private fun DrawScope.drawPulseEffect(
    color: Color,
    progress: Float
) {
    // Резкие вспышки
    val alpha = if (progress < 0.3f) 1f else 0.2f
    drawLedRing(
        ledStates = List(8) { color },
        alpha = alpha
    )
}

private fun DrawScope.drawChaseEffect(
    color: Color,
    progress: Float,
    clockwise: Boolean
) {
    val activeLed = if (clockwise) {
        (progress * 8).toInt() % 8
    } else {
        7 - ((progress * 8).toInt() % 8)
    }
    
    val ledStates = List(8) { index ->
        if (index == activeLed) color else Color.Gray
    }
    
    drawLedRing(ledStates = ledStates, alpha = 1f)
}

private fun DrawScope.drawFillEffect(
    color: Color,
    progress: Float
) {
    val activeLeds = (progress * 8).toInt().coerceIn(0, 8)
    
    val ledStates = List(8) { index ->
        if (index < activeLeds) color else Color.Gray
    }
    
    drawLedRing(ledStates = ledStates, alpha = 1f)
}

private fun DrawScope.drawSpinnerEffect(
    colors: List<Color>,
    progress: Float
) {
    val offset = (progress * 8).toInt() % 8
    
    val ledStates = List(8) { index ->
        val colorIndex = (index + offset) % colors.size
        colors.getOrElse(colorIndex) { Color.Gray }
    }
    
    drawLedRing(ledStates = ledStates, alpha = 1f)
}

private fun DrawScope.drawProgressEffect(
    color: Color,
    activeLeds: Int
) {
    val ledStates = List(8) { index ->
        if (index < activeLeds) color else Color.Gray
    }
    
    drawLedRing(ledStates = ledStates, alpha = 1f)
}

private fun DrawScope.drawSequenceEffect(
    steps: List<SequenceStep>,
    progress: Float
) {
    // Показываем первый LED action
    val ledAction = steps.filterIsInstance<SequenceStep.LedAction>().firstOrNull()
    
    val ledStates = List(8) { index ->
        if (ledAction != null && index == ledAction.ledIndex) {
            parseColor(ledAction.color)
        } else {
            Color.Gray
        }
    }
    
    drawLedRing(ledStates = ledStates, alpha = 1f)
}

// Утилиты

private fun parseColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val r = cleanHex.substring(0, 2).toInt(16)
        val g = cleanHex.substring(2, 4).toInt(16)
        val b = cleanHex.substring(4, 6).toInt(16)
        Color(r, g, b)
    } catch (e: Exception) {
        Color.Gray
    }
}
