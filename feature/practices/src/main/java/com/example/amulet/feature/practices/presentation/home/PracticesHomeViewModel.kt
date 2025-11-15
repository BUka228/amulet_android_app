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

    private val _uiState = MutableStateFlow(PracticesHomeState())
    val uiState: StateFlow<PracticesHomeState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<PracticesHomeSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    private val searchQuery = MutableStateFlow("")
    private val filters = MutableStateFlow(PracticeFilter())

    init {
        observeStreams()
    }

    fun handleEvent(event: PracticesHomeEvent) {
        when (event) {
            is PracticesHomeEvent.Refresh -> refresh()
            is PracticesHomeEvent.SelectTab -> _uiState.update { it.copy(selectedTab = event.tab) }
            is PracticesHomeEvent.UpdateSearchQuery -> searchQuery.value = event.query
            is PracticesHomeEvent.ApplyFilters -> applyFilters(event)
            is PracticesHomeEvent.ClearFilters -> filters.value = PracticeFilter()
            is PracticesHomeEvent.SelectCategory -> filters.update { it.copy(categoryId = event.categoryId) }
            is PracticesHomeEvent.ToggleFavorite -> toggleFavorite(event.practiceId, event.favorite)
            is PracticesHomeEvent.StartPractice -> startPractice(event.practiceId)
            is PracticesHomeEvent.PauseSession -> pause(event.sessionId)
            is PracticesHomeEvent.ResumeSession -> resume(event.sessionId)
            is PracticesHomeEvent.StopSession -> stop(event.sessionId, event.completed)
            is PracticesHomeEvent.OpenPractice -> navigateToPractice(event.practiceId)
            is PracticesHomeEvent.OpenCourse -> navigateToCourse(event.courseId)
            is PracticesHomeEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun applyFilters(e: PracticesHomeEvent.ApplyFilters) {
        filters.update {
            it.copy(
                type = e.type,
                categoryId = e.categoryId,
                onlyFavorites = e.onlyFavorites,
                durationFromSec = e.durationFromSec,
                durationToSec = e.durationToSec
            )
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeStreams() {
        val categoriesFlow = getCategoriesStreamUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        val favoritesFlow = getFavoritesStreamUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        val activeSessionFlow = getActiveSessionStreamUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, null)
        val coursesFlow = getCoursesStreamUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        val recommendationsFlow = getRecommendationsStreamUseCase(10).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        val searchFlow = searchQuery
            .debounce(300)
            .distinctUntilChanged()
        val practicesStream = filters
            .flatMapLatest { f -> getPracticesStreamUseCase(f) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        val recentSessionsFlow = getSessionsHistoryStreamUseCase(20)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        viewModelScope.launch {
            val baseCombined = combine(
                practicesStream,
                recommendationsFlow,
                favoritesFlow
            ) { practices: List<Practice>, recs: List<Practice>, favs: List<Practice> ->
                Triple(practices, recs, favs)
            }

            combine(
                baseCombined,
                categoriesFlow,
                activeSessionFlow,
                coursesFlow,
                recentSessionsFlow
            ) { triple: Triple<List<Practice>, List<Practice>, List<Practice>>, cats: List<PracticeCategory>, session: PracticeSession?, courses: List<Course>, sessions ->
                val recentIds = sessions.map { it.practiceId }
                val recent = triple.first.filter { it.id in recentIds }.take(10)
                val continueCourses = courses // без отдельного прогресса отображаем как список продолжения
                PracticesHomeState(
                    isLoadingOverview = false,
                    isLoadingCourses = false,
                    filters = filters.value,
                    categories = cats,
                    practices = triple.first,
                    recommendations = triple.second,
                    recent = recent,
                    favorites = triple.third,
                    activeSession = session,
                    courses = courses,
                    continueCourses = continueCourses
                )
            }.collect { state -> _uiState.value = state }
        }

        viewModelScope.launch {
            searchFlow.collect { q ->
                if (q != null) {
                    val res = searchPracticesUseCase(q, filters.value)
                    res.component1()?.let { list ->
                        _uiState.update { it.copy(practices = list) }
                    }
                    // ошибки игнорируем для простоты UX; можно показать Snackbar через sideEffect
                }
            }
        }
    }

    private fun refresh() { }

    private fun toggleFavorite(practiceId: String, favorite: Boolean) {
        viewModelScope.launch { setFavoritePracticeUseCase(practiceId, favorite) }
    }

    private fun startPractice(practiceId: String) {
        viewModelScope.launch { startPracticeUseCase(practiceId) }
    }

    private fun pause(sessionId: String) {
        viewModelScope.launch { pauseSessionUseCase(sessionId) }
    }

    private fun resume(sessionId: String) {
        viewModelScope.launch { resumeSessionUseCase(sessionId) }
    }

    private fun stop(sessionId: String, completed: Boolean) {
        viewModelScope.launch { stopSessionUseCase(sessionId, completed) }
    }

    private fun navigateToPractice(id: String) {
        viewModelScope.launch { _sideEffect.emit(PracticesHomeSideEffect.NavigateToPracticeDetails(id)) }
    }

    private fun navigateToCourse(id: String) {
        viewModelScope.launch { _sideEffect.emit(PracticesHomeSideEffect.NavigateToCourseDetails(id)) }
    }
}
