package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternElement
import com.example.amulet.shared.domain.patterns.model.PatternElementBreathing
import com.example.amulet.shared.domain.patterns.model.PatternElementChase
import com.example.amulet.shared.domain.patterns.model.PatternElementFill
import com.example.amulet.shared.domain.patterns.model.PatternElementProgress
import com.example.amulet.shared.domain.patterns.model.PatternElementPulse
import com.example.amulet.shared.domain.patterns.model.PatternElementSequence
import com.example.amulet.shared.domain.patterns.model.PatternElementSpinner
import com.example.amulet.shared.domain.patterns.model.PatternElementTimeline
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.model.SequenceStep

fun calculatePatternDuration(pattern: Pattern): Long {
    return pattern.spec.elements.fold(0L) { acc, element ->
        acc + element.durationContribution()
    }
}

private fun PatternElement.durationContribution(): Long {
    return when (this) {
        is PatternElementBreathing -> durationMs.toLong()
        is PatternElementChase -> speedMs.toLong() * 8L
        is PatternElementFill -> durationMs.toLong()
        is PatternElementPulse -> speed.toLong() * repeats.toLong()
        is PatternElementProgress -> 1000L
        is PatternElementSequence -> steps.fold(0L) { sAcc, step ->
            sAcc + when (step) {
                is SequenceStep.LedAction -> step.durationMs.toLong()
                is SequenceStep.DelayAction -> step.durationMs.toLong()
            }
        }
        is PatternElementSpinner -> speedMs.toLong() * 8L
        is PatternElementTimeline -> durationMs.toLong()
    }
}

@Composable
fun formatPatternDuration(durationMs: Long): String {
    return when {
        durationMs < 1000 -> stringResource(R.string.time_format_ms, durationMs.toInt())
        durationMs < 60_000 -> stringResource(R.string.time_format_seconds, durationMs / 1000f)
        else -> {
            val minutes = (durationMs / 60_000).toInt()
            stringResource(R.string.time_format_minutes, minutes)
        }
    }
}

fun PatternSpec.loopLabelRes(): Int {
    return if (loop) android.R.string.yes else android.R.string.no
}
