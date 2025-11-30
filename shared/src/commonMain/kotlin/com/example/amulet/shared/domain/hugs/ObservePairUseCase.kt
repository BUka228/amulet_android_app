package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.domain.hugs.model.Pair
import com.example.amulet.shared.domain.hugs.model.PairId
import kotlinx.coroutines.flow.Flow

class ObservePairUseCase(
    private val repository: PairsRepository
) {
    operator fun invoke(pairId: PairId): Flow<Pair?> = repository.observePair(pairId)
}
