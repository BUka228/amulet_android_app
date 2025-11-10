package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.PatternId

/**
 * UseCase для удаления тега из паттерна.
 */
class RemoveTagFromPatternUseCase(
    private val repository: PatternsRepository
) {
    suspend operator fun invoke(
        patternId: PatternId,
        tag: String
    ): AppResult<Unit> {
        return repository.removeTag(patternId, tag)
    }
}
