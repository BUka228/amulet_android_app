package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.PracticeSessionId

class PauseSessionUseCase(
    private val repository: PracticesRepository
) {
    suspend operator fun invoke(sessionId: PracticeSessionId): AppResult<Unit> = repository.pauseSession(sessionId)
}
