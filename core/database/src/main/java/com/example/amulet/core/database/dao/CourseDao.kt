package com.example.amulet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.amulet.core.database.entity.CourseEntity
import com.example.amulet.core.database.entity.CourseItemEntity
import com.example.amulet.core.database.entity.CourseProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCourseItems(items: List<CourseItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: CourseProgressEntity)

    @Query("SELECT * FROM courses ORDER BY title ASC")
    fun observeCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun observeCourseById(courseId: String): Flow<CourseEntity?>

    @Query("SELECT * FROM course_items WHERE courseId = :courseId ORDER BY `order` ASC")
    fun observeCourseItems(courseId: String): Flow<List<CourseItemEntity>>

    @Query("SELECT * FROM course_progress WHERE userId = :userId AND courseId = :courseId")
    fun observeCourseProgress(userId: String, courseId: String): Flow<CourseProgressEntity?>

    @Query("SELECT * FROM course_progress WHERE userId = :userId")
    fun observeAllProgress(userId: String): Flow<List<CourseProgressEntity>>

    @Query("DELETE FROM course_progress WHERE userId = :userId AND courseId = :courseId")
    suspend fun resetProgress(userId: String, courseId: String)

    @Query(
        "SELECT DISTINCT c.* FROM courses c " +
            "INNER JOIN course_items ci ON c.id = ci.courseId " +
            "WHERE ci.practiceId = :practiceId ORDER BY c.title ASC"
    )
    fun observeCoursesByPracticeId(practiceId: String): Flow<List<CourseEntity>>
}
