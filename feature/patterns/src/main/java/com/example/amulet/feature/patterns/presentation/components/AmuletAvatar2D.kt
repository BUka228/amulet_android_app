package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.amulet.shared.domain.patterns.model.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
    
    // Состояние анимации для каждого LED
    var ledColors by remember { mutableStateOf(List(ledCount) { Color.Gray.copy(alpha = 0.2f) }) }
    var currentElementIndex by remember { mutableStateOf(0) }
    var elementProgress by remember { mutableStateOf(0f) }
    
    // Анимация паттерна
    LaunchedEffect(spec, isPlaying) {
        if (spec == null || !isPlaying || spec.elements.isEmpty()) {
            // Сброс в неактивное состояние
            ledColors = List(ledCount) { Color.Gray.copy(alpha = 0.2f) }
            currentElementIndex = 0
            elementProgress = 0f
            return@LaunchedEffect
        }
        
        val startTime = System.currentTimeMillis()
        
        while (isPlaying) {
            val elapsed = System.currentTimeMillis() - startTime
            
            // Вычисляем текущий элемент и прогресс
            var totalDuration = 0L
            var foundElement = false
            
            for (i in spec.elements.indices) {
                val element = spec.elements[i]
                val elementDuration = getElementDuration(element)
                
                if (elapsed < totalDuration + elementDuration) {
                    currentElementIndex = i
                    elementProgress = ((elapsed - totalDuration).toFloat() / elementDuration).coerceIn(0f, 1f)
                    foundElement = true
                    break
                }
                
                totalDuration += elementDuration
            }
            
            // Если паттерн закончился
            if (!foundElement) {
                if (spec.loop) {
                    // Перезапуск с начала
                    val loopElapsed = elapsed % totalDuration
                    totalDuration = 0L
                    for (i in spec.elements.indices) {
                        val element = spec.elements[i]
                        val elementDuration = getElementDuration(element)
                        
                        if (loopElapsed < totalDuration + elementDuration) {
                            currentElementIndex = i
                            elementProgress = ((loopElapsed - totalDuration).toFloat() / elementDuration).coerceIn(0f, 1f)
                            break
                        }
                        
                        totalDuration += elementDuration
                    }
                } else {
                    // Остановка
                    ledColors = List(ledCount) { Color.Gray.copy(alpha = 0.2f) }
                    break
                }
            }
            
            // Обновляем цвета LED на основе текущего элемента
            val currentElement = spec.elements.getOrNull(currentElementIndex)
            if (currentElement != null) {
                ledColors = calculateLedColors(currentElement, elementProgress, ledCount)
            }
            
            kotlinx.coroutines.delay(16) // ~60 FPS
        }
    }
    
    Canvas(modifier = modifier.size(size)) {
        val canvasSize = this.size
        val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
        val ringRadius = (canvasSize.minDimension / 2f) * 0.7f
        val ledRadiusPx = ledRadius.toPx()
        
        // Рисуем каждый LED
        for (i in 0 until ledCount) {
            val angle = (i * 2 * PI / ledCount) - (PI / 2) // Начинаем сверху
            val ledX = center.x + (ringRadius * cos(angle)).toFloat()
            val ledY = center.y + (ringRadius * sin(angle)).toFloat()
            
            val ledColor = ledColors.getOrNull(i) ?: Color.Gray.copy(alpha = 0.2f)
            
            // Рисуем glow effect
            if (ledColor.alpha > 0.3f) {
                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        color = ledColor.copy(alpha = 0.3f)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            asFrameworkPaint().apply {
                                setShadowLayer(
                                    ledRadiusPx * 1.5f,
                                    0f,
                                    0f,
                                    ledColor.copy(alpha = 0.6f).toArgb()
                                )
                            }
                        }
                    }
                    canvas.drawCircle(
                        Offset(ledX, ledY),
                        ledRadiusPx * 1.3f,
                        paint
                    )
                }
            }
            
            // Рисуем LED
            drawCircle(
                color = ledColor,
                radius = ledRadiusPx,
                center = Offset(ledX, ledY)
            )
        }
    }
}

/**
 * Вычисляет длительность элемента в миллисекундах
 */
private fun getElementDuration(element: PatternElement): Long {
    return when (element) {
        is PatternElementBreathing -> element.durationMs.toLong()
        is PatternElementChase -> element.speedMs.toLong() * 8L // Полный круг
        is PatternElementFill -> element.durationMs.toLong()
        is PatternElementPulse -> element.speed.toLong() * element.repeats.toLong()
        is PatternElementProgress -> 1000L // Статичный, показываем 1 секунду
        is PatternElementSequence -> {
            element.steps.sumOf { step ->
                when (step) {
                    is SequenceStep.LedAction -> step.durationMs.toLong()
                    is SequenceStep.DelayAction -> step.durationMs.toLong()
                }
            }
        }
        is PatternElementSpinner -> element.speedMs.toLong() * 8L // Полный оборот
    }
}

/**
 * Вычисляет цвета LED для текущего элемента и прогресса
 */
private fun calculateLedColors(
    element: PatternElement,
    progress: Float,
    ledCount: Int
): List<Color> {
    return when (element) {
        is PatternElementBreathing -> {
            // Плавное затухание и появление всех LED
            val alpha = if (progress < 0.5f) {
                progress * 2f // Fade in
            } else {
                2f - (progress * 2f) // Fade out
            }
            val color = parseColor(element.color).copy(alpha = alpha)
            List(ledCount) { color }
        }
        
        is PatternElementPulse -> {
            // Быстрая вспышка - повторяющиеся пульсы
            val pulseProgress = (progress * element.repeats) % 1f
            val alpha = if (pulseProgress < 0.3f) {
                1f
            } else {
                (1f - ((pulseProgress - 0.3f) / 0.7f)).coerceIn(0f, 1f)
            }
            val color = parseColor(element.color).copy(alpha = alpha)
            List(ledCount) { color }
        }
        
        is PatternElementChase -> {
            // Бегущие огни
            val position = (progress * ledCount).toInt()
            List(ledCount) { i ->
                val distance = minOf(
                    kotlin.math.abs(i - position),
                    kotlin.math.abs(i - position + ledCount),
                    kotlin.math.abs(i - position - ledCount)
                )
                
                if (distance == 0) {
                    parseColor(element.color)
                } else if (distance == 1) {
                    parseColor(element.color).copy(alpha = 0.5f)
                } else {
                    Color.Gray.copy(alpha = 0.2f)
                }
            }
        }
        
        is PatternElementFill -> {
            // Последовательное заполнение
            val activeLeds = (progress * ledCount).toInt()
            List(ledCount) { i ->
                if (i < activeLeds) {
                    parseColor(element.color)
                } else if (i == activeLeds) {
                    val partialProgress = (progress * ledCount) - activeLeds
                    parseColor(element.color).copy(alpha = partialProgress)
                } else {
                    Color.Gray.copy(alpha = 0.2f)
                }
            }
        }
        
        is PatternElementSpinner -> {
            // Вращающийся двухцветный эффект
            val rotation = (progress * ledCount).toInt()
            List(ledCount) { i ->
                val adjustedIndex = (i + rotation) % ledCount
                if (adjustedIndex < ledCount / 2) {
                    parseColor(element.colors.getOrNull(0) ?: "#FFFFFF")
                } else {
                    parseColor(element.colors.getOrNull(1) ?: "#000000")
                }
            }
        }
        
        is PatternElementProgress -> {
            // Статичный индикатор прогресса
            List(ledCount) { i ->
                if (i < element.activeLeds) {
                    parseColor(element.color)
                } else {
                    Color.Gray.copy(alpha = 0.2f)
                }
            }
        }
        
        is PatternElementSequence -> {
            // Пользовательская последовательность
            var elapsed = 0L
            val totalDuration = element.steps.sumOf { step ->
                when (step) {
                    is SequenceStep.LedAction -> step.durationMs.toLong()
                    is SequenceStep.DelayAction -> step.durationMs.toLong()
                }
            }
            val targetTime = (progress * totalDuration).toLong()
            
            // Создаем массив цветов для всех LED
            val colors = MutableList(ledCount) { Color.Gray.copy(alpha = 0.2f) }
            
            for (step in element.steps) {
                val stepDuration = when (step) {
                    is SequenceStep.LedAction -> step.durationMs.toLong()
                    is SequenceStep.DelayAction -> step.durationMs.toLong()
                }
                
                if (targetTime < elapsed + stepDuration) {
                    // Применяем текущий шаг
                    when (step) {
                        is SequenceStep.LedAction -> {
                            if (step.ledIndex in 0 until ledCount) {
                                colors[step.ledIndex] = parseColor(step.color)
                            }
                        }
                        is SequenceStep.DelayAction -> {
                            // Пауза - оставляем текущие цвета
                        }
                    }
                    return colors
                }
                elapsed += stepDuration
            }
            
            // Fallback
            colors
        }
    }
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
