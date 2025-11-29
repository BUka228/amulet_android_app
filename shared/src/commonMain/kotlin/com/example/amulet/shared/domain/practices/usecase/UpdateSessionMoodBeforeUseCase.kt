package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.MoodRepository
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.MoodKind
import com.example.amulet.shared.domain.practices.model.MoodSource
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeSessionId

class UpdateSessionMoodBeforeUseCase(
    private val practicesRepository: PracticesRepository,
    private val moodRepository: MoodRepository,
) {

    suspend operator fun invoke(
        sessionId: PracticeSessionId,
        moodBefore: MoodKind?,
    ): AppResult<PracticeSession> {
        val result = practicesRepository.updateSessionMoodBefore(
            sessionId = sessionId,
            moodBefore = moodBefore,
        )

        if (moodBefore != null) {
            // Логируем событие настроения до практики, но не меняем основной результат use case.
            moodRepository.logMood(
                mood = moodBefore,
                source = MoodSource.PRACTICE_BEFORE,
            )
        }

        return result
    }
}
