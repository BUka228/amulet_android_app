package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.practices.model.PracticeFilter
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.first

class SearchCoursesUseCase(
    private val repository: CoursesRepository
) {
    suspend operator fun invoke(
        query: String,
        filter: PracticeFilter
    ): AppResult<List<Course>> {
        // Get all courses and filter in memory
        val allCourses = repository.getCoursesStream().first()
        
        val filtered = allCourses.filter { course ->
            val matchesQuery = if (query.isBlank()) true else {
                course.title.contains(query, ignoreCase = true) ||
                (course.description?.contains(query, ignoreCase = true) == true)
            }
            
            val matchesGoal = filter.goal?.let { course.goal == it } ?: true
            val matchesLevel = filter.level?.let { course.level == it } ?: true
            
            // Duration filter logic (if applicable to courses)
            val matchesDuration = if (filter.durationFromSec != null || filter.durationToSec != null) {
                val duration = course.totalDurationSec ?: 0
                val from = filter.durationFromSec ?: 0
                val to = filter.durationToSec ?: Int.MAX_VALUE
                duration in from..to
            } else {
                true
            }

            matchesQuery && matchesGoal && matchesLevel && matchesDuration
        }
        
        return Ok(filtered)
    }
}
