package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternId
import kotlinx.coroutines.flow.Flow

/**
 * UseCase для получения паттерна по ID.
 */
class GetPatternByIdUseCase(
    private val repository: PatternsRepository
) {
    operator fun invoke(id: PatternId): Flow<Pattern?> {
        return repository.getPatternById(id)
    }
}
