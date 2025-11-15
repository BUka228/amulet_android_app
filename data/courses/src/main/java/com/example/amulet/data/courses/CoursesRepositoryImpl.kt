package com.example.amulet.data.courses

import com.example.amulet.data.courses.datasource.LocalCoursesDataSource
import com.example.amulet.data.courses.datasource.RemoteCoursesDataSource
import com.example.amulet.data.courses.mapper.toDomain
import com.example.amulet.data.courses.mapper.toJsonArrayString
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.auth.UserSessionContext
import com.example.amulet.shared.core.auth.UserSessionProvider
import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.courses.model.CourseId
import com.example.amulet.shared.domain.courses.model.CourseItem
import com.example.amulet.shared.domain.courses.model.CourseItemId
import com.example.amulet.shared.domain.courses.model.CourseProgress
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoursesRepositoryImpl @Inject constructor(
    private val local: LocalCoursesDataSource,
    private val remote: RemoteCoursesDataSource,
    private val sessionProvider: UserSessionProvider,
    private val json: Json
) : CoursesRepository {

    private val currentUserId: String
        get() = when (val c = sessionProvider.currentContext) {
            is UserSessionContext.LoggedIn -> c.userId.value
            else -> throw IllegalStateException("User not authenticated")
        }

    override fun getCoursesStream(): Flow<List<Course>> =
        local.observeCourses().map { it.map { e -> e.toDomain(json) } }

    override fun getCourseById(id: CourseId): Flow<Course?> =
        local.observeCourseById(id).map { it?.toDomain(json) }

    override fun getCourseItemsStream(courseId: CourseId): Flow<List<CourseItem>> =
        local.observeCourseItems(courseId).map { it.map { i -> i.toDomain() } }

    override fun getCourseProgressStream(courseId: CourseId): Flow<CourseProgress?> =
        local.observeCourseProgress(currentUserId, courseId).map { it?.toDomain(json) }

    override suspend fun refreshCatalog(): AppResult<Unit> = remote.refreshCatalog()

    override suspend fun startCourse(courseId: CourseId): AppResult<CourseProgress> {
        val items = local.observeCourseItems(courseId).first()
        val now = System.currentTimeMillis()
        val progress = com.example.amulet.core.database.entity.CourseProgressEntity(
            userId = currentUserId,
            courseId = courseId,
            completedItemIdsJson = emptyList<String>().toJsonArrayString(json),
            currentItemId = items.minByOrNull { it.order }?.id,
            percent = 0,
            totalTimeSec = 0,
            updatedAt = now
        )
        local.upsertProgress(progress)
        return Ok(progress.toDomain(json))
    }

    override suspend fun continueCourse(courseId: CourseId): AppResult<CourseItemId?> {
        val p = local.observeCourseProgress(currentUserId, courseId).first()
        return Ok(p?.currentItemId)
    }

    override suspend fun completeItem(courseId: CourseId, itemId: CourseItemId): AppResult<CourseProgress> {
        val progress = local.observeCourseProgress(currentUserId, courseId).first()
            ?: return Err(AppError.NotFound)
        val items = local.observeCourseItems(courseId).first().sortedBy { it.order }
        val completedSet = progress.toDomain(json).completedItemIds.toMutableSet()
        completedSet.add(itemId)
        val next = items.firstOrNull { it.id !in completedSet }?.id
        val percent = if (items.isEmpty()) 0 else (completedSet.size * 100 / items.size)
        val updated = progress.copy(
            completedItemIdsJson = completedSet.toList().toJsonArrayString(json),
            currentItemId = next,
            percent = percent,
            updatedAt = System.currentTimeMillis()
        )
        local.upsertProgress(updated)
        return Ok(updated.toDomain(json))
    }

    override suspend fun resetProgress(courseId: CourseId): AppResult<Unit> {
        local.resetProgress(currentUserId, courseId)
        return Ok(Unit)
    }
}
