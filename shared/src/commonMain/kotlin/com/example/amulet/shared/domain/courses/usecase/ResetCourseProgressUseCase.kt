package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.model.CourseId

class ResetCourseProgressUseCase(
    private val repository: CoursesRepository
) {
    suspend operator fun invoke(courseId: CourseId): AppResult<Unit> = repository.resetProgress(courseId)
}
