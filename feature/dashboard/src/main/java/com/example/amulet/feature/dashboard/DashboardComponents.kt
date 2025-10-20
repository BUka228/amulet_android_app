package com.example.amulet.feature.dashboard

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.amulet.core.design.components.button.AmuletButton
import com.example.amulet.core.design.components.button.ButtonSize
import com.example.amulet.core.design.components.button.ButtonVariant
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.components.card.CardElevation
import com.example.amulet.core.design.foundation.color.AmuletPalette
import com.example.amulet.core.design.foundation.theme.AmuletTheme
import com.example.amulet.feature.dashboard.presentation.DailyStats
import com.example.amulet.feature.dashboard.presentation.DeviceStatus
import kotlin.math.sin

// ===== Статус Амулета =====
@Composable
fun AmuletStatusCard(
    device: DeviceStatus?,
    onNavigateToPairing: () -> Unit
) {
    val spacing = AmuletTheme.spacing

    if (device == null) {
        // Амулет не подключен
        AmuletCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardElevation.Default
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.BluetoothDisabled,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(spacing.md))
                Text(
                    text = stringResource(R.string.amulet_status_disconnected_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.amulet_status_disconnected_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(spacing.lg))
                AmuletButton(
                    text = stringResource(R.string.amulet_status_connect_button),
                    onClick = onNavigateToPairing,
                    variant = ButtonVariant.Primary,
                    fullWidth = true,
                    icon = Icons.AutoMirrored.Filled.BluetoothSearching
                )
            }
        }
    } else {
        // Амулет подключен
        AmuletCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardElevation.Default
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.lg)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (device.connectionStatus) {
                                            "connected" -> AmuletPalette.Success
                                            "connecting" -> AmuletPalette.Warning
                                            else -> AmuletPalette.Error
                                        }
                                    )
                            )
                            Text(
                                text = stringResource(
                                    when (device.connectionStatus) {
                                        "connected" -> R.string.amulet_status_connected
                                        "connecting" -> R.string.amulet_status_connecting
                                        else -> R.string.amulet_status_disconnected
                                    }
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Анимированный аватар амулета
                    AnimatedAmuletAvatar(
                        currentAnimation = device.currentAnimation,
                        modifier = Modifier.size(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(spacing.lg))

                // Статистика устройства
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DeviceStatItem(
                        icon = Icons.Default.Battery4Bar,
                        label = stringResource(R.string.amulet_stat_battery),
                        value = "${device.batteryLevel}%",
                        color = when {
                            device.batteryLevel > 60 -> AmuletPalette.Success
                            device.batteryLevel > 20 -> AmuletPalette.Warning
                            else -> AmuletPalette.Error
                        }
                    )
                    DeviceStatItem(
                        icon = Icons.Default.SignalCellularAlt,
                        label = stringResource(R.string.amulet_stat_signal),
                        value = stringResource(R.string.amulet_stat_signal_excellent),
                        color = AmuletPalette.Success
                    )
                    DeviceStatItem(
                        icon = Icons.Default.Lightbulb,
                        label = stringResource(R.string.amulet_stat_state),
                        value = device.currentAnimation ?: stringResource(R.string.amulet_stat_state_off),
                        color = AmuletPalette.Primary
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedAmuletAvatar(
    currentAnimation: String?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "amulet_animation")

    val animatedColor by infiniteTransition.animateColor(
        initialValue = AmuletPalette.Primary,
        targetValue = AmuletPalette.Secondary,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color_animation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    Box(
        modifier = modifier
            .scale(if (currentAnimation != null) scale else 1f)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (currentAnimation != null) animatedColor else MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Brightness5,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun DeviceStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    val spacing = AmuletTheme.spacing

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.xs)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ===== Быстрый старт =====
@Composable
fun QuickStartSection(
    onStartPractice: (String) -> Unit
) {
    val spacing = AmuletTheme.spacing

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        Text(
            text = stringResource(R.string.quick_start_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        AmuletCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardElevation.Low
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.quick_start_breathing_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.quick_start_breathing_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(spacing.md))

                AmuletButton(
                    text = stringResource(R.string.quick_start_button),
                    onClick = { onStartPractice("breathing_4_7_8") },
                    variant = ButtonVariant.Primary,
                    size = ButtonSize.Small,
                    fullWidth = false,
                    icon = Icons.Default.PlayArrow
                )
            }
        }
    }
}

// ===== Статистика дня =====
@Composable
fun DailyStatsSection(
    stats: DailyStats
) {
    val spacing = AmuletTheme.spacing

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        Text(
            text = stringResource(R.string.daily_stats_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Timer,
                label = stringResource(R.string.daily_stats_practices),
                value = stringResource(R.string.daily_stats_minutes, stats.practiceMinutes),
                color = AmuletPalette.Primary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Favorite,
                label = stringResource(R.string.daily_stats_hugs),
                value = stringResource(R.string.daily_stats_count, stats.hugsCount),
                color = AmuletPalette.EmotionLove
            )
        }

        AmuletCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardElevation.Low
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.md)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = AmuletPalette.Success
                    )
                    Text(
                        text = stringResource(R.string.daily_stats_calm_level),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(spacing.sm))

                LinearProgressIndicator(
                    progress = { stats.calmLevel / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = AmuletPalette.EmotionCalm,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(spacing.xs))

                Text(
                    text = stringResource(
                        R.string.daily_stats_calm_percent,
                        stats.calmLevel,
                        getCalmLevelText(stats.calmLevel)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    val spacing = AmuletTheme.spacing

    AmuletCard(
        modifier = modifier,
        elevation = CardElevation.Low
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ===== Быстрый доступ =====
@Composable
fun QuickAccessGrid(
    onNavigateToLibrary: () -> Unit,
    onNavigateToHugs: () -> Unit,
    onNavigateToPatterns: () -> Unit
) {
    val spacing = AmuletTheme.spacing

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        Text(
            text = stringResource(R.string.quick_access_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            QuickAccessItem(
                icon = Icons.AutoMirrored.Filled.LibraryBooks,
                title = stringResource(R.string.quick_access_library_title),
                subtitle = stringResource(R.string.quick_access_library_subtitle),
                color = AmuletPalette.Primary,
                onClick = onNavigateToLibrary
            )
            QuickAccessItem(
                icon = Icons.Default.Favorite,
                title = stringResource(R.string.quick_access_hugs_title),
                subtitle = stringResource(R.string.quick_access_hugs_subtitle),
                color = AmuletPalette.EmotionLove,
                onClick = onNavigateToHugs
            )
            QuickAccessItem(
                icon = Icons.Default.Palette,
                title = stringResource(R.string.quick_access_patterns_title),
                subtitle = stringResource(R.string.quick_access_patterns_subtitle),
                color = AmuletPalette.Accent,
                onClick = onNavigateToPatterns
            )
        }
    }
}

@Composable
fun QuickAccessItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    val spacing = AmuletTheme.spacing

    AmuletCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardElevation.Low
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ===== Утилиты =====
@Composable
private fun getCalmLevelText(level: Int): String {
    return stringResource(
        when {
            level >= 80 -> R.string.daily_stats_calm_excellent
            level >= 60 -> R.string.daily_stats_calm_good
            level >= 40 -> R.string.daily_stats_calm_normal
            level >= 20 -> R.string.daily_stats_calm_could_be_better
            else -> R.string.daily_stats_calm_need_practice
        }
    )
}
