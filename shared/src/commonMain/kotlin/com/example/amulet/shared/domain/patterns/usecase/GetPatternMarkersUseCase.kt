package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternMarkers

class GetPatternMarkersUseCase(
    private val repository: PatternsRepository,
) {

    suspend operator fun invoke(patternId: PatternId): AppResult<PatternMarkers?> {
        return repository.getPatternMarkers(patternId)
    }
}
