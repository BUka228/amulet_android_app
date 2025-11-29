package com.example.amulet.feature.practices.presentation.mood

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.StringRes
import com.example.amulet.feature.practices.R
import com.example.amulet.shared.domain.practices.model.MoodKind

@Composable
fun moodIcon(mood: MoodKind): ImageVector = when (mood) {
    MoodKind.NERVOUS -> Icons.Filled.MoodBad
    MoodKind.SLEEP -> Icons.Filled.Bedtime
    MoodKind.FOCUS -> Icons.Filled.Bolt
    MoodKind.RELAX -> Icons.Filled.Spa
    MoodKind.NEUTRAL -> Icons.Filled.SentimentNeutral
    MoodKind.HAPPY -> Icons.Filled.SentimentVerySatisfied
    MoodKind.SAD -> Icons.Filled.SentimentDissatisfied
    MoodKind.ANGRY -> Icons.Filled.SentimentVeryDissatisfied
    MoodKind.TIRED -> Icons.Filled.Mood
}

@Composable
fun moodColor(mood: MoodKind): Color {
    val colors = MaterialTheme.colorScheme
    return when (mood) {
        MoodKind.NERVOUS -> colors.error
        MoodKind.SLEEP -> colors.primary
        MoodKind.FOCUS -> colors.tertiary
        MoodKind.RELAX -> colors.secondary
        MoodKind.NEUTRAL -> colors.outline
        MoodKind.HAPPY -> colors.primaryContainer
        MoodKind.SAD -> colors.errorContainer
        MoodKind.ANGRY -> colors.inversePrimary
        MoodKind.TIRED -> colors.tertiaryContainer
    }
}

@StringRes
fun moodDescriptionRes(mood: MoodKind): Int = when (mood) {
    MoodKind.NERVOUS -> R.string.practices_home_mood_nervous_description
    MoodKind.SLEEP -> R.string.practices_home_mood_sleep_description
    MoodKind.FOCUS -> R.string.practices_home_mood_focus_description
    MoodKind.RELAX -> R.string.practices_home_mood_relax_description
    MoodKind.NEUTRAL -> R.string.practices_home_mood_neutral_description
    MoodKind.HAPPY -> R.string.practices_home_mood_happy_description
    MoodKind.SAD -> R.string.practices_home_mood_sad_description
    MoodKind.ANGRY -> R.string.practices_home_mood_angry_description
    MoodKind.TIRED -> R.string.practices_home_mood_tired_description
}
