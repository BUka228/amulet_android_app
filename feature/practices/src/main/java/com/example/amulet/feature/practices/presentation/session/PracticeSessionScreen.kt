package com.example.amulet.feature.practices.presentation.session

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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

        // Центр: простая визуализация
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            )
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

        // Аудио (заглушка)
        Text("Аудио режим (заглушка)")

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
