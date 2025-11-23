package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.model.CourseId
import com.example.amulet.shared.domain.courses.model.EnrollmentParams
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.first

/**
 * Use case для записи на курс
 * 
 * Создаёт CourseProgress (если отсутствует), генерирует PracticeSchedule
 * на основе EnrollmentParams и сохраняет их в репозиторий
 */
class EnrollCourseUseCase(
    private val coursesRepository: CoursesRepository,
    private val practicesRepository: PracticesRepository,
    private val createScheduleForCourseUseCase: CreateScheduleForCourseUseCase
) {
    /**
     * @param params Параметры записи на курс
     * @return Результат с количеством созданных расписаний
     */
    suspend operator fun invoke(params: EnrollmentParams): AppResult<Int> {
        return try {
            val courseId: CourseId = params.courseId
            
            // Получаем информацию о курсе
            val course = coursesRepository.getCourseById(courseId).first()
                ?: return Err(AppError.NotFound)
            
            val courseItems = coursesRepository.getCourseItemsStream(courseId).first()
            
            // Стартуем курс, если ещё не начат (создаёт CourseProgress)
            val progress = coursesRepository.getCourseProgressStream(courseId).first()
            if (progress == null || progress.percent == 0) {
                coursesRepository.startCourse(courseId)
            }
            
            // Генерируем расписания для практик курса
            val schedules = createScheduleForCourseUseCase(
                params = params,
                courseItems = courseItems
            )
            
            // Сохраняем расписания
            schedules.forEach { schedule ->
                practicesRepository.upsertSchedule(schedule)
            }
            
            Ok(schedules.size)
        } catch (e: Exception) {
            Err(AppError.Unknown)
        }
    }
}
