package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.Practice

class UpsertPracticeUseCase(
    private val repository: PracticesRepository
) {
    suspend operator fun invoke(practice: Practice): AppResult<Unit> = repository.upsertPractice(practice)
}
