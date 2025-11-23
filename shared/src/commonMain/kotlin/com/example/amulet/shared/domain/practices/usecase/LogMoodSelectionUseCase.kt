package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.MoodRepository
import com.example.amulet.shared.domain.practices.model.MoodKind
import com.example.amulet.shared.domain.practices.model.MoodSource

/**
 * Use case для логирования выбора настроения пользователем.
 * По умолчанию считается, что источник — экран Practices Home.
 */
class LogMoodSelectionUseCase(
    private val repository: MoodRepository,
) {

    suspend operator fun invoke(
        mood: MoodKind,
        source: MoodSource = MoodSource.PRACTICES_HOME,
    ): AppResult<Unit> {
        return repository.logMood(mood = mood, source = source)
    }
}
