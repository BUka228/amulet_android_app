package com.example.amulet.feature.practices.presentation.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.courses.model.CourseItem
import com.example.amulet.shared.domain.courses.model.CourseProgress
import com.example.amulet.shared.domain.courses.usecase.CompleteCourseItemUseCase
import com.example.amulet.shared.domain.courses.usecase.ContinueCourseUseCase
import com.example.amulet.shared.domain.courses.usecase.GetCourseByIdUseCase
import com.example.amulet.shared.domain.courses.usecase.GetCourseItemsStreamUseCase
import com.example.amulet.shared.domain.courses.usecase.GetCourseProgressStreamUseCase
import com.example.amulet.shared.domain.courses.usecase.ResetCourseProgressUseCase
import com.example.amulet.shared.domain.courses.usecase.StartCourseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun CourseDetailsRoute(
    courseId: String,
    onNavigateBack: () -> Unit,
    viewModel: CourseDetailsViewModel = hiltViewModel()
) {
    viewModel.setIdIfEmpty(courseId)
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(state.course?.title ?: "Курс", style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val progress = state.progress
                if (progress == null || progress.percent == 0) {
                    Button(onClick = { viewModel.startCourse() }) { Text("Старт") }
                } else {
                    Button(onClick = { viewModel.continueCourse() }) { Text("Продолжить") }
                }
                Button(onClick = onNavigateBack) { Text("Назад") }
            }
        }
        state.error?.let { Text("Ошибка: $it", color = MaterialTheme.colorScheme.error) }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.items) { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.title ?: "")
                    val completed = state.progress?.completedItemIds?.contains(item.id) == true
                    if (!completed) {
                        Button(onClick = { item.id?.let { viewModel.completeItem(it) } }) { Text("Готово") }
                    } else {
                        Text("✓")
                    }
                }
            }
        }
    }
}

data class CourseDetailsState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val courseId: String? = null,
    val course: Course? = null,
    val items: List<CourseItem> = emptyList(),
    val progress: CourseProgress? = null
)

@HiltViewModel
class CourseDetailsViewModel @Inject constructor(
    private val getCourseByIdUseCase: GetCourseByIdUseCase,
    private val getCourseItemsStreamUseCase: GetCourseItemsStreamUseCase,
    private val getCourseProgressStreamUseCase: GetCourseProgressStreamUseCase,
    private val startCourseUseCase: StartCourseUseCase,
    private val continueCourseUseCase: ContinueCourseUseCase,
    private val completeCourseItemUseCase: CompleteCourseItemUseCase,
    private val resetCourseProgressUseCase: ResetCourseProgressUseCase,
    private val savedStateHandle: SavedStateHandle
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
                getCourseProgressStreamUseCase(id)
            ) { course, items, progress ->
                CourseDetailsState(
                    isLoading = false,
                    courseId = id,
                    course = course,
                    items = items,
                    progress = progress
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    fun startCourse() {
        val id = _uiState.value.courseId ?: return
        viewModelScope.launch { startCourseUseCase(id) }
    }

    fun continueCourse() {
        val id = _uiState.value.courseId ?: return
        viewModelScope.launch { continueCourseUseCase(id) }
    }

    fun completeItem(itemId: String) {
        val id = _uiState.value.courseId ?: return
        viewModelScope.launch { completeCourseItemUseCase(id, itemId) }
    }

    fun reset() {
        val id = _uiState.value.courseId ?: return
        viewModelScope.launch { resetCourseProgressUseCase(id) }
    }
}
