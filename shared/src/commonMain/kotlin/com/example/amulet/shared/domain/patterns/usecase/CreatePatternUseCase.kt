package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternDraft
import com.github.michaelbull.result.andThen

/**
 * UseCase для создания паттерна с валидацией.
 */
class CreatePatternUseCase(
    private val repository: PatternsRepository,
    private val validator: PatternValidator
) {
    suspend operator fun invoke(
        draft: PatternDraft
    ): AppResult<Pattern> {
        Logger.d("Создание паттерна: ${draft.title}, тип: ${draft.kind}", "CreatePatternUseCase")
        // Валидация
        return validator.validate(draft.spec).andThen {
            Logger.d("Валидация пройдена, создание паттерна", "CreatePatternUseCase")
            // Создание
            repository.createPattern(draft)
        }
    }
}
