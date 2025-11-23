package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.model.CourseId
import com.example.amulet.shared.domain.courses.model.CourseModule
import kotlinx.coroutines.flow.Flow

class GetCourseModulesStreamUseCase(
    private val repository: CoursesRepository
) {
    operator fun invoke(courseId: CourseId): Flow<List<CourseModule>> =
        repository.getCourseModulesStream(courseId)
}
