package com.example.amulet.feature.patterns.presentation.editor.editors

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.amulet.core.design.components.textfield.AmuletTextField
import com.example.amulet.feature.patterns.R
import com.example.amulet.feature.patterns.presentation.components.ColorPicker
import com.example.amulet.shared.domain.patterns.model.*

@Composable
fun SequenceEditor(
    element: PatternElementSequence,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.sequence_steps_title, element.steps.size),
            style = MaterialTheme.typography.titleSmall
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            element.steps.forEachIndexed { idx, step ->
                ElevatedCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (step) {
                                    is SequenceStep.LedAction -> stringResource(R.string.sequence_step_led)
                                    is SequenceStep.DelayAction -> stringResource(R.string.sequence_step_delay)
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(enabled = idx > 0, onClick = {
                                    val list = element.steps.toMutableList()
                                    val tmp = list[idx - 1]
                                    list[idx - 1] = list[idx]
                                    list[idx] = tmp
                                    onUpdate(PatternElementSequence(list))
                                }) { Icon(Icons.Default.ArrowUpward, contentDescription = null) }
                                IconButton(enabled = idx < element.steps.lastIndex, onClick = {
                                    val list = element.steps.toMutableList()
                                    val tmp = list[idx + 1]
                                    list[idx + 1] = list[idx]
                                    list[idx] = tmp
                                    onUpdate(PatternElementSequence(list))
                                }) { Icon(Icons.Default.ArrowDownward, contentDescription = null) }
                                IconButton(onClick = {
                                    val list = element.steps.toMutableList()
                                    list.add(idx + 1, step)
                                    onUpdate(PatternElementSequence(list))
                                }) { Icon(Icons.Default.ContentCopy, contentDescription = null) }
                                IconButton(onClick = {
                                    val list = element.steps.toMutableList()
                                    list.removeAt(idx)
                                    onUpdate(PatternElementSequence(list))
                                }) { Icon(Icons.Default.Delete, contentDescription = null) }
                            }
                        }

                        when (step) {
                            is SequenceStep.LedAction -> {
                                var led by remember(step) { mutableStateOf(step.ledIndex) }
                                var color by remember(step) { mutableStateOf(step.color) }
                                var dur by remember(step) { mutableStateOf(step.durationMs) }
                                var durText by remember(dur) { mutableStateOf(dur.toString()) }

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(stringResource(R.string.sequence_led_index), style = MaterialTheme.typography.bodyMedium)
                                        Text(led.toString())
                                    }
                                    Slider(value = led.toFloat(), onValueChange = {
                                        led = it.toInt().coerceIn(0, 7)
                                        val list = element.steps.toMutableList()
                                        list[idx] = step.copy(ledIndex = led, color = color, durationMs = dur)
                                        onUpdate(PatternElementSequence(list))
                                    }, valueRange = 0f..7f, steps = 6)

                                    ColorPicker(
                                        color = color,
                                        onColorChange = { v ->
                                            color = v
                                            val list = element.steps.toMutableList()
                                            list[idx] = step.copy(ledIndex = led, color = color, durationMs = dur)
                                            onUpdate(PatternElementSequence(list))
                                        },
                                        label = stringResource(R.string.sequence_color_hex)
                                    )

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(stringResource(R.string.sequence_duration_ms), style = MaterialTheme.typography.bodyMedium)
                                        Text(dur.toString())
                                    }
                                    Slider(value = dur.toFloat(), onValueChange = {
                                        val nd = it.toInt().coerceIn(100, 6000)
                                        val list = element.steps.toMutableList()
                                        dur = nd
                                        durText = nd.toString()
                                        list[idx] = step.copy(ledIndex = led, color = color, durationMs = dur)
                                        onUpdate(PatternElementSequence(list))
                                    }, valueRange = 100f..6000f, steps = 50)

                                    AmuletTextField(
                                        value = durText,
                                        onValueChange = { txt ->
                                            val filtered = txt.filter { it.isDigit() }.take(5)
                                            durText = filtered
                                            val parsed = filtered.toIntOrNull()
                                            if (parsed != null) {
                                                val nd = parsed.coerceIn(100, 6000)
                                                if (nd != dur) {
                                                    dur = nd
                                                    val list = element.steps.toMutableList()
                                                    list[idx] = step.copy(ledIndex = led, color = color, durationMs = dur)
                                                    onUpdate(PatternElementSequence(list))
                                                }
                                            }
                                        },
                                        label = stringResource(R.string.sequence_duration_ms)
                                    )
                                }
                            }
                            is SequenceStep.DelayAction -> {
                                var dur by remember(step) { mutableStateOf(step.durationMs) }
                                var durText by remember(dur) { mutableStateOf(dur.toString()) }
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(stringResource(R.string.sequence_delay_ms), style = MaterialTheme.typography.bodyMedium)
                                        Text(dur.toString())
                                    }
                                    Slider(value = dur.toFloat(), onValueChange = {
                                        val nd = it.toInt().coerceIn(100, 6000)
        
                                        dur = nd
                                        durText = nd.toString()
                                        val list = element.steps.toMutableList()
                                        list[idx] = SequenceStep.DelayAction(dur)
                                        onUpdate(PatternElementSequence(list))
                                    }, valueRange = 100f..6000f, steps = 50)

                                    AmuletTextField(
                                        value = durText,
                                        onValueChange = { txt ->
                                            val filtered = txt.filter { it.isDigit() }.take(5)
                                            durText = filtered
                                            val parsed = filtered.toIntOrNull()
                                            if (parsed != null) {
                                                val nd = parsed.coerceIn(100, 6000)
                                                if (nd != dur) {
                                                    dur = nd
                                                    val list = element.steps.toMutableList()
                                                    list[idx] = SequenceStep.DelayAction(dur)
                                                    onUpdate(PatternElementSequence(list))
                                                }
                                            }
                                        },
                                        label = stringResource(R.string.sequence_delay_ms)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val list = element.steps.toMutableList()
                list.add(SequenceStep.LedAction(0, "#FFFFFF", 200))
                onUpdate(PatternElementSequence(list))
            }) { Text(stringResource(R.string.sequence_add_led)) }
            OutlinedButton(onClick = {
                val list = element.steps.toMutableList()
                list.add(SequenceStep.DelayAction(200))
                onUpdate(PatternElementSequence(list))
            }) { Text(stringResource(R.string.sequence_add_delay)) }
        }
    }
}
