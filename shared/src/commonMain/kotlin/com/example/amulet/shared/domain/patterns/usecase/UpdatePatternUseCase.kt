package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternUpdate
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.andThen

/**
 * UseCase для обновления паттерна с оптимистической блокировкой.
 */
class UpdatePatternUseCase(
    private val repository: PatternsRepository,
    private val validator: PatternValidator
) {
    suspend operator fun invoke(
        id: PatternId,
        version: Int,
        updates: PatternUpdate
    ): AppResult<Pattern> {
        // Валидация изменений
        val validationResult = updates.spec?.let { spec ->
            validator.validate(spec)
        } ?: Ok(Unit)
        
        return validationResult.andThen {
            // Обновление с версией
            repository.updatePattern(id, version, updates)
        }
    }
}
