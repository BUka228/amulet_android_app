package com.example.amulet.shared.domain.auth.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.auth.repository.AuthRepository

class EnableGuestModeUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(displayName: String? = null, language: String? = null): AppResult<Unit> =
        authRepository.enableGuestSession(displayName, language)
}
