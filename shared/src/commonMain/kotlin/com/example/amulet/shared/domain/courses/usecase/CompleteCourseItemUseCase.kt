package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.model.CourseId
import com.example.amulet.shared.domain.courses.model.CourseItemId
import com.example.amulet.shared.domain.courses.model.CourseProgress

class CompleteCourseItemUseCase(
    private val repository: CoursesRepository
) {
    suspend operator fun invoke(
        courseId: CourseId,
        itemId: CourseItemId
    ): AppResult<CourseProgress> = repository.completeItem(courseId, itemId)
}
