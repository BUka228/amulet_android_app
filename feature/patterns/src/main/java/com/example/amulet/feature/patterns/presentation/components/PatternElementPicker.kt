package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.amulet.core.design.components.textfield.AmuletTextField
import com.example.amulet.feature.patterns.R
import com.example.amulet.shared.domain.patterns.model.*

/**
 * ModalBottomSheet для выбора типа элемента паттерна.
 * Отображает grid с карточками типов элементов, сгруппированными по категориям.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternElementPickerDialog(
    onDismiss: () -> Unit,
    onElementTypeSelected: (PatternElementType) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    var searchQuery by remember { mutableStateOf("") }
    val allElements = PatternElementType.entries
    val filteredElements = if (searchQuery.isBlank()) {
        allElements
    } else {
        allElements.filter { elementType ->
            val name = stringResource(elementType.nameRes)
            val description = stringResource(elementType.descriptionRes)
            name.contains(searchQuery, ignoreCase = true) ||
            description.contains(searchQuery, ignoreCase = true)
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.pattern_element_picker_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close_dialog),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Поиск
                AmuletTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = stringResource(R.string.search_patterns_placeholder),
                    leadingIcon = Icons.Default.Search,
                    trailingIcon = if (searchQuery.isNotEmpty()) Icons.Default.Clear else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
                
                HorizontalDivider()
            }
        }
    ) {
        if (filteredElements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = stringResource(R.string.empty_search_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.empty_search_description, searchQuery),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp, top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredElements) { elementType ->
                    ElementTypeCard(
                        elementType = elementType,
                        onClick = { 
                            onElementTypeSelected(elementType)
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun ElementTypeCard(
    elementType: PatternElementType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Верхняя часть - иконка и категория
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Иконка с фоном
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = elementType.icon,
                            contentDescription = stringResource(elementType.nameRes),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Категория
                Text(
                    text = stringResource(
                        when (elementType.category) {
                            ElementCategory.BASIC -> R.string.pattern_element_picker_category_basic
                            ElementCategory.MOTION -> R.string.pattern_element_picker_category_motion
                            ElementCategory.EFFECTS -> R.string.pattern_element_picker_category_effects
                        }
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Название
            Text(
                text = stringResource(elementType.nameRes),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            // Описание
            Text(
                text = stringResource(elementType.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * Типы элементов паттерна с метаданными для UI.
 */
enum class PatternElementType(
    val icon: ImageVector,
    val nameRes: Int,
    val descriptionRes: Int,
    val category: ElementCategory
) {
    BREATHING(
        icon = Icons.Default.Air,
        nameRes = R.string.pattern_element_breathing,
        descriptionRes = R.string.pattern_element_breathing_desc,
        category = ElementCategory.BASIC
    ),
    PULSE(
        icon = Icons.Default.FlashOn,
        nameRes = R.string.pattern_element_pulse,
        descriptionRes = R.string.pattern_element_pulse_desc,
        category = ElementCategory.BASIC
    ),
    FILL(
        icon = Icons.Default.WaterDrop,
        nameRes = R.string.pattern_element_fill,
        descriptionRes = R.string.pattern_element_fill_desc,
        category = ElementCategory.BASIC
    ),
    CHASE(
        icon = Icons.Default.Autorenew,
        nameRes = R.string.pattern_element_chase,
        descriptionRes = R.string.pattern_element_chase_desc,
        category = ElementCategory.MOTION
    ),
    SPINNER(
        icon = Icons.Default.Sync,
        nameRes = R.string.pattern_element_spinner,
        descriptionRes = R.string.pattern_element_spinner_desc,
        category = ElementCategory.MOTION
    ),
    PROGRESS(
        icon = Icons.Default.LinearScale,
        nameRes = R.string.pattern_element_progress,
        descriptionRes = R.string.pattern_element_progress_desc,
        category = ElementCategory.EFFECTS
    ),
    SEQUENCE(
        icon = Icons.Default.Code,
        nameRes = R.string.pattern_element_sequence,
        descriptionRes = R.string.pattern_element_sequence_desc,
        category = ElementCategory.EFFECTS
    );

    /**
     * Создает экземпляр элемента паттерна с параметрами по умолчанию.
     */
    fun createDefaultElement(): PatternElement {
        return when (this) {
            BREATHING -> PatternElementBreathing(
                color = "#FF0000",
                durationMs = 2000
            )
            PULSE -> PatternElementPulse(
                color = "#00FF00",
                speed = 500,
                repeats = 3
            )
            CHASE -> PatternElementChase(
                color = "#0000FF",
                direction = ChaseDirection.CLOCKWISE,
                speedMs = 150
            )
            FILL -> PatternElementFill(
                color = "#FFAA00",
                durationMs = 1500
            )
            SPINNER -> PatternElementSpinner(
                colors = listOf("#FF0000", "#0000FF"),
                speedMs = 200
            )
            PROGRESS -> PatternElementProgress(
                color = "#00FFFF",
                activeLeds = 4
            )
            SEQUENCE -> PatternElementSequence(
                steps = emptyList()
            )
        }
    }
}

/**
 * Категории элементов для группировки в UI.
 */
enum class ElementCategory {
    BASIC,
    MOTION,
    EFFECTS
}

