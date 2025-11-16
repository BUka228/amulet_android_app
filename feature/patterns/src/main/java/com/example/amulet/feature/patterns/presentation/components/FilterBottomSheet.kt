package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.amulet.shared.domain.patterns.model.PatternKind

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    selectedKinds: Set<PatternKind>,
    selectedTags: Set<String>,
    availableTags: Set<String>,
    onKindToggle: (PatternKind) -> Unit,
    onTagToggle: (String) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxHeight(0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Фильтры",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                TextButton(onClick = onClearFilters) {
                    Text("Очистить")
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Фильтр по типам паттернов
            Text(
                text = "Тип паттерна",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(PatternKind.values()) { kind ->
                    FilterChip(
                        label = when (kind) {
                            PatternKind.LIGHT -> "Свет"
                            PatternKind.HAPTIC -> "Вибрация"
                            PatternKind.COMBO -> "Комбо"
                        },
                        isSelected = kind in selectedKinds,
                        onClick = { onKindToggle(kind) }
                    )
                }
            }
            
            // Фильтр по тегам
            if (availableTags.isNotEmpty()) {
                Text(
                    text = "Теги",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(availableTags.sorted()) { tag ->
                        FilterChip(
                            label = tag,
                            isSelected = tag in selectedTags,
                            onClick = { onTagToggle(tag) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
