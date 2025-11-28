package com.example.amulet.feature.practices.presentation.session

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.amulet.shared.domain.practices.model.PracticeAudioMode
import com.example.amulet.shared.domain.practices.model.PracticeSessionStatus

@Composable
fun PracticeSessionScreen(
    state: PracticeSessionState,
    onIntent: (PracticeSessionIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val isCompleted = state.session?.status == PracticeSessionStatus.COMPLETED

    BackHandler {
        onIntent(PracticeSessionIntent.Stop(completed = false))
        onNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Верх: прогресс
        Column(modifier = Modifier.fillMaxWidth()) {
            val total = state.progress?.totalSec ?: 0
            val progressFraction = if (total > 0) {
                state.progress!!.elapsedSec.toFloat() / total.toFloat()
            } else 0f

            LinearProgressIndicator(
                progress = progressFraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(text = state.goal ?: "")
        }

        CenterVisualization(state = state)

        if (!isCompleted && state.practice?.script?.steps?.isNotEmpty() == true) {
            StepsBlock(state = state)
            Spacer(Modifier.height(16.dp))
        }

        if (isCompleted) {
            FinalSessionBlock(state = state, onIntent = onIntent, onPlanTomorrow = null, onNavigateHome = onNavigateBack)
        } else {
            ControlsBlock(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun ControlsBlock(
    state: PracticeSessionState,
    onIntent: (PracticeSessionIntent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Яркость (простая версия)
        Text("Яркость")
        Slider(
            value = (state.brightnessLevel ?: 1.0).toFloat(),
            onValueChange = { level ->
                onIntent(PracticeSessionIntent.ChangeBrightness(level.toDouble()))
            },
            valueRange = 0.2f..1.0f,
            steps = 2,
        )

        Spacer(Modifier.height(16.dp))

        // Аудио режим
        Text("Аудио режим")
        val currentMode = state.audioMode ?: PracticeAudioMode.GUIDE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AudioModeChip(
                mode = PracticeAudioMode.GUIDE,
                selected = currentMode == PracticeAudioMode.GUIDE,
                onClick = { onIntent(PracticeSessionIntent.ChangeAudioMode(PracticeAudioMode.GUIDE)) }
            )
            AudioModeChip(
                mode = PracticeAudioMode.SOUND_ONLY,
                selected = currentMode == PracticeAudioMode.SOUND_ONLY,
                onClick = { onIntent(PracticeSessionIntent.ChangeAudioMode(PracticeAudioMode.SOUND_ONLY)) }
            )
            AudioModeChip(
                mode = PracticeAudioMode.SILENT,
                selected = currentMode == PracticeAudioMode.SILENT,
                onClick = { onIntent(PracticeSessionIntent.ChangeAudioMode(PracticeAudioMode.SILENT)) }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Статус амулета
        Text(
            text = "Амулет: " +
                when {
                    !state.isDeviceOnline -> "не в сети"
                    else -> "подключен"
                }
        )
        Text(text = "Батарея: " + (state.batteryLevel?.let { "$it%" } ?: "—"))
        Text(text = "Паттерн: " + (state.patternName ?: "—"))
    }
}

@Composable
private fun CenterVisualization(state: PracticeSessionState) {
    val total = state.progress?.totalSec ?: 0
    val rawProgress = if (total > 0) {
        state.progress!!.elapsedSec.toFloat() / total.toFloat()
    } else 0f

    val stepIndex = (state.currentStepIndex ?: 0).coerceAtLeast(0)

    // Немного "дышащая" анимация: размер зависит от прогресса и шага.
    val targetScale = 0.9f + 0.1f * ((stepIndex % 3))
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 600),
        label = "centerScale",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        val baseSize = 200.dp
        Box(
            modifier = Modifier
                .size(baseSize * animatedScale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f + 0.3f * rawProgress))
        )
    }
}

@Composable
private fun AudioModeChip(
    mode: PracticeAudioMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val label = when (mode) {
        PracticeAudioMode.GUIDE -> "Гид"
        PracticeAudioMode.SOUND_ONLY -> "Только звук"
        PracticeAudioMode.SILENT -> "Без звука"
    }

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun StepsBlock(
    state: PracticeSessionState,
) {
    val steps = state.practice?.script?.steps.orEmpty()
    val currentIndex = (state.currentStepIndex ?: 0).coerceAtLeast(0)

    // Показываем только уже "дошедшие" шаги: от 0 до currentIndex включительно.
    val visibleSteps = if (steps.isEmpty()) emptyList() else steps.take(currentIndex + 1)

    Column(modifier = Modifier.fillMaxWidth()) {
        visibleSteps.forEachIndexed { index, step ->
            val isCurrent = index == visibleSteps.lastIndex

            Text(
                text = step.title ?: "Шаг ${index + 1}",
                style = if (isCurrent) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                color = if (isCurrent) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            val description = step.description
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FinalSessionBlock(
    state: PracticeSessionState,
    onIntent: (PracticeSessionIntent) -> Unit,
    onPlanTomorrow: (() -> Unit)?,
    onNavigateHome: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Сессия завершена")
        Spacer(Modifier.height(8.dp))
        Text("Длительность: " + (state.progress?.elapsedSec ?: state.totalDurationSec ?: 0) + " сек")

        Spacer(Modifier.height(16.dp))
        Text("Как вы себя чувствуете?")
        Slider(
            value = (state.pendingRating ?: 3).toFloat(),
            onValueChange = { rating ->
                onIntent(PracticeSessionIntent.Rate(rating.toInt(), state.pendingNote))
            },
            valueRange = 1f..5f,
            steps = 3,
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { onPlanTomorrow?.invoke() }) {
                Text("Запланировать на завтра")
            }
            Button(onClick = onNavigateHome) {
                Text("Вернуться домой")
            }
        }
    }
}
