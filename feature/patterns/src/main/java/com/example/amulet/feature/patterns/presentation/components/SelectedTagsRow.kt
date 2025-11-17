package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.amulet.feature.patterns.R

/**
 * Компонент для отображения строки с выбранными тегами фильтрации.
 * Показывает чипы с тегами и кнопку очистки всех тегов.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedTagsRow(
    selectedTags: Set<String>,
    onTagClick: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = selectedTags.isNotEmpty(),
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Кнопка очистки всех тегов (показывается если тегов больше одного)
                if (selectedTags.size > 1) {
                    item {
                        IconButton(onClearAll) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(R.string.filter_clear_button),
                                modifier = Modifier.size(InputChipDefaults.AvatarSize)
                            )
                        }
                    }
                }
                
                // Чипы с выбранными тегами
                items(selectedTags.sorted()) { tag ->
                    SelectedTagChip(
                        tag = tag,
                        onClick = { onTagClick(tag) }
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

/**
 * Чип для отображения выбранного тега с возможностью удаления.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectedTagChip(
    tag: String,
    onClick: () -> Unit
) {
    InputChip(
        selected = true,
        onClick = onClick,
        label = { Text(tag) },
        avatar = {
            Icon(
                Icons.Default.Clear,
                contentDescription = "Удалить тег",
                modifier = Modifier.size(InputChipDefaults.AvatarSize)
            )
        }
    )
}
