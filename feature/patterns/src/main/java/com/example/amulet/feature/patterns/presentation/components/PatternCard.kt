package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.components.card.CardElevation
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternKind

@Composable
fun PatternCard(
    pattern: Pattern,
    onClick: () -> Unit,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onShare: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    AmuletCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardElevation.Low
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Тип паттерна с иконкой
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = stringResource(
                                    when (pattern.kind) {
                                        PatternKind.LIGHT -> R.string.pattern_kind_light
                                        PatternKind.HAPTIC -> R.string.pattern_kind_haptic
                                        PatternKind.COMBO -> R.string.pattern_kind_combo
                                    }
                                ),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (pattern.kind) {
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
                                text = pluralStringResource(
                                    R.plurals.pattern_elements_count,
                                    pattern.spec.elements.size,
                                    pattern.spec.elements.size
                                ),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                    
                    // Показываем количество использований для публичных паттернов
                    val usageCount = pattern.usageCount
                    if (pattern.public && usageCount != null && usageCount > 0) {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = pluralStringResource(
                                        R.plurals.pattern_downloads_count,
                                        usageCount,
                                        usageCount
                                    ),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Кнопка предпросмотра
            IconButton(onClick = onPreview) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.cd_pattern_preview)
                )
            }

            // Меню действий
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.cd_pattern_menu)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.pattern_card_edit)) },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.pattern_card_duplicate)) },
                        onClick = {
                            showMenu = false
                            onDuplicate()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                        }
                    )
                    if (onShare != null) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.pattern_card_share)) },
                            onClick = {
                                showMenu = false
                                onShare()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Share, contentDescription = null)
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.pattern_card_delete)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        }
    }
}
