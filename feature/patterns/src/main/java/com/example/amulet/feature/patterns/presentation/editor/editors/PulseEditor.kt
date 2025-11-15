package com.example.amulet.feature.patterns.presentation.editor.editors

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.amulet.feature.patterns.R
import com.example.amulet.feature.patterns.presentation.components.ColorPicker
import com.example.amulet.shared.domain.patterns.model.PatternElement
import com.example.amulet.shared.domain.patterns.model.PatternElementPulse

@Composable
fun PulseEditor(
    element: PatternElementPulse,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) },
            label = stringResource(R.string.pattern_element_color_label)
        )

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
                    text = stringResource(R.string.time_format_ms, element.speed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = element.speed.toFloat(),
                onValueChange = { onUpdate(element.copy(speed = it.toInt())) },
                valueRange = 100f..2000f,
                steps = 18
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pattern_element_repeats_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = element.repeats.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = element.repeats.toFloat(),
                onValueChange = { onUpdate(element.copy(repeats = it.toInt())) },
                valueRange = 1f..10f,
                steps = 8
            )
        }
    }
}
