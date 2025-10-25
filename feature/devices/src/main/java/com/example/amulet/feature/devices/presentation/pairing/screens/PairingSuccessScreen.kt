package com.example.amulet.feature.devices.presentation.pairing.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.feature.devices.R
import com.example.amulet.feature.devices.presentation.pairing.PairingViewModel
import kotlinx.coroutines.delay

@Composable
fun PairingSuccessScreen(
    viewModel: PairingViewModel = hiltViewModel(),
    onComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showConfetti = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Эффект конфетти (упрощенный)
        if (showConfetti) {
            ConfettiEffect()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(40.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Анимированная иконка успеха
                SuccessIcon()

                Text(
                    text = stringResource(R.string.pairing_success_title),
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(R.string.pairing_success_message),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Карточка с информацией об устройстве
                state.pairedDevice?.let { device ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = device.name ?: device.serialNumber,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            if (device.name != null) {
                                Text(
                                    text = device.serialNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.pairing_success_button),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun SuccessIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "success_icon")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        modifier = Modifier.size(120.dp * scale),
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ConfettiEffect() {
    val confettiPieces = remember {
        List(30) {
            ConfettiPiece(
                x = (0..100).random() / 100f,
                startY = -(0..50).random() / 100f,
                color = listOf(
                    Color(0xFF6750A4),
                    Color(0xFFE91E63),
                    Color(0xFF00BCD4),
                    Color(0xFF4CAF50),
                    Color(0xFFFF9800)
                ).random()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        confettiPieces.forEach { piece ->
            val y = size.height * (piece.startY + animationProgress * 1.2f)
            val x = size.width * piece.x + kotlin.math.sin(animationProgress * 5f) * 30f
            
            if (y > 0 && y < size.height) {
                drawCircle(
                    color = piece.color,
                    radius = 6.dp.toPx(),
                    center = Offset(x, y),
                    alpha = 1f - animationProgress * 0.5f
                )
            }
        }
    }
}

private data class ConfettiPiece(
    val x: Float,
    val startY: Float,
    val color: Color
)
