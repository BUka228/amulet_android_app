package com.example.amulet.shared.domain.practices.model

/**
 * Источник запуска сессии практики.
 * Хранится в БД как строка (см. toStorageString/parsePracticeSessionSource).
 */
sealed class PracticeSessionSource {
    /** Ручной старт из деталей/дома практик. */
    data object Manual : PracticeSessionSource()

    /** Сессия, начатая в рамках курса (courseId + itemId). */
    data class FromCourse(val courseId: String, val itemId: String) : PracticeSessionSource()

    /** Сессия, начатая из расписания (конкретный ScheduledSession.id). */
    data class FromSchedule(val scheduledId: String) : PracticeSessionSource()

    /** Искусственно созданная запись для пропущенной сессии расписания. */
    data class ScheduleSkip(val scheduledId: String) : PracticeSessionSource()

    /** Неизвестный/расширяемый формат (для обратной совместимости). */
    data class Unknown(val raw: String) : PracticeSessionSource()
}

private const val PREFIX_MANUAL = "MANUAL"
private const val PREFIX_COURSE = "COURSE:"
private const val PREFIX_SCHEDULE = "SCHEDULE:"
private const val PREFIX_SCHEDULE_SKIP = "SCHEDULE_SKIP:"

fun PracticeSessionSource.toStorageString(): String = when (this) {
    is PracticeSessionSource.Manual -> PREFIX_MANUAL
    is PracticeSessionSource.FromCourse -> "$PREFIX_COURSE${courseId}:$itemId"
    is PracticeSessionSource.FromSchedule -> PREFIX_SCHEDULE + scheduledId
    is PracticeSessionSource.ScheduleSkip -> PREFIX_SCHEDULE_SKIP + scheduledId
    is PracticeSessionSource.Unknown -> raw
}

fun parsePracticeSessionSource(raw: String?): PracticeSessionSource? {
    if (raw == null) return null
    return when {
        raw == PREFIX_MANUAL -> PracticeSessionSource.Manual
        raw.startsWith(PREFIX_COURSE) -> {
            // Формат: COURSE:courseId:itemId
            val payload = raw.removePrefix(PREFIX_COURSE)
            val parts = payload.split(":", limit = 2)
            if (parts.size == 2) {
                PracticeSessionSource.FromCourse(courseId = parts[0], itemId = parts[1])
            } else {
                PracticeSessionSource.Unknown(raw)
            }
        }
        raw.startsWith(PREFIX_SCHEDULE) ->
            PracticeSessionSource.FromSchedule(raw.removePrefix(PREFIX_SCHEDULE))
        raw.startsWith(PREFIX_SCHEDULE_SKIP) ->
            PracticeSessionSource.ScheduleSkip(raw.removePrefix(PREFIX_SCHEDULE_SKIP))
        else -> PracticeSessionSource.Unknown(raw)
    }
}
