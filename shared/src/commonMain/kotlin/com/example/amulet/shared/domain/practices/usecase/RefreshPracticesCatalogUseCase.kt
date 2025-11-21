package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.PracticesRepository
class RefreshPracticesCatalogUseCase(
    private val repository: PracticesRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return repository.refreshCatalog()
    }
}
