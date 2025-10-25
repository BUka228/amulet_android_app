package com.example.amulet.feature.devices.presentation.pairing.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.feature.devices.R
import com.example.amulet.feature.devices.presentation.pairing.PairingViewModel
import kotlinx.coroutines.delay

@Composable
fun PairingProgressScreen(
    viewModel: PairingViewModel = hiltViewModel(),
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Автоматический переход при успехе
    LaunchedEffect(state.pairedDevice) {
        if (state.pairedDevice != null) {
            delay(1000) // Показываем успех 1 секунду
            onSuccess()
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            delay(2000)
            onError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Этапы паринга
        PairingSteps(progress = state.pairingProgress)

        Spacer(Modifier.height(48.dp))

        // Анимированное сообщение
        AnimatedContent(
            targetState = state.pairingProgress,
            label = "progress_message",
            transitionSpec = {
                (slideInVertically { it } + fadeIn()) togetherWith
                        (slideOutVertically { -it } + fadeOut())
            }
        ) { progress ->
            Text(
                text = progress ?: stringResource(R.string.pairing_connecting_ble),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(16.dp))

        // Прогресс-индикатор
        if (state.pairedDevice == null && state.error == null) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(6.dp),
            )
        }
    }
}

@Composable
private fun PairingSteps(progress: String?) {
    val steps = listOf(
        Triple(Icons.Default.Bluetooth, stringResource(R.string.pairing_step_connect), 0),
        Triple(Icons.Default.Cloud, stringResource(R.string.pairing_step_claim), 1),
        Triple(Icons.Default.Settings, stringResource(R.string.pairing_step_configure), 2)
    )

    // Определяем текущий шаг по прогрессу
    val currentStep = when {
        progress?.contains("Bluetooth", ignoreCase = true) == true -> 0
        progress?.contains("аккаунт", ignoreCase = true) == true -> 1
        progress?.contains("Настройка", ignoreCase = true) == true -> 2
        else -> 0
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (icon, label, _) ->
            val isActive = index <= currentStep
            val isCompleted = index < currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Иконка с анимацией
                StepIcon(
                    icon = if (isCompleted) Icons.Default.CheckCircle else icon,
                    isActive = isActive,
                    isCompleted = isCompleted
                )

                // Метка
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.alpha(if (isActive) 1f else 0.5f)
                )
            }

            // Линия между шагами
            if (index < steps.size - 1) {
                Divider(
                    modifier = Modifier
                        .width(40.dp)
                        .padding(bottom = 40.dp),
                    thickness = 2.dp,
                    color = if (index < currentStep) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun StepIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    isCompleted: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "step_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = if (isActive && !isCompleted) 0.95f else 1f,
        targetValue = if (isActive && !isCompleted) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        shape = MaterialTheme.shapes.large,
        color = when {
            isCompleted -> MaterialTheme.colorScheme.primaryContainer
            isActive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier.size(64.dp * if (isActive && !isCompleted) scale else 1f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = when {
                    isCompleted -> MaterialTheme.colorScheme.primary
                    isActive -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
