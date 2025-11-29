package com.example.amulet.shared.domain.practices.model

import com.example.amulet.shared.domain.user.model.UserId

/**
 * Доменная запись о настроении пользователя.
 * Может использоваться как для чек-инов с Home, так и перед/после практик.
 */
data class MoodEntry(
    val id: String,
    val userId: UserId,
    val mood: MoodKind,
    val source: MoodSource,
    val createdAt: Long,
)

/**
 * Тип (категория) настроения, не зависящий от UI-модели.
 */
enum class MoodKind {
    NERVOUS,
    SLEEP,
    FOCUS,
    RELAX,
    NEUTRAL,
    HAPPY,
    SAD,
    ANGRY,
    TIRED,
}

/**
 * Источник, откуда пришло событие о настроении.
 */
enum class MoodSource {
    /** Выбор настроения на экране Practices Home. */
    PRACTICES_HOME,

    /** Оценка настроения до практики. */
    PRACTICE_BEFORE,

    /** Оценка настроения после практики. */
    PRACTICE_AFTER,
}
