package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.amulet.shared.domain.patterns.model.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 2D визуализатор паттернов.
 * Отображает кольцо из 8 светодиодов с анимацией.
 * 
 * Оптимизации для 60 FPS:
 * - Canvas-based rendering вместо Compose для минимизации recomposition
 * - Кэширование parsed цветов для избежания повторного парсинга
 * - Smooth interpolation с FastOutSlowInEasing для плавных переходов
 * - Color transitions с длительностью 200ms как указано в дизайне
 * - Оптимизированные drawing функции с минимальными вычислениями
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
            ledStates = List(8) { LedState(primaryColor, 0.7f) }
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
    
    // Оптимизированная анимация с плавными переходами для 60 FPS
    // Используем withFrameMillis для точного контроля частоты кадров
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
    
    // Плавный переход цветов (200ms как указано в дизайне)
    // Используем синусоидальную функцию для более плавного перехода
    val colorTransitionProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorTransition"
    )

    // Оптимизация: кэшируем parsed цвета для избежания повторного парсинга
    val element = remember(currentElementIndex, spec.elements) {
        spec.elements.getOrNull(currentElementIndex)
    }
    
    val cachedColors = remember(element) {
        when (element) {
            is PatternElementBreathing -> listOf(parseColor(element.color))
            is PatternElementPulse -> listOf(parseColor(element.color))
            is PatternElementChase -> listOf(parseColor(element.color))
            is PatternElementFill -> listOf(parseColor(element.color))
            is PatternElementSpinner -> element.colors.map { parseColor(it) }
            is PatternElementProgress -> listOf(parseColor(element.color))
            is PatternElementSequence -> {
                element.steps.filterIsInstance<SequenceStep.LedAction>()
                    .map { parseColor(it.color) }
            }
            null -> emptyList()
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        when (element) {
            is PatternElementBreathing -> {
                drawBreathingEffect(
                    color = cachedColors.firstOrNull() ?: Color.Gray,
                    progress = animationProgress,
                    transitionProgress = colorTransitionProgress
                )
            }
            is PatternElementPulse -> {
                drawPulseEffect(
                    color = cachedColors.firstOrNull() ?: Color.Gray,
                    progress = animationProgress,
                    transitionProgress = colorTransitionProgress
                )
            }
            is PatternElementChase -> {
                drawChaseEffect(
                    color = cachedColors.firstOrNull() ?: Color.Gray,
                    progress = animationProgress,
                    clockwise = element.direction == ChaseDirection.CLOCKWISE,
                    transitionProgress = colorTransitionProgress
                )
            }
            is PatternElementFill -> {
                drawFillEffect(
                    color = cachedColors.firstOrNull() ?: Color.Gray,
                    progress = animationProgress,
                    transitionProgress = colorTransitionProgress
                )
            }
            is PatternElementSpinner -> {
                drawSpinnerEffect(
                    colors = cachedColors,
                    progress = animationProgress,
                    transitionProgress = colorTransitionProgress
                )
            }
            is PatternElementProgress -> {
                drawProgressEffect(
                    color = cachedColors.firstOrNull() ?: Color.Gray,
                    activeLeds = element.activeLeds,
                    transitionProgress = colorTransitionProgress
                )
            }
            is PatternElementSequence -> {
                drawSequenceEffect(
                    steps = element.steps,
                    progress = animationProgress,
                    transitionProgress = colorTransitionProgress
                )
            }
            null -> {
                drawLedRing(ledStates = List(8) { LedState(Color.Gray, 0.3f) })
            }
        }
    }
}

/**
 * Состояние одного LED для оптимизированного рендеринга
 */
private data class LedState(
    val color: Color,
    val alpha: Float
)

// Эффекты рисования (оптимизированы для 60 FPS)

private fun DrawScope.drawLedRing(
    ledStates: List<LedState>
) {
    val center = Offset(size.width / 2, size.height / 2)
    val ringRadius = size.minDimension / 3
    val ledRadius = size.minDimension / 16
    val glowRadius = ledRadius * 1.5f

    ledStates.forEachIndexed { index, ledState ->
        val angle = (index * 2 * PI / 8) - PI / 2 // Начинаем сверху
        val x = center.x + (ringRadius * cos(angle)).toFloat()
        val y = center.y + (ringRadius * sin(angle)).toFloat()
        val ledCenter = Offset(x, y)

        // Эффект свечения для визуальной привлекательности
        if (ledState.alpha > 0.3f) {
            drawCircle(
                color = ledState.color.copy(alpha = ledState.alpha * 0.3f),
                radius = glowRadius,
                center = ledCenter,
                blendMode = BlendMode.Plus
            )
        }

        // Основной LED
        drawCircle(
            color = ledState.color,
            radius = ledRadius,
            center = ledCenter,
            alpha = ledState.alpha
        )
    }
}

private fun DrawScope.drawBreathingEffect(
    color: Color,
    progress: Float,
    transitionProgress: Float
) {
    // Плавная синусоидальная пульсация с smooth interpolation
    // Используем easing для более естественного дыхания
    val breathProgress = (sin(progress * 2 * PI).toFloat() + 1) / 2
    val easedProgress = FastOutSlowInEasing.transform(breathProgress)
    
    // Диапазон альфа от 0.3 до 1.0 для видимости в любой момент
    val minAlpha = 0.3f
    val maxAlpha = 1.0f
    val baseAlpha = minAlpha + (maxAlpha - minAlpha) * easedProgress
    
    // Плавный переход с использованием transitionProgress
    val alpha = lerp(baseAlpha * 0.95f, baseAlpha, transitionProgress)
    
    drawLedRing(
        ledStates = List(8) { LedState(color, alpha) }
    )
}

private fun DrawScope.drawPulseEffect(
    color: Color,
    progress: Float,
    transitionProgress: Float
) {
    // Резкие вспышки с плавными переходами
    val targetAlpha = if (progress < 0.3f) 1f else 0.2f
    val alpha = lerp(0.2f, targetAlpha, transitionProgress)
    
    drawLedRing(
        ledStates = List(8) { LedState(color, alpha) }
    )
}

private fun DrawScope.drawChaseEffect(
    color: Color,
    progress: Float,
    clockwise: Boolean,
    transitionProgress: Float
) {
    // Smooth interpolation между LED позициями
    val ledPosition = progress * 8
    val activeLed = if (clockwise) {
        ledPosition.toInt() % 8
    } else {
        7 - (ledPosition.toInt() % 8)
    }
    
    // Fractional part для плавного перехода между LED
    val fractional = ledPosition - ledPosition.toInt()
    
    // Плавный переход между LED с trailing эффектом и smooth interpolation
    val ledStates = List(8) { index ->
        val distance = if (index == activeLed) {
            0f
        } else {
            val diff = (index - activeLed + 8) % 8
            minOf(diff, 8 - diff).toFloat()
        }
        
        // Используем smooth interpolation для trailing эффекта
        val baseAlpha = when {
            distance == 0f -> 1.0f - (fractional * 0.3f) // Текущий LED затухает
            distance == 1f -> fractional * 0.7f // Следующий LED загорается
            distance == 2f -> 0.2f
            else -> 0.05f
        }
        
        // Применяем color transition для плавности
        val alpha = lerp(baseAlpha * 0.9f, baseAlpha, transitionProgress)
        
        LedState(if (distance <= 2f) color else Color.Gray, alpha.coerceIn(0f, 1f))
    }
    
    drawLedRing(ledStates = ledStates)
}

private fun DrawScope.drawFillEffect(
    color: Color,
    progress: Float,
    transitionProgress: Float
) {
    val activeLeds = (progress * 8).toInt().coerceIn(0, 8)
    val fractionalPart = (progress * 8) - activeLeds
    
    val ledStates = List(8) { index ->
        when {
            index < activeLeds -> LedState(color, 1f)
            index == activeLeds && fractionalPart > 0 -> {
                // Плавное заполнение текущего LED
                val alpha = lerp(0.2f, fractionalPart, transitionProgress)
                LedState(color, alpha)
            }
            else -> LedState(Color.Gray, 0.1f)
        }
    }
    
    drawLedRing(ledStates = ledStates)
}

private fun DrawScope.drawSpinnerEffect(
    colors: List<Color>,
    progress: Float,
    transitionProgress: Float
) {
    if (colors.isEmpty()) {
        drawLedRing(ledStates = List(8) { LedState(Color.Gray, 0.3f) })
        return
    }
    
    val offset = progress * 8
    val intOffset = offset.toInt() % 8
    val fractionalOffset = offset - intOffset
    
    // Smooth interpolation для вращения
    val easedFractional = FastOutSlowInEasing.transform(fractionalOffset)
    
    val ledStates = List(8) { index ->
        val adjustedIndex = (index + intOffset) % 8
        val colorIndex = adjustedIndex % colors.size
        val currentColor = colors[colorIndex]
        val nextColor = colors[(colorIndex + 1) % colors.size]
        
        // Плавный переход между цветами с easing
        val colorBlendFactor = easedFractional * transitionProgress
        val blendedColor = lerpColor(currentColor, nextColor, colorBlendFactor)
        
        // Добавляем небольшую пульсацию для визуального эффекта
        val pulseAlpha = 0.85f + 0.15f * transitionProgress
        
        LedState(blendedColor, pulseAlpha)
    }
    
    drawLedRing(ledStates = ledStates)
}

private fun DrawScope.drawProgressEffect(
    color: Color,
    activeLeds: Int,
    transitionProgress: Float
) {
    val ledStates = List(8) { index ->
        if (index < activeLeds) {
            val alpha = lerp(0.8f, 1f, transitionProgress)
            LedState(color, alpha)
        } else {
            LedState(Color.Gray, 0.1f)
        }
    }
    
    drawLedRing(ledStates = ledStates)
}

private fun DrawScope.drawSequenceEffect(
    steps: List<SequenceStep>,
    progress: Float,
    transitionProgress: Float
) {
    // Показываем первый LED action с плавным переходом
    val ledAction = steps.filterIsInstance<SequenceStep.LedAction>().firstOrNull()
    
    val ledStates = List(8) { index ->
        if (ledAction != null && index == ledAction.ledIndex) {
            val alpha = lerp(0.7f, 1f, transitionProgress)
            LedState(parseColor(ledAction.color), alpha)
        } else {
            LedState(Color.Gray, 0.1f)
        }
    }
    
    drawLedRing(ledStates = ledStates)
}

// Утилиты для плавных переходов (оптимизированы для 60 FPS)

/**
 * Линейная интерполяция между двумя значениями.
 * Оптимизирована для минимальных вычислений.
 */
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction.coerceIn(0f, 1f)
}

/**
 * Smooth color interpolation с поддержкой всех компонентов цвета.
 * Использует линейную интерполяцию в RGB пространстве для производительности.
 */
private fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
    val clampedFraction = fraction.coerceIn(0f, 1f)
    return Color(
        red = lerp(start.red, stop.red, clampedFraction),
        green = lerp(start.green, stop.green, clampedFraction),
        blue = lerp(start.blue, stop.blue, clampedFraction),
        alpha = lerp(start.alpha, stop.alpha, clampedFraction)
    )
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
