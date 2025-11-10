package com.example.amulet.shared.domain.patterns.builder

import com.example.amulet.shared.domain.patterns.model.*

/**
 * Конструктор паттернов для максимально кастомизируемого создания анимаций.
 * Предоставляет fluent API для построения сложных паттернов.
 * 
 * Пример использования:
 * ```
 * val pattern = PatternBuilder()
 *     .setHardwareVersion(100)
 *     .setLoop(true)
 *     .addBreathing("#FF0000", 2000)
 *     .addPulse("#00FF00", 500, 3)
 *     .addChase("#0000FF", ChaseDirection.CLOCKWISE, 100)
 *     .build()
 * ```
 */
class PatternBuilder {
    private var hardwareVersion: Int = 100
    private var durationMs: Int? = null
    private var loop: Boolean = false
    private val elements = mutableListOf<PatternElement>()
    
    /**
     * Устанавливает версию железа (100 или 200).
     */
    fun setHardwareVersion(version: Int): PatternBuilder {
        this.hardwareVersion = version
        return this
    }
    
    /**
     * Устанавливает общую длительность паттерна в миллисекундах.
     */
    fun setDuration(durationMs: Int): PatternBuilder {
        this.durationMs = durationMs
        return this
    }
    
    /**
     * Устанавливает режим зацикливания паттерна.
     */
    fun setLoop(loop: Boolean): PatternBuilder {
        this.loop = loop
        return this
    }
    
    /**
     * Добавляет элемент дыхания (плавное изменение яркости).
     * 
     * @param color Цвет в HEX формате (#RRGGBB)
     * @param durationMs Длительность анимации в миллисекундах
     */
    fun addBreathing(color: String, durationMs: Int): PatternBuilder {
        elements.add(PatternElementBreathing(color, durationMs))
        return this
    }
    
    /**
     * Добавляет элемент пульсации (ритмичные вспышки).
     * 
     * @param color Цвет в HEX формате (#RRGGBB)
     * @param speed Интервал между пульсами в миллисекундах
     * @param repeats Количество повторений
     */
    fun addPulse(color: String, speed: Int, repeats: Int): PatternBuilder {
        elements.add(PatternElementPulse(color, speed, repeats))
        return this
    }
    
    /**
     * Добавляет элемент бегущих огней (движение света по кольцу).
     * 
     * @param color Цвет в HEX формате (#RRGGBB)
     * @param direction Направление движения (CLOCKWISE или COUNTER_CLOCKWISE)
     * @param speedMs Скорость между шагами в миллисекундах
     */
    fun addChase(color: String, direction: ChaseDirection, speedMs: Int): PatternBuilder {
        elements.add(PatternElementChase(color, direction, speedMs))
        return this
    }
    
    /**
     * Добавляет элемент заполнения кольца.
     * 
     * @param color Цвет в HEX формате (#RRGGBB)
     * @param durationMs Длительность заполнения в миллисекундах
     */
    fun addFill(color: String, durationMs: Int): PatternBuilder {
        elements.add(PatternElementFill(color, durationMs))
        return this
    }
    
    /**
     * Добавляет элемент спиннера (вращающиеся двухцветные эффекты).
     * 
     * @param color1 Первый цвет в HEX формате (#RRGGBB)
     * @param color2 Второй цвет в HEX формате (#RRGGBB)
     * @param speedMs Скорость вращения в миллисекундах
     */
    fun addSpinner(color1: String, color2: String, speedMs: Int): PatternBuilder {
        elements.add(PatternElementSpinner(listOf(color1, color2), speedMs))
        return this
    }
    
    /**
     * Добавляет элемент прогресс-бара.
     * 
     * @param color Цвет в HEX формате (#RRGGBB)
     * @param activeLeds Количество активных диодов (1-8)
     */
    fun addProgress(color: String, activeLeds: Int): PatternBuilder {
        elements.add(PatternElementProgress(color, activeLeds))
        return this
    }
    
    /**
     * Добавляет элемент последовательности для "секретных кодов".
     * 
     * @param steps Список шагов последовательности
     */
    fun addSequence(steps: List<SequenceStep>): PatternBuilder {
        elements.add(PatternElementSequence(steps))
        return this
    }
    
    /**
     * Добавляет элемент последовательности с помощью DSL-билдера.
     * 
     * Пример:
     * ```
     * addSequence {
     *     led(0, "#FF00FF", 150)
     *     delay(100)
     *     led(0, "#FF00FF", 150)
     *     delay(400)
     *     led(4, "#FFFF00", 200)
     * }
     * ```
     */
    fun addSequence(builder: SequenceBuilder.() -> Unit): PatternBuilder {
        val sequenceBuilder = SequenceBuilder()
        sequenceBuilder.builder()
        elements.add(PatternElementSequence(sequenceBuilder.build()))
        return this
    }
    
    /**
     * Добавляет произвольный элемент паттерна.
     */
    fun addElement(element: PatternElement): PatternBuilder {
        elements.add(element)
        return this
    }
    
    /**
     * Добавляет несколько элементов паттерна.
     */
    fun addElements(vararg elements: PatternElement): PatternBuilder {
        this.elements.addAll(elements)
        return this
    }
    
    /**
     * Очищает все добавленные элементы.
     */
    fun clear(): PatternBuilder {
        elements.clear()
        return this
    }
    
    /**
     * Строит финальную спецификацию паттерна.
     */
    fun build(): PatternSpec {
        return PatternSpec(
            type = "custom",
            hardwareVersion = hardwareVersion,
            durationMs = durationMs,
            loop = loop,
            elements = elements.toList()
        )
    }
}

/**
 * DSL-билдер для создания последовательностей.
 */
class SequenceBuilder {
    private val steps = mutableListOf<SequenceStep>()
    
    /**
     * Добавляет действие с конкретным диодом.
     * 
     * @param ledIndex Индекс диода (0-7)
     * @param color Цвет в HEX формате (#RRGGBB)
     * @param durationMs Длительность свечения в миллисекундах
     */
    fun led(ledIndex: Int, color: String, durationMs: Int) {
        steps.add(SequenceStep.LedAction(ledIndex, color, durationMs))
    }
    
    /**
     * Добавляет паузу между действиями.
     * 
     * @param durationMs Длительность задержки в миллисекундах
     */
    fun delay(durationMs: Int) {
        steps.add(SequenceStep.DelayAction(durationMs))
    }
    
    internal fun build(): List<SequenceStep> = steps.toList()
}

/**
 * Вспомогательная функция для создания паттерна с помощью DSL.
 * 
 * Пример:
 * ```
 * val pattern = buildPattern {
 *     setHardwareVersion(100)
 *     setLoop(true)
 *     addBreathing("#FF0000", 2000)
 *     addPulse("#00FF00", 500, 3)
 * }
 * ```
 */
fun buildPattern(builder: PatternBuilder.() -> Unit): PatternSpec {
    val patternBuilder = PatternBuilder()
    patternBuilder.builder()
    return patternBuilder.build()
}
