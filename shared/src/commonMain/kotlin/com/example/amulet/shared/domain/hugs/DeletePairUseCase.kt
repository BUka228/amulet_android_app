package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.model.PairId

class DeletePairUseCase(
    private val repository: PairsRepository
) {
    suspend operator fun invoke(pairId: PairId): AppResult<Unit> = repository.deletePair(pairId)
}
