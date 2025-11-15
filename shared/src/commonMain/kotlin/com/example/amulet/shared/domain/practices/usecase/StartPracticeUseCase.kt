package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.PracticeId
import com.example.amulet.shared.domain.practices.model.PracticeSession

class StartPracticeUseCase(
    private val repository: PracticesRepository
) {
    suspend operator fun invoke(
        practiceId: PracticeId,
        intensity: Double? = null,
        brightness: Double? = null
    ): AppResult<PracticeSession> = repository.startPractice(practiceId, intensity, brightness)
}
