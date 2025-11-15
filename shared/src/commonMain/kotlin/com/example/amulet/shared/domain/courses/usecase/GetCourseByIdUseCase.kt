package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.courses.model.CourseId
import kotlinx.coroutines.flow.Flow

class GetCourseByIdUseCase(
    private val repository: CoursesRepository
) {
    operator fun invoke(id: CourseId): Flow<Course?> = repository.getCourseById(id)
}
