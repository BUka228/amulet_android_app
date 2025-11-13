package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.model.PatternElementBreathing
import com.example.amulet.shared.domain.patterns.model.PatternElementChase
import com.example.amulet.shared.domain.patterns.model.PatternElementFill
import com.example.amulet.shared.domain.patterns.model.PatternElementProgress
import com.example.amulet.shared.domain.patterns.model.PatternElementPulse
import com.example.amulet.shared.domain.patterns.model.PatternElementSequence
import com.example.amulet.shared.domain.patterns.model.PatternElementSpinner
import com.example.amulet.shared.domain.patterns.model.PatternElementTimeline
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.model.SequenceStep
import com.example.amulet.shared.domain.patterns.model.TargetGroup
import com.example.amulet.shared.domain.patterns.model.TargetLed
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok

/**
 * Валидатор паттернов.
 * Проверяет корректность спецификации паттерна.
 */
class PatternValidator {
    
    fun validate(spec: PatternSpec): AppResult<Unit> {
        // Проверка количества элементов
        if (spec.elements.size > MAX_ELEMENTS) {
            return Err(AppError.Validation(
                mapOf("elements" to "Максимум $MAX_ELEMENTS элементов")
            ))
        }
        
        // Проверка каждого элемента
        spec.elements.forEachIndexed { index, element ->
            when (element) {
                is PatternElementBreathing -> {
                    validateColor(element.color, index)?.let { return it }
                    validateDuration(element.durationMs, index)?.let { return it }
                }
                is PatternElementPulse -> {
                    validateColor(element.color, index)?.let { return it }
                    validateDuration(element.speed, index)?.let { return it }
                    if (element.repeats < 1) {
                        return Err(AppError.Validation(
                            mapOf("elements[$index].repeats" to "Количество повторений должно быть больше 0")
                        ))
                    }
                }
                is PatternElementChase -> {
                    validateColor(element.color, index)?.let { return it }
                    validateDuration(element.speedMs, index)?.let { return it }
                }
                is PatternElementFill -> {
                    validateColor(element.color, index)?.let { return it }
                    validateDuration(element.durationMs, index)?.let { return it }
                }
                is PatternElementSpinner -> {
                    if (element.colors.size != 2) {
                        return Err(AppError.Validation(
                            mapOf("elements[$index].colors" to "Спиннер должен иметь ровно 2 цвета")
                        ))
                    }
                    element.colors.forEachIndexed { colorIndex, color ->
                        validateColor(color, index)?.let { return it }
                    }
                    validateDuration(element.speedMs, index)?.let { return it }
                }
                is PatternElementProgress -> {
                    validateColor(element.color, index)?.let { return it }
                    if (element.activeLeds !in 1..8) {
                        return Err(AppError.Validation(
                            mapOf("elements[$index].activeLeds" to "Количество активных диодов должно быть от 1 до 8")
                        ))
                    }
                }
                is PatternElementSequence -> {
                    validateSequence(element, index)?.let { return it }
                }
                is PatternElementTimeline -> {
                    validateTimeline(element, index)?.let { return it }
                }
            }
        }
        
        return Ok(Unit)
    }
    
    private fun validateColor(color: String, index: Int): AppResult<Unit>? {
        if (!color.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
            return Err(AppError.Validation(
                mapOf("elements[$index].color" to "Некорректный формат цвета (ожидается #RRGGBB)")
            ))
        }
        return null
    }

    private fun validateTimeline(element: PatternElementTimeline, index: Int): AppResult<Unit>? {
        if (element.durationMs !in MIN_DURATION..MAX_DURATION) {
            return Err(AppError.Validation(
                mapOf("elements[$index].durationMs" to "Длительность должна быть от ${MIN_DURATION}ms до ${MAX_DURATION}ms")
            ))
        }
        if (element.tickMs !in 1..element.durationMs) {
            return Err(AppError.Validation(
                mapOf("elements[$index].tickMs" to "Шаг должен быть в диапазоне 1..durationMs")
            ))
        }
        element.tracks.forEachIndexed { tIdx, track ->
            when (val target = track.target) {
                is TargetLed -> if (target.index !in 0..7) {
                    return Err(AppError.Validation(
                        mapOf("elements[$index].tracks[$tIdx].target" to "Индекс диода должен быть от 0 до 7")
                    ))
                }
                is TargetGroup -> {
                    if (target.indices.isEmpty()) {
                        return Err(AppError.Validation(
                            mapOf("elements[$index].tracks[$tIdx].target" to "Группа не может быть пустой")
                        ))
                    }
                    if (target.indices.any { it !in 0..7 }) {
                        return Err(AppError.Validation(
                            mapOf("elements[$index].tracks[$tIdx].target" to "Все индексы группы должны быть 0..7")
                        ))
                    }
                    if (target.indices.size != target.indices.toSet().size) {
                        return Err(AppError.Validation(
                            mapOf("elements[$index].tracks[$tIdx].target" to "Индексы в группе не должны повторяться")
                        ))
                    }
                }
                else -> { }
            }

            val clips = track.clips
            clips.forEachIndexed { cIdx, c ->
                if (c.startMs !in 0..element.durationMs) {
                    return Err(AppError.Validation(
                        mapOf("elements[$index].tracks[$tIdx].clips[$cIdx].startMs" to "startMs вне диапазона 0..durationMs")
                    ))
                }
                if (c.durationMs !in MIN_DURATION..element.durationMs) {
                    return Err(AppError.Validation(
                        mapOf("elements[$index].tracks[$tIdx].clips[$cIdx].durationMs" to "durationMs должен быть от ${MIN_DURATION}ms")
                    ))
                }
                if (c.startMs + c.durationMs > element.durationMs) {
                    return Err(AppError.Validation(
                        mapOf("elements[$index].tracks[$tIdx].clips[$cIdx]" to "Клип выходит за пределы таймлайна")
                    ))
                }
                if (c.fadeInMs < 0 || c.fadeOutMs < 0 || c.fadeInMs > c.durationMs || c.fadeOutMs > c.durationMs) {
                    return Err(AppError.Validation(
                        mapOf("elements[$index].tracks[$tIdx].clips[$cIdx]" to "fadeIn/fadeOut должны быть в пределах длительности клипа")
                    ))
                }
                if (!c.color.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
                    return Err(AppError.Validation(
                        mapOf("elements[$index].tracks[$tIdx].clips[$cIdx].color" to "Некорректный формат цвета (ожидается #RRGGBB)")
                    ))
                }
            }
            val sorted = clips.sortedBy { it.startMs }
            for (i in 1 until sorted.size) {
                val prev = sorted[i - 1]
                val cur = sorted[i]
                if (cur.startMs < prev.startMs + prev.durationMs) {
                    return Err(AppError.Validation(
                        mapOf("elements[$index].tracks[$tIdx].clips" to "Клипы в одной дорожке не должны пересекаться")
                    ))
                }
            }
        }
        return null
    }
    
    private fun validateDuration(durationMs: Int, index: Int): AppResult<Unit>? {
        if (durationMs !in MIN_DURATION..MAX_DURATION) {
            return Err(AppError.Validation(
                mapOf("elements[$index].durationMs" to "Длительность должна быть от ${MIN_DURATION}ms до ${MAX_DURATION}ms")
            ))
        }
        return null
    }
    
    private fun validateSequence(element: PatternElementSequence, index: Int): AppResult<Unit>? {
        if (element.steps.isEmpty()) {
            return Err(AppError.Validation(
                mapOf("elements[$index].steps" to "Последовательность не может быть пустой")
            ))
        }
        
        element.steps.forEachIndexed { stepIndex, step ->
            when (step) {
                is SequenceStep.LedAction -> {
                    if (step.ledIndex !in 0..7) {
                        return Err(AppError.Validation(
                            mapOf("elements[$index].steps[$stepIndex].ledIndex" to "Индекс диода должен быть от 0 до 7")
                        ))
                    }
                    validateColor(step.color, index)?.let { return it }
                    validateDuration(step.durationMs, index)?.let { return it }
                }
                is SequenceStep.DelayAction -> {
                    validateDuration(step.durationMs, index)?.let { return it }
                }
            }
        }
        
        return null
    }
    
    companion object {
        const val MAX_ELEMENTS = 100
        const val MIN_DURATION = 100
        const val MAX_DURATION = 60000
    }
}
