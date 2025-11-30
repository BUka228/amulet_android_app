package com.example.amulet.shared.domain.hugs.model

import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.user.model.UserId

/**
 * Команда, описывающая входящее «удалённое объятие»,
 * которое нужно воспроизвести на амулете.
 */
data class RemoteHugCommand(
    val hugId: HugId?,
    val pairId: PairId?,
    val fromUserId: UserId,
    val toUserId: UserId?,
    val emotion: Emotion?,
    /**
     * Явный patternId из бэкенда.
     * Если не задан, используется emotion.patternId.
     */
    val patternIdOverride: PatternId? = null,
    /**
     * Дополнительный payload (например, для аналитики/отладки).
     */
    val payload: Map<String, Any?>? = null,
)
