package com.example.amulet.shared.domain.practices.model

sealed class PracticeSessionSource {
    data object Manual : PracticeSessionSource()
    data class ScheduleSkip(val scheduledId: String) : PracticeSessionSource()
    data class Unknown(val raw: String) : PracticeSessionSource()
}

private const val PREFIX_SCHEDULE_SKIP = "SCHEDULE_SKIP:"

fun PracticeSessionSource.toStorageString(): String = when (this) {
    is PracticeSessionSource.Manual -> "MANUAL"
    is PracticeSessionSource.ScheduleSkip -> PREFIX_SCHEDULE_SKIP + scheduledId
    is PracticeSessionSource.Unknown -> raw
}

fun parsePracticeSessionSource(raw: String?): PracticeSessionSource? {
    if (raw == null) return null
    return when {
        raw == "MANUAL" -> PracticeSessionSource.Manual
        raw.startsWith(PREFIX_SCHEDULE_SKIP) ->
            PracticeSessionSource.ScheduleSkip(raw.removePrefix(PREFIX_SCHEDULE_SKIP))
        else -> PracticeSessionSource.Unknown(raw)
    }
}
