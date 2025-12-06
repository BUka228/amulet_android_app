package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternSpec

fun calculatePatternDuration(pattern: Pattern): Long {
    return pattern.spec.timeline.durationMs.toLong()
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
