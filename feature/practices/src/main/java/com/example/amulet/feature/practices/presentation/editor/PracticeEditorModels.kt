package com.example.amulet.feature.practices.presentation.editor

import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.practices.model.PracticeStepType
import com.example.amulet.shared.domain.practices.model.PracticeType

/**
 * Редакторские модели для конструктора пользовательской практики.
 */

data class EditorPractice(
    val id: String? = null,
    val title: String = "",
    val type: PracticeType = PracticeType.BREATH,
    val targetDurationSec: Int? = null,
    val basePatternId: PatternId? = null,
    val about: String = "",
    val howItGoes: String = "",
    val safetyNotes: String = "",
    val steps: List<EditorStep> = emptyList(),
)

data class EditorStep(
    val order: Int,
    val type: PracticeStepType,
    val title: String = "",
    val description: String = "",
    val durationSec: Int? = null,
    val binding: StepBinding = StepBinding.None,
)

sealed interface StepBinding {
    /** Чисто текстовый шаг без привязки к паттерну. */
    data object None : StepBinding

    /** Один паттерн, опционально повторённый несколько раз подряд. */
    data class SinglePattern(
        val patternId: PatternId,
        val repeatCount: Int = 1,
    ) : StepBinding

    /** Группа сегментов одного базового паттерна, заданных по индексам сегментов. */
    data class SegmentGroup(
        val parentPatternId: PatternId,
        val segmentIndices: List<Int>,
        val repeatCount: Int = 1,
    ) : StepBinding
}
