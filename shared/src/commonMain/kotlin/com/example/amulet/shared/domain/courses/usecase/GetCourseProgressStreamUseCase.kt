package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.model.CourseId
import com.example.amulet.shared.domain.courses.model.CourseProgress
import kotlinx.coroutines.flow.Flow

class GetCourseProgressStreamUseCase(
    private val repository: CoursesRepository
) {
    operator fun invoke(courseId: CourseId): Flow<CourseProgress?> = repository.getCourseProgressStream(courseId)
}
