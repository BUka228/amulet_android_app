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

// ===== Секция Устройств =====
@Composable
fun DevicesSection(
    devices: List<com.example.amulet.shared.domain.devices.model.Device>,
    connectedDevice: com.example.amulet.shared.domain.devices.model.Device?,
    connectedBatteryLevel: Int?,
    onDeviceClick: (String) -> Unit,
    onNavigateToPairing: () -> Unit,
    onNavigateToDevicesList: () -> Unit
) {
    val spacing = AmuletTheme.spacing

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.xs)
    ) {
        // Header с кнопкой "Все устройства"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dashboard_devices_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (devices.isNotEmpty()) {
                TextButton(onClick = onNavigateToDevicesList) {
                    Text(
                        stringResource(R.string.dashboard_view_all),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        if (devices.isEmpty()) {
            // Нет устройств - призыв к действию
            EmptyDevicesCard(onNavigateToPairing = onNavigateToPairing)
        } else {
            // Показываем подключенное устройство или первое в списке
            val displayDevice = connectedDevice ?: devices.firstOrNull()
            displayDevice?.let { device ->
                ConnectedDeviceCard(
                    device = device,
                    isConnected = device == connectedDevice,
                    connectedBatteryLevel = if (device == connectedDevice) connectedBatteryLevel else null,
                    onClick = { onDeviceClick(device.id.value) }
                )
            }
            
            // Если есть еще устройства, показываем их количество
            if (devices.size > 1) {
                Text(
                    text = stringResource(R.string.dashboard_more_devices, devices.size - 1),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = spacing.sm)
                )
            }
        }
    }
}

@Composable
private fun EmptyDevicesCard(
    onNavigateToPairing: () -> Unit
) {
    val spacing = AmuletTheme.spacing

    AmuletCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardElevation.Low
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.BluetoothDisabled,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(spacing.sm))
            Text(
                text = stringResource(R.string.dashboard_no_devices_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.dashboard_no_devices_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(spacing.md))
            AmuletButton(
                text = stringResource(R.string.dashboard_add_device_button),
                onClick = onNavigateToPairing,
                variant = ButtonVariant.Primary,
                fullWidth = true,
                size = ButtonSize.Small,
                icon = Icons.AutoMirrored.Filled.BluetoothSearching
            )
        }
    }
}

@Composable
private fun ConnectedDeviceCard(
    device: com.example.amulet.shared.domain.devices.model.Device,
    isConnected: Boolean,
    connectedBatteryLevel: Int?,
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
                .padding(spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                Icon(
                    imageVector = Icons.Default.Watch,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isConnected) AmuletPalette.Primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Column {
                    Text(
                        text = device.name ?: device.bleAddress,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) AmuletPalette.Success else MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = if (isConnected) "Подключено" else "Не подключено",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val batteryToShow: Int? = when {
                isConnected && connectedBatteryLevel != null -> connectedBatteryLevel
                device.batteryLevel != null -> device.batteryLevel?.toInt()
                else -> null
            }

            batteryToShow?.let { battery ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = when {
                            battery > 60 -> AmuletPalette.Success
                            battery > 20 -> AmuletPalette.Warning
                            else -> AmuletPalette.Error
                        }
                    )
                    Text(
                        text = "${battery}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


// ===== Быстрый старт =====
@Composable
fun QuickStartSection(
    title: String?,
    subtitle: String?,
    onStartPractice: () -> Unit
) {
    val spacing = AmuletTheme.spacing

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.xs)
    ) {
        Text(
            text = stringResource(R.string.quick_start_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        AmuletCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardElevation.Low
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val primaryTitle = title ?: stringResource(R.string.quick_start_breathing_title)
                val primarySubtitle = subtitle?.takeWhile { it != '.' } ?: stringResource(R.string.quick_start_breathing_subtitle)

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AmuletPalette.Primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = null,
                            tint = AmuletPalette.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = primaryTitle,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = primarySubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onStartPractice) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.quick_start_button),
                        tint = AmuletPalette.Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
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
        verticalArrangement = Arrangement.spacedBy(spacing.xs)
    ) {
        Text(
            text = stringResource(R.string.quick_access_title),
            style = MaterialTheme.typography.titleMedium,
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
                .padding(spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
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
