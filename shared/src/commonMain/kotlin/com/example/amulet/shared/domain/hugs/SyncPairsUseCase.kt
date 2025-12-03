package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult

class SyncPairsUseCase(
    private val repository: PairsRepository
) {
    suspend operator fun invoke(): AppResult<Unit> = repository.syncPairs()
}
