package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.PracticeAudioMode
import com.example.amulet.shared.domain.practices.model.PracticeId
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeSessionSource

class StartPracticeUseCase(
    private val repository: PracticesRepository
) {
    suspend operator fun invoke(
        practiceId: PracticeId,
        intensity: Double? = null,
        brightness: Double? = null,
        vibrationLevel: Double? = null,
        audioMode: PracticeAudioMode? = null,
        source: PracticeSessionSource? = PracticeSessionSource.Manual,
    ): AppResult<PracticeSession> =
        repository.startPractice(
            practiceId = practiceId,
            intensity = intensity,
            brightness = brightness,
            vibrationLevel = vibrationLevel,
            audioMode = audioMode,
            source = source,
        )
}
