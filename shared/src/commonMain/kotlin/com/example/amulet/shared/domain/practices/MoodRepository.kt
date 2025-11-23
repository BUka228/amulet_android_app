package com.example.amulet.shared.domain.practices

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.model.MoodEntry
import com.example.amulet.shared.domain.practices.model.MoodKind
import com.example.amulet.shared.domain.practices.model.MoodSource
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для работы с историей настроения пользователя.
 */
interface MoodRepository {

    /**
     * Логирует событие о настроении пользователя.
     */
    suspend fun logMood(
        mood: MoodKind,
        source: MoodSource,
    ): AppResult<Unit>

    /**
     * Стрим истории настроения пользователя (на будущее для графиков/статистики).
     */
    fun getMoodHistoryStream(): Flow<List<MoodEntry>>
}
