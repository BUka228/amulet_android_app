package com.example.amulet.feature.practices.presentation.session

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amulet.core.design.foundation.color.AmuletPalette
import com.example.amulet.core.design.foundation.theme.AmuletTheme
import com.example.amulet.feature.practices.R
import com.example.amulet.shared.domain.practices.model.PracticeAudioMode
import com.example.amulet.shared.domain.practices.model.PracticeSessionStatus
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

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

    // Градиентный фон из темы
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp,)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isCompleted) {

                Spacer(Modifier.height(20.dp))
                // Центральная визуализация с круговым прогрессом
                CenterVisualizationWithProgress(state = state)
                
                Spacer(Modifier.height(32.dp))

                // Timeline шагов
                if (state.practice?.script?.steps?.isNotEmpty() == true) {
                    StepsTimeline(state = state)
                    Spacer(Modifier.height(24.dp))
                }

                // Карточка статуса амулета
                AmuletStatusCard(state = state)
                
                Spacer(Modifier.height(16.dp))

                // Блок управления
                ControlsCard(state = state, onIntent = onIntent)
            } else {
                // Финальный экран
                FinalSessionBlock(
                    state = state,
                    onIntent = onIntent,
                    onPlanTomorrow = null,
                    onNavigateHome = onNavigateBack
                )
            }
        }
    }
}


@Composable
private fun ControlsCard(
    state: PracticeSessionState,
    onIntent: (PracticeSessionIntent) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Яркость
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LightMode,
                    contentDescription = null,
                    tint = AmuletPalette.Accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.practice_session_brightness),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = (state.brightnessLevel ?: 1.0).toFloat(),
                        onValueChange = { level ->
                            onIntent(PracticeSessionIntent.ChangeBrightness(level.toDouble()))
                        },
                        valueRange = 0.2f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = AmuletPalette.Accent,
                            activeTrackColor = AmuletPalette.Accent,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Аудио режим
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = AmuletPalette.InfoLight,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.practice_session_audio_mode),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    
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
                }
            }
        }
    }
}


@Composable
private fun CenterVisualizationWithProgress(state: PracticeSessionState) {
    val total = state.progress?.totalSec ?: 1
    val elapsed = state.progress?.elapsedSec ?: 0
    val progressFraction = if (total > 0) {
        (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val currentStepIndex = state.currentStepIndex ?: 0
    val steps = state.practice?.script?.steps.orEmpty()
    val currentStep = steps.getOrNull(currentStepIndex)
    
    val timeRemaining = total - elapsed

    Box(
        modifier = Modifier.size(320.dp),
        contentAlignment = Alignment.Center
    ) {
        // Круговой прогресс-бар
        CircularProgressIndicator(
            state = state,
            progress = progressFraction,
            modifier = Modifier.fillMaxSize()
        )

        // Визуализация дыхания
        BreathingVisualizer(
            currentStep = currentStep,
            timeRemainingSec = timeRemaining,
            modifier = Modifier.size(280.dp)
        )
    }
}

@Composable
private fun CircularProgressIndicator(
    state: PracticeSessionState,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "progress"
    )

    // Получаем цвета из темы перед Canvas
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Canvas(modifier = modifier) {
        val strokeWidth = 8.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2

        // Фоновое кольцо
        drawCircle(
            color = backgroundColor,
            radius = radius,
            style = Stroke(width = strokeWidth)
        )

        // Прогресс-кольцо с градиентом из темы
        val sweepAngle = 360f * animatedProgress
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    primaryColor,
                    secondaryColor,
                    AmuletPalette.InfoLight,
                    AmuletPalette.Primary
                )
            ),
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun StepsTimeline(state: PracticeSessionState) {
    val steps = state.practice?.script?.steps.orEmpty()
    val currentIndex = (state.currentStepIndex ?: 0).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.practice_session_steps_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))

            AnimatedContent(
                targetState = currentIndex.coerceAtMost(steps.lastIndex),
                transitionSpec = {
                    if (targetState > initialState) {
                        // Новый шаг выезжает снизу, старый уезжает вверх
                        slideInVertically { height -> height } + fadeIn() togetherWith
                                slideOutVertically { height -> -height } + fadeOut()
                    } else {
                        // При возврате назад — наоборот
                        slideInVertically { height -> -height } + fadeIn() togetherWith
                                slideOutVertically { height -> height } + fadeOut()
                    }
                },
                label = "stepsTimelineAnimation"
            ) { animatedIndex ->
                val step = steps.getOrNull(animatedIndex)
                if (step != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Индикатор статуса (дизайн сохранён, всегда текущий шаг)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            AmuletPalette.InfoLight
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${animatedIndex + 1}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        // Информация о шаге (как для текущего шага раньше)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = step.title ?: "Шаг ${animatedIndex + 1}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val desc = step.description
                            if (desc != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AmuletStatusCard(state: PracticeSessionState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Статус подключения
            StatusItem(
                icon = if (state.isDeviceOnline) Icons.Default.Check else Icons.Default.Error,
                label = if (state.isDeviceOnline) {
                    stringResource(id = R.string.practice_session_device_online)
                } else {
                    stringResource(id = R.string.practice_session_device_offline)
                },
                color = if (state.isDeviceOnline) AmuletTheme.colors.successLight else AmuletTheme.colors.errorLight
            )

            // Батарея
            StatusItem(
                icon = Icons.Default.BatteryChargingFull,
                label = "${state.batteryLevel ?: 0}%",
                color = when {
                    (state.batteryLevel ?: 0) > 50 -> AmuletTheme.colors.successLight
                    (state.batteryLevel ?: 0) > 20 -> AmuletTheme.colors.warningLight
                    else -> AmuletTheme.colors.errorLight
                }
            )

            // Паттерн
            StatusItem(
                icon = Icons.Default.Pattern,
                label = state.patternName ?: "—",
                color = AmuletPalette.InfoLight
            )
        }
    }
}

@Composable
private fun StatusItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
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
        PracticeAudioMode.GUIDE -> "🎙️ Гид"
        PracticeAudioMode.SOUND_ONLY -> "🔊 Звук"
        PracticeAudioMode.SILENT -> "🔇 Тихо"
    }

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { 
            Text(
                text = label,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ) 
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}



@Composable
private fun FinalSessionBlock(
    state: PracticeSessionState,
    onIntent: (PracticeSessionIntent) -> Unit,
    onPlanTomorrow: (() -> Unit)?,
    onNavigateHome: () -> Unit,
) {
    // Анимация конфетти
    ConfettiAnimation()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Иконка успеха
        val scale = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale.value)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AmuletTheme.colors.success,
                            AmuletTheme.colors.successDark
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.practice_session_completed_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(32.dp))

        // Статистика
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        icon = Icons.Default.Timer,
                        value = formatDuration(state.progress?.elapsedSec ?: state.totalDurationSec ?: 0),
                        label = stringResource(id = R.string.practice_session_stat_duration)
                    )
                    StatItem(
                        icon = Icons.Default.CheckCircle,
                        value = "${state.practice?.script?.steps?.size ?: 0}",
                        label = stringResource(id = R.string.practice_session_stat_steps)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Рейтинг с эмодзи
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.practice_session_rating_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))

                // Эмодзи рейтинг
                val currentRating = state.pendingRating ?: 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        1 to "😢",
                        2 to "😕",
                        3 to "😐",
                        4 to "😊",
                        5 to "😄"
                    ).forEach { (rating, emoji) ->
                        EmojiRatingButton(
                            emoji = emoji,
                            selected = currentRating == rating,
                            onClick = {
                                onIntent(PracticeSessionIntent.Rate(rating, state.pendingNote))
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Кнопки
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmuletTheme.colors.success
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.practice_session_back_home),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (onPlanTomorrow != null) {
                OutlinedButton(
                    onClick = { onPlanTomorrow() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.practice_session_plan_tomorrow),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfettiAnimation() {
    val confettiPieces = remember {
        List(30) {
            ConfettiPiece(
                x = Random.nextFloat(),
                initialY = -0.1f - Random.nextFloat() * 0.2f,
                speed = 0.3f + Random.nextFloat() * 0.4f,
                color = listOf(
                    AmuletPalette.Accent,
                    AmuletPalette.EmotionLove,
                    AmuletPalette.Secondary,
                    AmuletPalette.SecondaryLight,
                    AmuletPalette.ErrorLight,
                    AmuletPalette.Primary
                ).random()
            )
        }
    }

    confettiPieces.forEach { piece ->
        var yPosition by remember { mutableStateOf(piece.initialY) }

        LaunchedEffect(Unit) {
            while (true) {
                yPosition += piece.speed * 0.016f
                if (yPosition > 1.2f) {
                    yPosition = -0.1f
                }
                delay(16)
            }
        }

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val x = piece.x * size.width
            val y = yPosition * size.height

            drawCircle(
                color = piece.color,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

private data class ConfettiPiece(
    val x: Float,
    val initialY: Float,
    val speed: Float,
    val color: Color
)

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AmuletTheme.colors.successLight,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun EmojiRatingButton(
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "emojiScale"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (selected) Color.White.copy(alpha = 0.2f) else Color.Transparent
            )
            .clickableWithoutRipple { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 32.sp
        )
    }
}

private fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier = this.then(
    Modifier.pointerInput(Unit) {
        detectTapGestures { onClick() }
    }
)

@Composable
private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return if (minutes > 0) {
        stringResource(id = R.string.practice_session_duration_minutes_seconds, minutes, secs)
    } else {
        stringResource(id = R.string.practice_session_duration_seconds, secs)
    }
}
