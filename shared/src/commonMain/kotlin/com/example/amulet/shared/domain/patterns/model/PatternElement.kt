package com.example.amulet.shared.domain.patterns.model

import kotlinx.serialization.Serializable

/**
 * Базовый интерфейс для элементов паттерна.
 * Каждый элемент представляет собой отдельный эффект анимации.
 */
@Serializable
sealed interface PatternElement

/**
 * Элемент дыхания (BREATHING команда).
 * Плавное изменение яркости всех диодов.
 */
@Serializable
data class PatternElementBreathing(
    val color: String,  // HEX формат: #RRGGBB
    val durationMs: Int  // Длительность анимации
) : PatternElement

/**
 * Элемент пульсации (PULSE команда).
 * Ритмичные вспышки всех диодов.
 */
@Serializable
data class PatternElementPulse(
    val color: String,  // HEX формат: #RRGGBB
    val speed: Int,  // Интервал между пульсами в мс
    val repeats: Int  // Количество повторений
) : PatternElement

/**
 * Элемент бегущих огней (CHASE команда).
 * Эффект движения света по кольцу.
 */
@Serializable
data class PatternElementChase(
    val color: String,  // HEX формат: #RRGGBB
    val direction: ChaseDirection,  // CW или CCW
    val speedMs: Int  // Скорость между шагами
) : PatternElement

/**
 * Элемент заполнения кольца (FILL команда).
 * Постепенное заполнение всех диодов.
 */
@Serializable
data class PatternElementFill(
    val color: String,  // HEX формат: #RRGGBB
    val durationMs: Int  // Длительность заполнения
) : PatternElement

/**
 * Элемент спиннера (SPINNER команда).
 * Вращающиеся двухцветные эффекты.
 */
@Serializable
data class PatternElementSpinner(
    val colors: List<String>,  // 2 цвета в HEX формате
    val speedMs: Int  // Скорость вращения
) : PatternElement

/**
 * Элемент прогресс-бара (PROGRESS команда).
 * Отображение уровня/прогресса через количество активных диодов.
 */
@Serializable
data class PatternElementProgress(
    val color: String,  // HEX формат: #RRGGBB
    val activeLeds: Int  // Количество активных диодов (1-8)
) : PatternElement

/**
 * Элемент последовательности (для "секретных кодов").
 * Транслируется в SET_LED и DELAY команды.
 */
@Serializable
data class PatternElementSequence(
    val steps: List<SequenceStep>
) : PatternElement

/**
 * Шаг последовательности для секретных кодов.
 */
@Serializable
sealed interface SequenceStep {
    /**
     * Действие с конкретным диодом.
     */
    @Serializable
    data class LedAction(
        val ledIndex: Int,  // 0-7 для конкретного диода
        val color: String,  // HEX формат: #RRGGBB
        val durationMs: Int  // Длительность свечения
    ) : SequenceStep
    
    /**
     * Пауза между действиями.
     */
    @Serializable
    data class DelayAction(
        val durationMs: Int  // Длительность задержки
    ) : SequenceStep
}
