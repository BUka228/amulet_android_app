package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.SyncResult

/**
 * UseCase для ручной синхронизации паттернов с облаком.
 */
class SyncPatternsUseCase(
    private val repository: PatternsRepository
) {
    suspend operator fun invoke(): AppResult<SyncResult> {
        Logger.d("Начало синхронизации паттернов с облаком", "SyncPatternsUseCase")
        val result = repository.syncWithCloud()
        Logger.d("Синхронизация завершена: $result", "SyncPatternsUseCase")
        return result
    }
}
