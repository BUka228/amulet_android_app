package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeSessionId

class StopSessionUseCase(
    private val repository: PracticesRepository
) {
    suspend operator fun invoke(
        sessionId: PracticeSessionId,
        completed: Boolean
    ): AppResult<PracticeSession> = repository.stopSession(sessionId, completed)
}
