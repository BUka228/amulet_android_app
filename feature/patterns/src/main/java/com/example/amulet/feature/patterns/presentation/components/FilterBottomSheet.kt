package com.example.amulet.feature.patterns.presentation.components

import android.R.attr.singleLine
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.amulet.core.design.components.textfield.AmuletTextField
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.PatternKind

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    selectedKinds: Set<PatternKind>,
    selectedTags: Set<String>,
    availableTags: Set<String>,
    onKindToggle: (PatternKind) -> Unit,
    onTagClick: (String) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var tagSearchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Фильтруем теги по поисковому запросу
    val filteredTags = availableTags.filter { tag ->
        tag.contains(tagSearchQuery, ignoreCase = true)
    }.sorted()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.filter_sheet_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                
                TextButton(onClick = onClearFilters) {
                    Text(stringResource(R.string.filter_clear_button))
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Фильтр по типам паттернов
            Text(
                text = stringResource(R.string.filter_pattern_type_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                PatternKind.entries.forEach { kind ->
                    FilterChip(
                        label = when (kind) {
                            PatternKind.LIGHT -> stringResource(R.string.filter_kind_light)
                            PatternKind.HAPTIC -> stringResource(R.string.filter_kind_haptic)
                            PatternKind.COMBO -> stringResource(R.string.filter_kind_combo)
                        },
                        isSelected = kind in selectedKinds,
                        onClick = { onKindToggle(kind) }
                    )
                }
            }
            
            // Фильтр по тегам
            if (availableTags.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.filter_tags_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Поле поиска по тегам
                AmuletTextField(
                    value = tagSearchQuery,
                    onValueChange = { tagSearchQuery = it },
                    placeholder = stringResource(R.string.filter_search_tags_hint),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { keyboardController?.hide() }
                    ),
                    trailingIcon = if (tagSearchQuery.isNotEmpty()) {
                            Icons.Default.Clear
                        } else {
                            null
                        }

                )
                
                // Теги в FlowRow
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    filteredTags.forEach { tag ->
                        FilterChip(
                            label = tag,
                            isSelected = tag in selectedTags,
                            onClick = { onTagClick(tag) }
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
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
