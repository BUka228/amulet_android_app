package com.example.amulet.feature.patterns.presentation.editor.editors

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.amulet.feature.patterns.R
import com.example.amulet.feature.patterns.presentation.components.ColorPicker
import com.example.amulet.shared.domain.patterns.model.PatternElement
import com.example.amulet.shared.domain.patterns.model.PatternElementSpinner

@Composable
fun SpinnerEditor(
    element: PatternElementSpinner,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ColorPicker(
            color = element.colors.getOrNull(0) ?: "#FFFFFF",
            onColorChange = {
                val newColors = element.colors.toMutableList()
                if (newColors.isEmpty()) {
                    newColors.add(it)
                } else {
                    newColors[0] = it
                }
                onUpdate(element.copy(colors = newColors))
            },
            label = stringResource(R.string.pattern_element_color_primary)
        )

        ColorPicker(
            color = element.colors.getOrNull(1) ?: "#000000",
            onColorChange = {
                val newColors = element.colors.toMutableList()
                if (newColors.size > 1) {
                    newColors[1] = it
                } else {
                    while (newColors.size < 2) {
                        newColors.add(it)
                    }
                }
                onUpdate(element.copy(colors = newColors))
            },
            label = stringResource(R.string.pattern_element_color_secondary)
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
