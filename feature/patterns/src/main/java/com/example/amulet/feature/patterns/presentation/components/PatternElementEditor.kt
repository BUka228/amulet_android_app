package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.amulet.core.design.components.card.AmuletCard

import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.*

@Composable
fun PatternElementEditor(
    element: PatternElement,
    index: Int,
    onUpdate: (PatternElement) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    modifier: Modifier = Modifier,
    onAddElement: ((PatternElementType) -> Unit)? = null,
    onOpenEditor: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showElementPicker by remember { mutableStateOf(false) }

    // Element Picker BottomSheet
    if (showElementPicker && onAddElement != null) {
        PatternElementPickerDialog(
            onDismiss = { showElementPicker = false },
            onElementTypeSelected = { elementType ->
                onAddElement(elementType)
                showElementPicker = false
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(stringResource(R.string.dialog_delete_element_title))
            },
            text = {
                Text(stringResource(R.string.dialog_delete_element_message))
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemove()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.dialog_delete_element_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.dialog_delete_element_cancel))
                }
            }
        )
    }

    AmuletCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            // Компактный заголовок элемента
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = { onMoveUp?.invoke() },
                            enabled = onMoveUp != null,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowUp,
                                contentDescription = stringResource(R.string.cd_move_element_up),
                                modifier = Modifier.size(18.dp),
                                tint = if (onMoveUp != null) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                }
                            )
                        }
                        Icon(
                            getElementIcon(element),
                            contentDescription = stringResource(R.string.cd_element_icon),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { onMoveDown?.invoke() },
                            enabled = onMoveDown != null,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.cd_move_element_down),
                                modifier = Modifier.size(18.dp),
                                tint = if (onMoveDown != null) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                }
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${index + 1}. ${getElementName(element)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = getElementDescription(element),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column {

                    // Кнопка добавления нового элемента (если доступна)
                    if (onAddElement != null) {
                        IconButton(
                            onClick = { showElementPicker = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.pattern_editor_add_element),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.cd_delete_element),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    IconButton(
                        onClick = onOpenEditor,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.pattern_editor_element_expand),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}


// Вспомогательные функции

private fun getElementIcon(element: PatternElement) = when (element) {
    is PatternElementBreathing -> Icons.Default.Air
    is PatternElementPulse -> Icons.Default.FiberManualRecord
    is PatternElementChase -> Icons.Default.Autorenew
    is PatternElementFill -> Icons.Default.FormatColorFill
    is PatternElementSpinner -> Icons.Default.Sync
    is PatternElementProgress -> Icons.Default.LinearScale
    is PatternElementSequence -> Icons.AutoMirrored.Filled.List
    is PatternElementTimeline -> Icons.Default.Timeline
}

@Composable
private fun getElementName(element: PatternElement) = when (element) {
    is PatternElementBreathing -> stringResource(R.string.pattern_element_breathing)
    is PatternElementPulse -> stringResource(R.string.pattern_element_pulse)
    is PatternElementChase -> stringResource(R.string.pattern_element_chase)
    is PatternElementFill -> stringResource(R.string.pattern_element_fill)
    is PatternElementSpinner -> stringResource(R.string.pattern_element_spinner)
    is PatternElementProgress -> stringResource(R.string.pattern_element_progress)
    is PatternElementSequence -> stringResource(R.string.pattern_element_sequence)
    is PatternElementTimeline -> stringResource(R.string.pattern_element_timeline)
}

private fun getElementDescription(element: PatternElement) = when (element) {
    is PatternElementBreathing -> "${element.color}, ${element.durationMs}мс"
    is PatternElementPulse -> "${element.color}, ${element.speed}мс × ${element.repeats}"
    is PatternElementChase -> "${element.color}, ${element.speedMs}мс"
    is PatternElementFill -> "${element.color}, ${element.durationMs}мс"
    is PatternElementSpinner -> "${element.colors.size} цвета, ${element.speedMs}мс"
    is PatternElementProgress -> "${element.color}, ${element.activeLeds} диодов"
    is PatternElementSequence -> "${element.steps.size} шагов"
    is PatternElementTimeline -> "${element.tracks.size} треков, ${element.durationMs}мс"
}
