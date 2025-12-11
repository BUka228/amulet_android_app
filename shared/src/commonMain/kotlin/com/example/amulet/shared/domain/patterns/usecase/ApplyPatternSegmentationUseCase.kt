package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.andThen
import kotlinx.coroutines.flow.firstOrNull

/**
 * Обвязочный use case для применения разбиения паттерна на сегменты.
 */
class ApplyPatternSegmentationUseCase(
    private val repository: PatternsRepository,
    private val slicePatternIntoSegments: SlicePatternIntoSegmentsUseCase,
) {

    suspend operator fun invoke(
        patternId: PatternId,
        markersMs: List<Int>,
    ): AppResult<Unit> {
        // 1. Загружаем базовый паттерн из локального репозитория (предполагается, что он уже ensureLoaded)
        val basePattern = repository.getPatternById(patternId).firstOrNull()
            ?: return Err(AppError.NotFound)

        // 2. Считаем сегменты через слайсер
        return slicePatternIntoSegments(basePattern, markersMs).andThen { segments ->
            if (segments.isEmpty()) {
                Err(AppError.Validation(mapOf("segments" to "No segments produced for given markers")))
            } else {
                // 3. Пересохраняем сегменты для данного паттерна
                repository.upsertSegmentsForPattern(patternId, segments)
            }
        }
    }
}
