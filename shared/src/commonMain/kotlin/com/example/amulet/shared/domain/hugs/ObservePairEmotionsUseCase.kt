package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.domain.hugs.model.PairEmotion
import com.example.amulet.shared.domain.hugs.model.PairId
import kotlinx.coroutines.flow.Flow

class ObservePairEmotionsUseCase(
    private val repository: PairsRepository
) {
    operator fun invoke(pairId: PairId): Flow<List<PairEmotion>> =
        repository.observePairEmotions(pairId)
}
