package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternFilter
import kotlinx.coroutines.flow.Flow

/**
 * UseCase для получения списка паттернов с фильтрацией.
 */
class GetPatternsStreamUseCase(
    private val repository: PatternsRepository
) {
    operator fun invoke(
        filter: PatternFilter
    ): Flow<List<Pattern>> {
        return repository.getPatternsStream(filter)
    }
}
