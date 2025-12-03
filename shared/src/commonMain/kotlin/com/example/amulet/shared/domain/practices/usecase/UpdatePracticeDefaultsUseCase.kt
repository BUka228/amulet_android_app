package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.model.PracticeAudioMode
import com.example.amulet.shared.domain.user.model.UserPreferences
import kotlinx.coroutines.flow.first

/**
 * Фасад для частичного обновления пользовательских настроек практик (дефолтные значения).
 *
 * Позволяет UI передать только изменённые поля, не пересобирая весь UserPreferences.
 * Остальные поля копируются из текущего состояния.
 */
class UpdatePracticeDefaultsUseCase(
    private val getUserPreferencesStreamUseCase: GetUserPreferencesStreamUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
) {

    /**
     * Все параметры опциональны. Если параметр == null, поле остаётся без изменений.
     * Если параметр передан (включая пустые списки), значение перезаписывается.
     */
    suspend operator fun invoke(
        defaultIntensity: Double? = null,
        defaultBrightness: Double? = null,
        defaultAudioMode: PracticeAudioMode? = null,
        goals: List<String>? = null,
        interests: List<String>? = null,
        preferredDurationsSec: List<Int>? = null,
    ): AppResult<Unit> {
        val current: UserPreferences = getUserPreferencesStreamUseCase().first()

        val updated = current.copy(
            defaultIntensity = defaultIntensity ?: current.defaultIntensity,
            defaultBrightness = defaultBrightness ?: current.defaultBrightness,
            defaultAudioMode = defaultAudioMode ?: current.defaultAudioMode,
            goals = goals ?: current.goals,
            interests = interests ?: current.interests,
            preferredDurationsSec = preferredDurationsSec ?: current.preferredDurationsSec,
            // hugsDndEnabled не трогаем — им управляет отдельный флоу Hugs DND
            hugsDndEnabled = current.hugsDndEnabled,
        )

        return updateUserPreferencesUseCase(updated)
    }
}
