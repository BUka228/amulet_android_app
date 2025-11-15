package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.UserPreferences

class UpdateUserPreferencesUseCase(
    private val repository: PracticesRepository
) {
    suspend operator fun invoke(preferences: UserPreferences): AppResult<Unit> = repository.updateUserPreferences(preferences)
}
