package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.PatternId

/**
 * UseCase для удаления паттерна с проверкой зависимостей.
 */
class DeletePatternUseCase(
    private val repository: PatternsRepository
) {
    suspend operator fun invoke(
        id: PatternId
    ): AppResult<Unit> {
        return repository.deletePattern(id)
    }
}
