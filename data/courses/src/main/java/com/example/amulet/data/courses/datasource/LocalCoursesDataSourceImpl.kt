package com.example.amulet.data.courses.datasource

import com.example.amulet.core.database.dao.CourseDao
import com.example.amulet.core.database.entity.CourseEntity
import com.example.amulet.core.database.entity.CourseItemEntity
import com.example.amulet.core.database.entity.CourseProgressEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalCoursesDataSourceImpl @Inject constructor(
    private val dao: CourseDao
) : LocalCoursesDataSource {
    override fun observeCourses(): Flow<List<CourseEntity>> = dao.observeCourses()
    override fun observeCourseById(courseId: String): Flow<CourseEntity?> = dao.observeCourseById(courseId)
    override fun observeCourseItems(courseId: String): Flow<List<CourseItemEntity>> = dao.observeCourseItems(courseId)
    override fun observeCourseProgress(userId: String, courseId: String): Flow<CourseProgressEntity?> = dao.observeCourseProgress(userId, courseId)
    override suspend fun upsertCourses(items: List<CourseEntity>) { dao.upsertCourses(items) }
    override suspend fun upsertCourseItems(items: List<CourseItemEntity>) { dao.upsertCourseItems(items) }
    override suspend fun upsertProgress(entity: CourseProgressEntity) { dao.upsertProgress(entity) }
    override suspend fun resetProgress(userId: String, courseId: String) { dao.resetProgress(userId, courseId) }
}
