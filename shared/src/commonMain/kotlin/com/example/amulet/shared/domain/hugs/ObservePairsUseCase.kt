package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.domain.hugs.model.Pair
import kotlinx.coroutines.flow.Flow

class ObservePairsUseCase(
    private val repository: PairsRepository
) {
    operator fun invoke(): Flow<List<Pair>> = repository.observePairs()
}
