package com.example.amulet.shared.domain.courses

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.courses.model.CourseId
import com.example.amulet.shared.domain.courses.model.CourseItem
import com.example.amulet.shared.domain.courses.model.CourseItemId
import com.example.amulet.shared.domain.courses.model.CourseModule
import com.example.amulet.shared.domain.courses.model.CourseProgress
import com.example.amulet.shared.domain.practices.model.PracticeId
import kotlinx.coroutines.flow.Flow

interface CoursesRepository {
    fun getCoursesStream(): Flow<List<Course>>

    fun getCourseById(id: CourseId): Flow<Course?>

    fun getCourseItemsStream(courseId: CourseId): Flow<List<CourseItem>>

    fun getCourseModulesStream(courseId: CourseId): Flow<List<CourseModule>>

    fun getCourseProgressStream(courseId: CourseId): Flow<CourseProgress?>

    fun getAllCoursesProgressStream(): Flow<List<CourseProgress>>

    fun getCoursesByPracticeId(practiceId: PracticeId): Flow<List<Course>>

    suspend fun refreshCatalog(): AppResult<Unit>

    suspend fun seedLocalData(): AppResult<Unit>

    suspend fun startCourse(courseId: CourseId): AppResult<CourseProgress>

    suspend fun continueCourse(courseId: CourseId): AppResult<CourseItemId?>

    suspend fun completeItem(courseId: CourseId, itemId: CourseItemId): AppResult<CourseProgress>

    suspend fun resetProgress(courseId: CourseId): AppResult<Unit>
}
