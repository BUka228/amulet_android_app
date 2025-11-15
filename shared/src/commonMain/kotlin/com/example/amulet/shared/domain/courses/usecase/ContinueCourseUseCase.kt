package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.model.CourseId
import com.example.amulet.shared.domain.courses.model.CourseItemId

class ContinueCourseUseCase(
    private val repository: CoursesRepository
) {
    suspend operator fun invoke(courseId: CourseId): AppResult<CourseItemId?> = repository.continueCourse(courseId)
}
