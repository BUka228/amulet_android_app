package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternId

class GetPatternSegmentsUseCase(
    private val repository: PatternsRepository,
) {

    suspend operator fun invoke(parentId: PatternId): AppResult<List<Pattern>> {
        return repository.getSegmentsForPattern(parentId)
    }
}
