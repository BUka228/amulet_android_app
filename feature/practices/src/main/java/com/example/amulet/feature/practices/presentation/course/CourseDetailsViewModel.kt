package com.example.amulet.feature.practices.presentation.course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.courses.usecase.CheckItemUnlockUseCase
import com.example.amulet.shared.domain.courses.usecase.CompleteCourseItemUseCase
import com.example.amulet.shared.domain.courses.usecase.ContinueCourseUseCase
import com.example.amulet.shared.domain.courses.usecase.GetCourseByIdUseCase
import com.example.amulet.shared.domain.courses.usecase.GetCourseItemsStreamUseCase
import com.example.amulet.shared.domain.courses.usecase.GetCourseModulesStreamUseCase
import com.example.amulet.shared.domain.courses.usecase.GetCourseProgressStreamUseCase
import com.example.amulet.shared.domain.courses.usecase.ResetCourseProgressUseCase
import com.example.amulet.shared.domain.courses.usecase.StartCourseUseCase
import com.example.amulet.shared.domain.courses.usecase.EnrollCourseUseCase
import com.example.amulet.shared.domain.practices.usecase.GetScheduledSessionsStreamUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

@OptIn(kotlin.time.ExperimentalTime::class)
@HiltViewModel
class CourseDetailsViewModel @Inject constructor(
    private val getCourseByIdUseCase: GetCourseByIdUseCase,
    private val getCourseItemsStreamUseCase: GetCourseItemsStreamUseCase,
    private val getCourseModulesStreamUseCase: GetCourseModulesStreamUseCase,
    private val getCourseProgressStreamUseCase: GetCourseProgressStreamUseCase,
    private val getScheduledSessionsStreamUseCase: GetScheduledSessionsStreamUseCase,
    private val startCourseUseCase: StartCourseUseCase,
    private val continueCourseUseCase: ContinueCourseUseCase,
    private val resetCourseProgressUseCase: ResetCourseProgressUseCase,
    private val completeCourseItemUseCase: CompleteCourseItemUseCase,
    private val checkItemUnlockUseCase: CheckItemUnlockUseCase,
    private val enrollCourseUseCase: EnrollCourseUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(CourseDetailsState())
    val uiState: StateFlow<CourseDetailsState> = _uiState.asStateFlow()

    fun setIdIfEmpty(id: String) {
        if (_uiState.value.courseId == null) {
            _uiState.update { it.copy(courseId = id) }
            observe()
        }
    }

    private fun observe() {
        val id = _uiState.value.courseId ?: return
        viewModelScope.launch {
            combine(
                getCourseByIdUseCase(id),
                getCourseItemsStreamUseCase(id),
                getCourseModulesStreamUseCase(id),
                getCourseProgressStreamUseCase(id),
                getScheduledSessionsStreamUseCase()
            ) { course, items, modules, progress, sessions ->
                val courseSessions = sessions.filter { it.courseId == id }
                
                // Auto-expand first module if none expanded and modules exist
                val currentExpanded = _uiState.value.expandedModuleIds
                val initialExpanded = if (currentExpanded.isEmpty() && modules.isNotEmpty()) {
                    setOf(modules.first().id.value)
                } else {
                    currentExpanded
                }
                
                // Проверяем статус разблокировки для каждого элемента
                val unlockedItemIds = items.mapNotNull { item ->
                    val isUnlocked = checkItemUnlockUseCase(id, item.id).component1() ?: true

                    if (isUnlocked) item.id else null
                }.toSet()
                
                // Определяем ближайшие сессии (следующие 2-3)
                val now = Clock.System.now().toEpochMilliseconds()
                val upcomingSessions = courseSessions
                    .filter { it.scheduledTime >= now }
                    .sortedBy { it.scheduledTime }
                    .take(3)
                
                _uiState.value.copy(
                    isLoading = false,
                    courseId = id,
                    course = course,
                    items = items,
                    modules = modules,
                    progress = progress,
                    scheduledSessions = courseSessions,
                    expandedModuleIds = initialExpanded,
                    unlockedItemIds = unlockedItemIds,
                    upcomingSessions = upcomingSessions
                )
            }.collect { state -> _uiState.update { state } }
        }
    }

    fun onEvent(event: CourseDetailsEvent) {
        when (event) {
            is CourseDetailsEvent.OnModuleClick -> toggleModule(event.moduleId)
            is CourseDetailsEvent.OnPracticeClick -> handlePracticeClick(event.practiceId)
            CourseDetailsEvent.OnStartCourse -> startCourse()
            CourseDetailsEvent.OnContinueCourse -> continueCourse()
            CourseDetailsEvent.OnResetCourse -> reset()
            CourseDetailsEvent.OnNavigateBack -> { /* Handled by UI */ }
            is CourseDetailsEvent.OnOpenEnrollmentWizard -> openEnrollmentWizard(event.mode)
            CourseDetailsEvent.OnDismissEnrollmentWizard -> dismissEnrollmentWizard()
            is CourseDetailsEvent.OnEnrollCourse -> enrollCourse(event.params)
            CourseDetailsEvent.OnOpenScheduleEdit -> { /* Handled by UI */ }
            CourseDetailsEvent.OnRestartCourse -> resetAndEnroll()
            CourseDetailsEvent.OnNextPracticeConsumed -> clearNextPractice()
        }
    }

    private fun toggleModule(moduleId: String) {
        _uiState.update { state ->
            val current = state.expandedModuleIds
            val newExpanded = if (current.contains(moduleId)) {
                current - moduleId
            } else {
                current + moduleId
            }
            state.copy(expandedModuleIds = newExpanded)
        }
    }

    private fun handlePracticeClick(practiceId: String) {
        // Logic for practice click handled by navigation in Route
    }

    private fun startCourse() {
        val id = _uiState.value.courseId ?: return
        viewModelScope.launch { startCourseUseCase(id) }
    }

    private fun continueCourse() {
        val id = _uiState.value.courseId ?: return
        viewModelScope.launch {
            // Продолжаем курс и получаем следующий элемент
            val result = continueCourseUseCase(id)
            val nextItemId = result.component1()
            if (nextItemId != null) {
                val items = _uiState.value.items
                val nextItem = items.firstOrNull { it.id == nextItemId }
                nextItem?.practiceId?.let { practiceId ->
                    _uiState.update { it.copy(nextPracticeId = practiceId) }
                }
            }
        }
    }

    private fun reset() {
        val id = _uiState.value.courseId ?: return
        viewModelScope.launch { resetCourseProgressUseCase(id) }
    }
    
    private fun openEnrollmentWizard(mode: CourseEnrollmentMode) {
        _uiState.update { it.copy(showEnrollmentWizard = true, enrollmentMode = mode) }
    }
    
    private fun dismissEnrollmentWizard() {
        _uiState.update { it.copy(showEnrollmentWizard = false, enrollmentMode = null) }
    }
    
    private fun enrollCourse(params: com.example.amulet.shared.domain.courses.model.EnrollmentParams) {
        viewModelScope.launch {
            _uiState.update { it.copy(enrollmentInProgress = true) }
            val result = enrollCourseUseCase(params)
            _uiState.update { 
                it.copy(
                    enrollmentInProgress = false,
                    showEnrollmentWizard = false
                ) 
            }
            // TODO: Show success toast/snackbar
        }
    }
    
    private fun resetAndEnroll() {
        val id = _uiState.value.courseId ?: return
        viewModelScope.launch {
            resetCourseProgressUseCase(id)
            _uiState.update { it.copy(showEnrollmentWizard = true, enrollmentMode = CourseEnrollmentMode.REPEAT) }
        }
    }

    private fun clearNextPractice() {
        _uiState.update { it.copy(nextPracticeId = null) }
    }
}
