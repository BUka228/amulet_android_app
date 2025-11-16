package com.example.amulet.shared.domain.practices

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeCategory
import com.example.amulet.shared.domain.practices.model.PracticeFilter
import com.example.amulet.shared.domain.practices.model.PracticeId
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeSessionId
import com.example.amulet.shared.domain.practices.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PracticesRepository {
    fun getPracticesStream(
        filter: PracticeFilter
    ): Flow<List<Practice>>

    fun getPracticeById(id: PracticeId): Flow<Practice?>

    fun getCategoriesStream(): Flow<List<PracticeCategory>>

    fun getFavoritesStream(): Flow<List<Practice>>

    fun getRecommendationsStream(limit: Int? = null): Flow<List<Practice>>

    suspend fun search(
        query: String,
        filter: PracticeFilter
    ): AppResult<List<Practice>>

    suspend fun refreshCatalog(): AppResult<Unit>

    suspend fun seedLocalData(): AppResult<Unit>

    suspend fun setFavorite(
        practiceId: PracticeId,
        favorite: Boolean
    ): AppResult<Unit>

    fun getActiveSessionStream(): Flow<PracticeSession?>

    fun getSessionsHistoryStream(limit: Int? = null): Flow<List<PracticeSession>>

    suspend fun startPractice(
        practiceId: PracticeId,
        intensity: Double? = null,
        brightness: Double? = null,
    ): AppResult<PracticeSession>

    suspend fun pauseSession(
        sessionId: PracticeSessionId
    ): AppResult<Unit>

    suspend fun resumeSession(
        sessionId: PracticeSessionId
    ): AppResult<Unit>

    suspend fun stopSession(
        sessionId: PracticeSessionId,
        completed: Boolean
    ): AppResult<PracticeSession>

    fun getUserPreferencesStream(): Flow<UserPreferences>

    suspend fun updateUserPreferences(
        preferences: UserPreferences
    ): AppResult<Unit>
}

