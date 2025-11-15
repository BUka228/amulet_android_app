package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.model.CourseId
import com.example.amulet.shared.domain.courses.model.CourseItem
import kotlinx.coroutines.flow.Flow

class GetCourseItemsStreamUseCase(
    private val repository: CoursesRepository
) {
    operator fun invoke(courseId: CourseId): Flow<List<CourseItem>> = repository.getCourseItemsStream(courseId)
}
