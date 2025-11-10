package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.PatternId

/**
 * UseCase для добавления тега к паттерну.
 */
class AddTagToPatternUseCase(
    private val repository: PatternsRepository
) {
    suspend operator fun invoke(
        patternId: PatternId,
        tag: String
    ): AppResult<Unit> {
        return repository.addTag(patternId, tag)
    }
}
