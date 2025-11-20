package com.example.amulet.feature.practices.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeCategory
import com.example.amulet.shared.domain.practices.model.PracticeFilter
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeType
import com.example.amulet.shared.domain.practices.usecase.GetActiveSessionStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetCategoriesStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetFavoritesStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetPracticesStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetRecommendationsStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetSessionsHistoryStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.PauseSessionUseCase
import com.example.amulet.shared.domain.practices.usecase.ResumeSessionUseCase
import com.example.amulet.shared.domain.practices.usecase.SearchPracticesUseCase
import com.example.amulet.shared.domain.practices.usecase.SetFavoritePracticeUseCase
import com.example.amulet.shared.domain.practices.usecase.StartPracticeUseCase
import com.example.amulet.shared.domain.practices.usecase.StopSessionUseCase
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.courses.usecase.GetCoursesStreamUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
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
    private val setFavoritePracticeUseCase: SetFavoritePracticeUseCase,
    private val startPracticeUseCase: StartPracticeUseCase,
    private val pauseSessionUseCase: PauseSessionUseCase,
    private val resumeSessionUseCase: ResumeSessionUseCase,
    private val stopSessionUseCase: StopSessionUseCase,
    private val searchPracticesUseCase: SearchPracticesUseCase,
    private val getCoursesStreamUseCase: GetCoursesStreamUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PracticesHomeState())
    val state: StateFlow<PracticesHomeState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<PracticesHomeEffect>()
    val effects = _effects.asSharedFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        observeData()
        updateGreeting()
        observeSearch()
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

    @OptIn(FlowPreview::class)
    private fun observeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val recommendationsFlow = getRecommendationsStreamUseCase(limit = 2)
            val quickRitualsFlow = getPracticesStreamUseCase(
                PracticeFilter(
                    durationToSec = 5 * 60
                )
            )
            val coursesFlow = getCoursesStreamUseCase()
            val recentFlow = getSessionsHistoryStreamUseCase(limit = 5)

            combine(
                recommendationsFlow,
                quickRitualsFlow,
                coursesFlow,
                recentFlow
            ) { recommendations, quickRituals, courses, recent ->
                Triple(recommendations, quickRituals, Pair(courses, recent))
            }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = Triple(emptyList(), emptyList(), Pair(emptyList(), emptyList()))
                )
                .collect { (recommendations, quickRituals, coursesAndRecent) ->
                    val (courses, recent) = coursesAndRecent
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            recommendedPractices = recommendations,
                            recommendedCourse = courses.firstOrNull(),
                            myCourses = courses,
                            quickRituals = quickRituals,
                            recentSessions = recent,
                            isNewUser = recent.isEmpty(),
                        )
                    }
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        _state.update {
                            it.copy(
                                searchResults = emptyList(),
                                isSearchLoading = false,
                                searchError = null,
                            )
                        }
                        return@collect
                    }

                    _state.update { it.copy(isSearchLoading = true, searchError = null) }

                    val result = searchPracticesUseCase(
                        query = query,
                        filter = PracticeFilter()
                    )
                    val (practices, error) = result

                    _state.update {
                        it.copy(
                            isSearchLoading = false,
                            searchResults = practices ?: emptyList(),
                            searchError = error,
                        )
                    }

                    if (error != null) {
                        emitEffect(PracticesHomeEffect.ShowError(error))
                    }
                }
        }
    }

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
            PracticesHomeIntent.EnterSearch -> enterSearch()
            PracticesHomeIntent.ExitSearch -> exitSearch()
            is PracticesHomeIntent.ChangeSearchQuery -> changeSearchQuery(intent.query)
        }
    }

    private fun onSelectMood(mood: MoodChip) {
        _state.update { it.copy(selectedMood = mood) }
        // На данном этапе фильтрация реализуется на уровне UI/подборок в будущем
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            // Сейчас используем существующие стримы, отдельного refresh каталога не дергаем
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun enterSearch() {
        _state.update {
            it.copy(
                isSearchMode = true,
                searchQuery = "",
                searchResults = emptyList(),
                isSearchLoading = false,
                searchError = null,
            )
        }
        searchQueryFlow.value = ""
    }

    private fun exitSearch() {
        _state.update {
            it.copy(
                isSearchMode = false,
                searchQuery = "",
                searchResults = emptyList(),
                isSearchLoading = false,
                searchError = null,
            )
        }
        searchQueryFlow.value = ""
    }

    private fun changeSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
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
