package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.model.Course
import kotlinx.coroutines.flow.Flow

class GetCoursesStreamUseCase(
    private val repository: CoursesRepository
) {
    operator fun invoke(): Flow<List<Course>> = repository.getCoursesStream()
}
