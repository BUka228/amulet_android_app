package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.domain.hugs.model.Hug
import com.example.amulet.shared.domain.hugs.model.PairId
import kotlinx.coroutines.flow.Flow

class ObserveHugsForPairUseCase(
    private val repository: HugsRepository
) {
    operator fun invoke(pairId: PairId): Flow<List<Hug>> =
        repository.observeHugsForPair(pairId)
}
