package com.example.amulet.feature.practices.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.practices.model.PracticeFilter
import com.example.amulet.shared.domain.practices.usecase.GetActiveSessionStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetCategoriesStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetFavoritesStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetPracticesStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetRecommendationsStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetSessionsHistoryStreamUseCase
import com.example.amulet.shared.domain.courses.usecase.GetCourseProgressStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.PauseSessionUseCase
import com.example.amulet.shared.domain.practices.usecase.ResumeSessionUseCase
import com.example.amulet.shared.domain.practices.usecase.SetFavoritePracticeUseCase
import com.example.amulet.shared.domain.practices.usecase.StartPracticeUseCase
import com.example.amulet.shared.domain.practices.usecase.StopSessionUseCase
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.courses.usecase.GetCoursesStreamUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PracticesHomeViewModel @Inject constructor(
    private val getPracticesStreamUseCase: GetPracticesStreamUseCase,
    private val getCategoriesStreamUseCase: GetCategoriesStreamUseCase,
    private val getFavoritesStreamUseCase: GetFavoritesStreamUseCase,
    private val getRecommendationsStreamUseCase: GetRecommendationsStreamUseCase,
    private val getActiveSessionStreamUseCase: GetActiveSessionStreamUseCase,
    private val getSessionsHistoryStreamUseCase: GetSessionsHistoryStreamUseCase,
    private val getScheduledSessionsStreamUseCase: com.example.amulet.shared.domain.practices.usecase.GetScheduledSessionsStreamUseCase,
    private val setFavoritePracticeUseCase: SetFavoritePracticeUseCase,
    private val getCoursesStreamUseCase: GetCoursesStreamUseCase,
    private val getAllCoursesProgressStreamUseCase: com.example.amulet.shared.domain.courses.usecase.GetAllCoursesProgressStreamUseCase,
    private val refreshPracticesCatalogUseCase: com.example.amulet.shared.domain.practices.usecase.RefreshPracticesCatalogUseCase,
    private val refreshCoursesCatalogUseCase: com.example.amulet.shared.domain.courses.usecase.RefreshCoursesCatalogUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PracticesHomeState())
    val state: StateFlow<PracticesHomeState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<PracticesHomeEffect>()
    val effects = _effects.asSharedFlow()

    init {
        observeData()
        updateGreeting()
    }

    private fun updateGreeting() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> "Доброе утро"
            in 12..17 -> "Добрый день"
            in 18..22 -> "Добрый вечер"
            else -> "Доброй ночи"
        }
        _state.update { it.copy(greeting = greeting) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val moodFlow = state.map { it.selectedMood }.distinctUntilChanged()

            val recommendationsFlow = moodFlow.flatMapLatest { mood ->
                getRecommendationsStreamUseCase(limit = 5, goal = mood.practiceGoal)
            }
            
            val quickRitualsFlow = getPracticesStreamUseCase(
                PracticeFilter(
                    durationToSec = 300 // 5 minutes
                )
            )
            val coursesFlow = getCoursesStreamUseCase()
            val recentFlow = getSessionsHistoryStreamUseCase(limit = 5)
            val scheduledFlow = getScheduledSessionsStreamUseCase()
            val coursesProgressFlow = getAllCoursesProgressStreamUseCase()
            val allPracticesFlow = getPracticesStreamUseCase(PracticeFilter())

            combine(
                listOf(
                    recommendationsFlow,
                    quickRitualsFlow,
                    coursesFlow,
                    recentFlow,
                    scheduledFlow,
                    coursesProgressFlow,
                    allPracticesFlow
                )
            ) { args ->
                val recommendations = args[0] as List<com.example.amulet.shared.domain.practices.model.Practice>
                val quickRituals = args[1] as List<com.example.amulet.shared.domain.practices.model.Practice>
                val courses = args[2] as List<Course>
                val recent = args[3] as List<com.example.amulet.shared.domain.practices.model.PracticeSession>
                val scheduled = args[4] as List<com.example.amulet.shared.domain.practices.model.ScheduledSession>
                val coursesProgress = args[5] as List<com.example.amulet.shared.domain.courses.model.CourseProgress>
                val allPractices = args[6] as List<com.example.amulet.shared.domain.practices.model.Practice>
                val currentMood = _state.value.selectedMood

                val recommendedCourse = courses.find { it.goal == currentMood.practiceGoal } 
                    ?: courses.firstOrNull { it.tags.contains("popular") }
                    ?: courses.firstOrNull()

                val progressMap = coursesProgress.associateBy { it.courseId }
                val practicesMap = allPractices.associateBy { it.id }
                
                val recentUi = recent.map { session ->
                    RecentSessionUi(
                        id = session.id,
                        practiceId = session.practiceId,
                        practiceTitle = practicesMap[session.practiceId]?.title ?: "Практика",
                        durationSec = session.durationSec
                    )
                }
                
                PracticesHomeData(recommendations, quickRituals, courses, recommendedCourse, recentUi, scheduled, progressMap)
            }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = PracticesHomeData()
                )
                .collect { data ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            recommendedPractices = data.recommendations,
                            recommendedCourse = data.recommendedCourse,
                            myCourses = data.courses,
                            coursesProgress = data.coursesProgress,
                            quickRituals = data.quickRituals,
                            recentSessions = data.recent,
                            scheduledSessions = data.scheduled,
                            isNewUser = data.recent.isEmpty(),
                            hasPlan = data.scheduled.isNotEmpty()
                        )
                    }
                }
        }
    }

    private data class PracticesHomeData(
        val recommendations: List<com.example.amulet.shared.domain.practices.model.Practice> = emptyList(),
        val quickRituals: List<com.example.amulet.shared.domain.practices.model.Practice> = emptyList(),
        val courses: List<Course> = emptyList(),
        val recommendedCourse: Course? = null,
        val recent: List<RecentSessionUi> = emptyList(),
        val scheduled: List<com.example.amulet.shared.domain.practices.model.ScheduledSession> = emptyList(),
        val coursesProgress: Map<String, com.example.amulet.shared.domain.courses.model.CourseProgress> = emptyMap()
    )

    fun onIntent(intent: PracticesHomeIntent) {
        when (intent) {
            is PracticesHomeIntent.SelectMood -> onSelectMood(intent.mood)
            PracticesHomeIntent.Refresh -> refresh()
            is PracticesHomeIntent.OpenPractice -> emitEffect(PracticesHomeEffect.NavigateToPractice(intent.practiceId))
            is PracticesHomeIntent.OpenCourse -> emitEffect(PracticesHomeEffect.NavigateToCourse(intent.courseId))
            is PracticesHomeIntent.ToggleFavorite -> toggleFavorite(intent.practiceId, intent.favorite)
            PracticesHomeIntent.OpenSchedule -> emitEffect(PracticesHomeEffect.NavigateToSchedule)
            PracticesHomeIntent.OpenStats -> emitEffect(PracticesHomeEffect.NavigateToStats)
            PracticesHomeIntent.OpenSearch -> emitEffect(PracticesHomeEffect.NavigateToSearch)
            PracticesHomeIntent.CreateDayRitual -> emitEffect(PracticesHomeEffect.NavigateToSchedule)
            is PracticesHomeIntent.RescheduleSession -> emitEffect(PracticesHomeEffect.NavigateToSchedule) // Placeholder
            is PracticesHomeIntent.CancelSession -> { /* TODO: Implement cancel session logic */ }
            is PracticesHomeIntent.ShowPracticeDetails -> emitEffect(PracticesHomeEffect.NavigateToPractice(intent.practiceId))
        }
    }

    private fun onSelectMood(mood: MoodChip) {
        _state.update { it.copy(selectedMood = mood) }
        // На данном этапе фильтрация реализуется на уровне UI/подборок в будущем
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            // Обновляем данные через UseCases
            refreshPracticesCatalogUseCase()
            refreshCoursesCatalogUseCase()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun toggleFavorite(practiceId: String, favorite: Boolean) {
        viewModelScope.launch {
            val result = setFavoritePracticeUseCase(practiceId, favorite)
            val error = result.component2()
            if (error != null) {
                emitEffect(PracticesHomeEffect.ShowError(error))
            }
        }
    }

    private fun emitEffect(effect: PracticesHomeEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
