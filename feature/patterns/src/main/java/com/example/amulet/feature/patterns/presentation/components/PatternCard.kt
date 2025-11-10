package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternKind

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternCard(
    pattern: Pattern,
    onClick: () -> Unit,
    onPreview: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Визуализация паттерна (небольшая превью)
            PatternVisualizer(
                pattern = pattern,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Информация о паттерне
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pattern.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                pattern.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Тип паттерна
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                when (pattern.kind) {
                                    PatternKind.LIGHT -> "Свет"
                                    PatternKind.HAPTIC -> "Вибрация"
                                    PatternKind.COMBO -> "Комбо"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                when (pattern.kind) {
                                    PatternKind.LIGHT -> Icons.Default.Lightbulb
                                    PatternKind.HAPTIC -> Icons.Default.SettingsInputComponent
                                    PatternKind.COMBO -> Icons.Default.Star
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )

                    // Количество элементов
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                "${pattern.spec.elements.size} эл.",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            // Кнопка предпросмотра
            IconButton(onClick = onPreview) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Предпросмотр"
                )
            }

            // Меню действий
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Больше действий"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Дублировать") },
                        onClick = {
                            showMenu = false
                            onDuplicate()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Удалить") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}
