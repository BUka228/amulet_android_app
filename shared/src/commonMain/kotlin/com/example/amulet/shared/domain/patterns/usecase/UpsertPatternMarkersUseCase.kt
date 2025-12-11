package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.PatternMarkers

class UpsertPatternMarkersUseCase(
    private val repository: PatternsRepository,
) {

    suspend operator fun invoke(markers: PatternMarkers): AppResult<Unit> {
        return repository.upsertPatternMarkers(markers)
    }
}
