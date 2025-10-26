package com.example.amulet.feature.devices.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.amulet.feature.devices.R
import com.example.amulet.shared.domain.devices.model.Device
import com.example.amulet.shared.domain.devices.model.DeviceStatus

@Composable
fun DeviceCard(
    device: Device,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = device.name ?: stringResource(R.string.device_details_default_name),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DeviceStatusChip(status = device.status)
                    device.batteryLevel?.let { battery ->
                        BatteryIndicator(batteryLevel = battery)
                    }
                }
            }

            Icon(
                imageVector = when (device.status) {
                    DeviceStatus.ONLINE -> Icons.Default.Bluetooth
                    else -> Icons.Default.BluetoothDisabled
                },
                contentDescription = null,
                tint = when (device.status) {
                    DeviceStatus.ONLINE -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
fun DeviceStatusChip(status: DeviceStatus) {
    val (text, color) = when (status) {
        DeviceStatus.ONLINE -> stringResource(R.string.devices_list_status_online) to MaterialTheme.colorScheme.primary
        DeviceStatus.OFFLINE -> stringResource(R.string.devices_list_status_offline) to MaterialTheme.colorScheme.error
        DeviceStatus.CHARGING -> stringResource(R.string.devices_list_status_charging) to MaterialTheme.colorScheme.tertiary
        DeviceStatus.UNKNOWN -> stringResource(R.string.devices_list_status_unknown) to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun BatteryIndicator(batteryLevel: Double) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (batteryLevel > 0.5) Icons.Default.BatteryFull else Icons.Default.BatteryStd,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = when {
                batteryLevel > 0.5 -> MaterialTheme.colorScheme.primary
                batteryLevel > 0.2 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            }
        )
        Text(
            text = "${(batteryLevel * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
