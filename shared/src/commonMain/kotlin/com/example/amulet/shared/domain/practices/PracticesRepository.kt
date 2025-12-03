package com.example.amulet.shared.domain.practices

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeBadge
import com.example.amulet.shared.domain.practices.model.PracticeCategory
import com.example.amulet.shared.domain.practices.model.PracticeCollection
import com.example.amulet.shared.domain.practices.model.PracticeFilter
import com.example.amulet.shared.domain.practices.model.PracticeGoal
import com.example.amulet.shared.domain.practices.model.PracticeId
import com.example.amulet.shared.domain.practices.model.ScheduledSession
import com.example.amulet.shared.domain.practices.model.PracticePlan
import com.example.amulet.shared.domain.practices.model.PracticeSchedule
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeSessionSource
import com.example.amulet.shared.domain.practices.model.PracticeSessionId
import com.example.amulet.shared.domain.practices.model.PracticeStatistics
import com.example.amulet.shared.domain.practices.model.PracticeTag
import com.example.amulet.shared.domain.user.model.UserPreferences
import com.example.amulet.shared.domain.practices.model.PracticeAudioMode
import com.example.amulet.shared.domain.practices.model.MoodKind
import kotlinx.coroutines.flow.Flow

interface PracticesRepository {
    fun getPracticesStream(
        filter: PracticeFilter
    ): Flow<List<Practice>>

    fun getPracticeById(id: PracticeId): Flow<Practice?>

    fun getCategoriesStream(): Flow<List<PracticeCategory>>

    fun getFavoritesStream(): Flow<List<Practice>>

    fun getRecommendationsStream(limit: Int? = null, contextGoal: PracticeGoal? = null): Flow<List<Practice>>

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
        vibrationLevel: Double? = null,
        audioMode: PracticeAudioMode? = null,
        source: PracticeSessionSource? = PracticeSessionSource.Manual,
    ): AppResult<PracticeSession>

    suspend fun stopSession(
        sessionId: PracticeSessionId,
        completed: Boolean
    ): AppResult<PracticeSession>

    suspend fun updateSessionMoodBefore(
        sessionId: PracticeSessionId,
        moodBefore: MoodKind?,
    ): AppResult<PracticeSession>

    suspend fun updateSessionFeedback(
        sessionId: PracticeSessionId,
        rating: Int?,
        moodAfter: MoodKind?,
        feedbackNote: String?,
    ): AppResult<PracticeSession>

    fun getUserPreferencesStream(): Flow<UserPreferences>
    
    fun getSchedulesStream(): Flow<List<PracticeSchedule>>
    
    fun getScheduleByPracticeId(practiceId: PracticeId): Flow<PracticeSchedule?>

    suspend fun upsertSchedule(
        schedule: PracticeSchedule
    ): AppResult<Unit>

    suspend fun deleteSchedule(
        scheduleId: String
    ): AppResult<Unit>

    suspend fun updateUserPreferences(
        preferences: UserPreferences
    ): AppResult<Unit>

    suspend fun skipScheduledSession(
        session: ScheduledSession
    ): AppResult<Unit>

    // Plans
    fun getPlansStream(): Flow<List<PracticePlan>>
    fun getPlanById(id: String): Flow<PracticePlan?>
    fun getSchedulesByPlanStream(planId: String): Flow<List<PracticeSchedule>>
    suspend fun upsertPlan(plan: PracticePlan): AppResult<Unit>
    suspend fun deletePlan(planId: String): AppResult<Unit>

    // Statistics & badges
    fun getStatisticsStream(): Flow<PracticeStatistics?>
    fun getBadgesStream(): Flow<List<PracticeBadge>>

    // Tags & collections (для дома практик / каталога)
    fun getPracticeTagsStream(): Flow<List<PracticeTag>>
    fun getCollectionsStream(): Flow<List<PracticeCollection>>
}
