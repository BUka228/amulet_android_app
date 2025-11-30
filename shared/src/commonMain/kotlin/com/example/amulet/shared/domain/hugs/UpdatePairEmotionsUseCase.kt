package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.model.PairEmotion
import com.example.amulet.shared.domain.hugs.model.PairId

class UpdatePairEmotionsUseCase(
    private val repository: PairsRepository
) {
    suspend operator fun invoke(pairId: PairId, emotions: List<PairEmotion>): AppResult<Unit> =
        repository.updatePairEmotions(pairId, emotions)
}
