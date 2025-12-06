package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.model.PatternTimeline
import com.example.amulet.shared.domain.patterns.model.TimelineClip
import com.example.amulet.shared.domain.patterns.model.TimelineTrack
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
        Logger.d("Начало валидации паттерна: ${spec.type}, timeline", "PatternValidator")
        return validateTimeline(spec.timeline)
    }
    
    private fun validateColor(color: String, index: Int): AppResult<Unit>? {
        if (!color.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
            return Err(AppError.Validation(
                mapOf("elements[$index].color" to "Некорректный формат цвета (ожидается #RRGGBB)")
            ))
        }
        return null
    }

    private fun validateTimeline(timeline: PatternTimeline): AppResult<Unit> {
        if (timeline.durationMs !in MIN_DURATION..MAX_DURATION) {
            return Err(AppError.Validation(
                mapOf("timeline.durationMs" to "Длительность должна быть от ${MIN_DURATION}ms до ${MAX_DURATION}ms")
            ))
        }
        timeline.tracks.forEachIndexed { tIdx, track ->
            when (val target = track.target) {
                is TargetLed -> if (target.index !in 0..7) {
                    return Err(AppError.Validation(
                        mapOf("timeline.tracks[$tIdx].target" to "Индекс диода должен быть от 0 до 7")
                    ))
                }
                is TargetGroup -> {
                    if (target.indices.isEmpty()) {
                        return Err(AppError.Validation(
                            mapOf("timeline.tracks[$tIdx].target" to "Группа не может быть пустой")
                        ))
                    }
                    if (target.indices.any { it !in 0..7 }) {
                        return Err(AppError.Validation(
                            mapOf("timeline.tracks[$tIdx].target" to "Все индексы группы должны быть 0..7")
                        ))
                    }
                    if (target.indices.size != target.indices.toSet().size) {
                        return Err(AppError.Validation(
                            mapOf("timeline.tracks[$tIdx].target" to "Индексы в группе не должны повторяться")
                        ))
                    }
                }
                else -> { }
            }

            val clips: List<TimelineClip> = track.clips
            clips.forEachIndexed { cIdx, c ->
                if (c.startMs !in 0..timeline.durationMs) {
                    return Err(AppError.Validation(
                        mapOf("timeline.tracks[$tIdx].clips[$cIdx].startMs" to "startMs вне диапазона 0..durationMs")
                    ))
                }
                if (c.durationMs !in MIN_DURATION..timeline.durationMs) {
                    return Err(AppError.Validation(
                        mapOf("timeline.tracks[$tIdx].clips[$cIdx].durationMs" to "durationMs должен быть от ${MIN_DURATION}ms")
                    ))
                }
                if (c.startMs + c.durationMs > timeline.durationMs) {
                    return Err(AppError.Validation(
                        mapOf("timeline.tracks[$tIdx].clips[$cIdx]" to "Клип выходит за пределы таймлайна")
                    ))
                }
                if (c.fadeInMs < 0 || c.fadeOutMs < 0 || c.fadeInMs > c.durationMs || c.fadeOutMs > c.durationMs) {
                    return Err(AppError.Validation(
                        mapOf("timeline.tracks[$tIdx].clips[$cIdx]" to "fadeIn/fadeOut должны быть в пределах длительности клипа")
                    ))
                }
                if (!c.color.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
                    return Err(AppError.Validation(
                        mapOf("timeline.tracks[$tIdx].clips[$cIdx].color" to "Некорректный формат цвета (ожидается #RRGGBB)")
                    ))
                }
            }
            val sorted = clips.sortedBy { it.startMs }
            for (i in 1 until sorted.size) {
                val prev = sorted[i - 1]
                val cur = sorted[i]
                if (cur.startMs < prev.startMs + prev.durationMs) {
                    return Err(AppError.Validation(
                        mapOf("timeline.tracks[$tIdx].clips" to "Клипы в одной дорожке не должны пересекаться")
                    ))
                }
            }
        }
        return Ok(Unit)
    }
    
    companion object {
        const val MIN_DURATION = 100
        const val MAX_DURATION = 60000
    }
}
