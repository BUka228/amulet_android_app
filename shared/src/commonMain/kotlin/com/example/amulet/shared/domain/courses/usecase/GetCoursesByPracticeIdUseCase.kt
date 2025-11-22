package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.practices.model.PracticeId
import kotlinx.coroutines.flow.Flow

class GetCoursesByPracticeIdUseCase(
    private val repository: CoursesRepository
) {
    operator fun invoke(practiceId: PracticeId): Flow<List<Course>> =
        repository.getCoursesByPracticeId(practiceId)
}
