package com.example.amulet.feature.patterns.presentation.components

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
import androidx.compose.ui.unit.dp
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
                HorizontalDivider()
            }
        }
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
                    // Базовые элементы
                    item {
                        CategoryHeader(
                            title = stringResource(R.string.pattern_element_picker_category_basic)
                        )
                    }
                    item { Spacer(modifier = Modifier.height(0.dp)) }

                    items(basicElements) { elementType ->
                        ElementTypeCard(
                            elementType = elementType,
                            onClick = { onElementTypeSelected(elementType) }
                        )
                    }
                    
                    // Заполнитель если нечетное количество
                    if (basicElements.size % 2 != 0) {
                        item { Spacer(modifier = Modifier.height(0.dp)) }
                    }

                    // Движение
                    item {
                        CategoryHeader(
                            title = stringResource(R.string.pattern_element_picker_category_motion)
                        )
                    }
                    item { Spacer(modifier = Modifier.height(0.dp)) }

                    items(motionElements) { elementType ->
                        ElementTypeCard(
                            elementType = elementType,
                            onClick = { onElementTypeSelected(elementType) }
                        )
                    }
                    
                    // Заполнитель если нечетное количество
                    if (motionElements.size % 2 != 0) {
                        item { Spacer(modifier = Modifier.height(0.dp)) }
                    }

                    // Эффекты
                    item {
                        CategoryHeader(
                            title = stringResource(R.string.pattern_element_picker_category_effects)
                        )
                    }
                    item { Spacer(modifier = Modifier.height(0.dp)) }

                    items(effectElements) { elementType ->
                        ElementTypeCard(
                            elementType = elementType,
                            onClick = { onElementTypeSelected(elementType) }
                        )
                    }
                }
    }
}

@Composable
private fun CategoryHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp)
    )
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
            .height(140.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Иконка с фоном
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = elementType.icon,
                        contentDescription = stringResource(elementType.nameRes),
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Название
            Text(
                text = stringResource(elementType.nameRes),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Описание
            Text(
                text = stringResource(elementType.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                modifier = Modifier.align(Alignment.CenterHorizontally)
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

// Группировка элементов по категориям
private val basicElements = PatternElementType.values()
    .filter { it.category == ElementCategory.BASIC }

private val motionElements = PatternElementType.values()
    .filter { it.category == ElementCategory.MOTION }

private val effectElements = PatternElementType.values()
    .filter { it.category == ElementCategory.EFFECTS }
