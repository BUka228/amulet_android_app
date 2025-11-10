package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.amulet.shared.domain.patterns.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternElementEditor(
    element: PatternElement,
    index: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onUpdate: (PatternElement) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var showDetails by remember { mutableStateOf(isSelected) }

    LaunchedEffect(isSelected) {
        showDetails = isSelected
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            onSelect()
            showDetails = !showDetails
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Заголовок элемента
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        getElementIcon(element),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "${index + 1}. ${getElementName(element)}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = getElementDescription(element),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    // Кнопки перемещения
                    if (onMoveUp != null) {
                        IconButton(
                            onClick = onMoveUp,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowUp,
                                contentDescription = "Вверх",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (onMoveDown != null) {
                        IconButton(
                            onClick = onMoveDown,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Вниз",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showDetails = !showDetails },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showDetails) "Свернуть" else "Развернуть",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Детали элемента (раскрывающиеся)
            if (showDetails) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                when (element) {
                    is PatternElementBreathing -> BreathingEditor(element, onUpdate)
                    is PatternElementPulse -> PulseEditor(element, onUpdate)
                    is PatternElementChase -> ChaseEditor(element, onUpdate)
                    is PatternElementFill -> FillEditor(element, onUpdate)
                    is PatternElementSpinner -> SpinnerEditor(element, onUpdate)
                    is PatternElementProgress -> ProgressEditor(element, onUpdate)
                    is PatternElementSequence -> SequenceEditor(element, onUpdate)
                }
            }
        }
    }
}

@Composable
private fun BreathingEditor(
    element: PatternElementBreathing,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) }
        )
        
        Text("Длительность: ${element.durationMs} мс", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = element.durationMs.toFloat(),
            onValueChange = { onUpdate(element.copy(durationMs = it.toInt())) },
            valueRange = 100f..10000f,
            steps = 98
        )
    }
}

@Composable
private fun PulseEditor(
    element: PatternElementPulse,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) }
        )
        
        Text("Скорость: ${element.speed} мс", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = element.speed.toFloat(),
            onValueChange = { onUpdate(element.copy(speed = it.toInt())) },
            valueRange = 100f..2000f,
            steps = 18
        )

        Text("Повторы: ${element.repeats}", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = element.repeats.toFloat(),
            onValueChange = { onUpdate(element.copy(repeats = it.toInt())) },
            valueRange = 1f..10f,
            steps = 8
        )
    }
}

@Composable
private fun ChaseEditor(
    element: PatternElementChase,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) }
        )

        Text("Направление", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = element.direction == ChaseDirection.CLOCKWISE,
                onClick = { onUpdate(element.copy(direction = ChaseDirection.CLOCKWISE)) },
                label = { Text("По часовой") }
            )
            FilterChip(
                selected = element.direction == ChaseDirection.COUNTER_CLOCKWISE,
                onClick = { onUpdate(element.copy(direction = ChaseDirection.COUNTER_CLOCKWISE)) },
                label = { Text("Против часовой") }
            )
        }
        
        Text("Скорость: ${element.speedMs} мс", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = element.speedMs.toFloat(),
            onValueChange = { onUpdate(element.copy(speedMs = it.toInt())) },
            valueRange = 50f..1000f,
            steps = 18
        )
    }
}

@Composable
private fun FillEditor(
    element: PatternElementFill,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) }
        )
        
        Text("Длительность: ${element.durationMs} мс", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = element.durationMs.toFloat(),
            onValueChange = { onUpdate(element.copy(durationMs = it.toInt())) },
            valueRange = 100f..5000f,
            steps = 48
        )
    }
}

@Composable
private fun SpinnerEditor(
    element: PatternElementSpinner,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Цвет 1", style = MaterialTheme.typography.bodySmall)
        ColorPicker(
            color = element.colors.getOrNull(0) ?: "#FFFFFF",
            onColorChange = { 
                val newColors = element.colors.toMutableList()
                newColors[0] = it
                onUpdate(element.copy(colors = newColors))
            }
        )

        Text("Цвет 2", style = MaterialTheme.typography.bodySmall)
        ColorPicker(
            color = element.colors.getOrNull(1) ?: "#000000",
            onColorChange = { 
                val newColors = element.colors.toMutableList()
                if (newColors.size > 1) {
                    newColors[1] = it
                } else {
                    newColors.add(it)
                }
                onUpdate(element.copy(colors = newColors))
            }
        )
        
        Text("Скорость: ${element.speedMs} мс", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = element.speedMs.toFloat(),
            onValueChange = { onUpdate(element.copy(speedMs = it.toInt())) },
            valueRange = 50f..1000f,
            steps = 18
        )
    }
}

@Composable
private fun ProgressEditor(
    element: PatternElementProgress,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ColorPicker(
            color = element.color,
            onColorChange = { onUpdate(element.copy(color = it)) }
        )
        
        Text("Активных диодов: ${element.activeLeds}", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = element.activeLeds.toFloat(),
            onValueChange = { onUpdate(element.copy(activeLeds = it.toInt())) },
            valueRange = 1f..8f,
            steps = 6
        )
    }
}

@Composable
private fun SequenceEditor(
    element: PatternElementSequence,
    onUpdate: (PatternElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Последовательность из ${element.steps.size} шагов", style = MaterialTheme.typography.bodySmall)
        Text(
            "Редактирование последовательностей в разработке",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ColorPicker(
    color: String,
    onColorChange: (String) -> Unit
) {
    val presetColors = listOf(
        "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF",
        "#FF8800", "#8800FF", "#00FF88", "#FFFFFF"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Цвет: $color", style = MaterialTheme.typography.bodySmall)
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            presetColors.take(5).forEach { presetColor ->
                FilterChip(
                    selected = color.equals(presetColor, ignoreCase = true),
                    onClick = { onColorChange(presetColor) },
                    label = { Text(presetColor.takeLast(6)) }
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            presetColors.drop(5).forEach { presetColor ->
                FilterChip(
                    selected = color.equals(presetColor, ignoreCase = true),
                    onClick = { onColorChange(presetColor) },
                    label = { Text(presetColor.takeLast(6)) }
                )
            }
        }
    }
}

// Вспомогательные функции

private fun getElementIcon(element: PatternElement) = when (element) {
    is PatternElementBreathing -> Icons.Default.Air
    is PatternElementPulse -> Icons.Default.FiberManualRecord
    is PatternElementChase -> Icons.Default.RotateRight
    is PatternElementFill -> Icons.Default.FormatColorFill
    is PatternElementSpinner -> Icons.Default.Autorenew
    is PatternElementProgress -> Icons.Default.LinearScale
    is PatternElementSequence -> Icons.Default.ViewList
}

private fun getElementName(element: PatternElement) = when (element) {
    is PatternElementBreathing -> "Дыхание"
    is PatternElementPulse -> "Пульсация"
    is PatternElementChase -> "Бегущие огни"
    is PatternElementFill -> "Заполнение"
    is PatternElementSpinner -> "Спиннер"
    is PatternElementProgress -> "Прогресс"
    is PatternElementSequence -> "Последовательность"
}

private fun getElementDescription(element: PatternElement) = when (element) {
    is PatternElementBreathing -> "${element.color}, ${element.durationMs}мс"
    is PatternElementPulse -> "${element.color}, ${element.speed}мс × ${element.repeats}"
    is PatternElementChase -> "${element.color}, ${element.speedMs}мс"
    is PatternElementFill -> "${element.color}, ${element.durationMs}мс"
    is PatternElementSpinner -> "${element.colors.size} цвета, ${element.speedMs}мс"
    is PatternElementProgress -> "${element.color}, ${element.activeLeds} диодов"
    is PatternElementSequence -> "${element.steps.size} шагов"
}
