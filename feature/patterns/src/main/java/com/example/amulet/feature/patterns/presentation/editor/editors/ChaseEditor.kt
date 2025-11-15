package com.example.amulet.feature.patterns.presentation.editor.editors

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.amulet.feature.patterns.R
import com.example.amulet.feature.patterns.presentation.components.ColorPicker
import com.example.amulet.shared.domain.patterns.model.*

@Composable
fun ChaseEditor(
    element: PatternElementChase,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) },
            label = stringResource(R.string.pattern_element_color_label)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.pattern_element_direction_label),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = element.direction == ChaseDirection.CLOCKWISE,
                    onClick = { onUpdate(element.copy(direction = ChaseDirection.CLOCKWISE)) },
                    label = { Text(stringResource(R.string.pattern_element_direction_cw)) },
                    leadingIcon = if (element.direction == ChaseDirection.CLOCKWISE) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = element.direction == ChaseDirection.COUNTER_CLOCKWISE,
                    onClick = { onUpdate(element.copy(direction = ChaseDirection.COUNTER_CLOCKWISE)) },
                    label = { Text(stringResource(R.string.pattern_element_direction_ccw)) },
                    leadingIcon = if (element.direction == ChaseDirection.COUNTER_CLOCKWISE) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_speed_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = stringResource(R.string.time_format_ms, element.speedMs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = element.speedMs.toFloat(),
                onValueChange = { onUpdate(element.copy(speedMs = it.toInt())) },
                valueRange = 50f..1000f,
                steps = 18
            )
        }
    }
}
