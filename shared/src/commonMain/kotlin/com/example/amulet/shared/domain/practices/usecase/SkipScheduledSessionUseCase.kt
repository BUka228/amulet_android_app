package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.ScheduledSession

class SkipScheduledSessionUseCase(
    private val repository: PracticesRepository
) {
    suspend operator fun invoke(session: ScheduledSession): AppResult<Unit> =
        repository.skipScheduledSession(session)
}
