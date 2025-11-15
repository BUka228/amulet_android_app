package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.UserPreferences
import kotlinx.coroutines.flow.Flow

class GetUserPreferencesStreamUseCase(
    private val repository: PracticesRepository
) {
    operator fun invoke(): Flow<UserPreferences> = repository.getUserPreferencesStream()
}
