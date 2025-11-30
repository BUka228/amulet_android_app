package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.model.UserPreferences
import com.example.amulet.shared.domain.practices.usecase.GetUserPreferencesStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.UpdateUserPreferencesUseCase
import kotlinx.coroutines.flow.first

/**
 * Helper-use case для управления глобальным режимом DND для «объятий».
 * Позволяет экрану настроек одним вызовом включать/выключать DND.
 */
class SetHugsDndEnabledUseCase(
    private val getUserPreferencesStream: GetUserPreferencesStreamUseCase,
    private val updateUserPreferences: UpdateUserPreferencesUseCase,
) {

    suspend operator fun invoke(enabled: Boolean): AppResult<Unit> {
        val current: UserPreferences = getUserPreferencesStream().first()
        val updated = current.copy(hugsDndEnabled = enabled)
        return updateUserPreferences(updated)
    }
}
