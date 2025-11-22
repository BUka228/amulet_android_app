package com.example.amulet.data.courses.datasource

import com.example.amulet.core.database.entity.CourseEntity
import com.example.amulet.core.database.entity.CourseItemEntity
import com.example.amulet.core.database.entity.CourseModuleEntity
import com.example.amulet.core.database.entity.CourseProgressEntity
import com.example.amulet.data.courses.seed.CourseSeed
import kotlinx.coroutines.flow.Flow

interface LocalCoursesDataSource {
    fun observeCourses(): Flow<List<CourseEntity>>
    fun observeCourseById(courseId: String): Flow<CourseEntity?>
    fun observeCourseItems(courseId: String): Flow<List<CourseItemEntity>>
    fun observeCourseModules(courseId: String): Flow<List<CourseModuleEntity>>
    fun observeCourseProgress(userId: String, courseId: String): Flow<CourseProgressEntity?>
    fun observeAllProgress(userId: String): Flow<List<CourseProgressEntity>>
    fun observeCoursesByPracticeId(practiceId: String): Flow<List<CourseEntity>>
    suspend fun upsertCourses(items: List<CourseEntity>)
    suspend fun upsertCourseItems(items: List<CourseItemEntity>)
    suspend fun upsertCourseModules(items: List<CourseModuleEntity>)
    suspend fun upsertProgress(entity: CourseProgressEntity)
    suspend fun resetProgress(userId: String, courseId: String)
    suspend fun seedPresets(presets: List<CourseSeed>)
}
