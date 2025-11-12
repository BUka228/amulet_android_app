package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.amulet.core.design.components.textfield.AmuletTextField
import com.example.amulet.feature.patterns.R

/**
 * Компактный ColorPicker с пресетами и HEX вводом
 */
@Composable
fun ColorPicker(
    color: String,
    onColorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        PresetColorGrid(
            selectedColor = color,
            onColorSelected = onColorChange
        )

        HexColorInput(
            color = color,
            onColorChange = onColorChange
        )
    }
}

/**
 * Компактная сетка пресетов цветов
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetColorGrid(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val presetColors = remember {
        listOf(
            "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF",
            "#00FFFF", "#FF8800", "#8800FF", "#00FF88", "#FFFFFF"
        )
    }

    FlowRow(
        horizontalArrangement = Arrangement.SpaceEvenly,
        maxItemsInEachRow = 5,
        modifier = Modifier.fillMaxWidth()
    ) {
        presetColors.forEach { presetColor ->
            ColorChip(
                color = presetColor,
                isSelected = selectedColor.equals(presetColor, ignoreCase = true),
                onClick = { onColorSelected(presetColor) },
            )
        }
    }
}

/**
 * Компактный чип цвета
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorChip(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipColor = remember(color) { parseHexColor(color) }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(chipColor)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .semantics { contentDescription = color }
            )
        },
        modifier = modifier
    )
}

/**
 * Компактный HEX ввод
 */
@Composable
private fun HexColorInput(
    color: String,
    onColorChange: (String) -> Unit
) {
    var hexValue by remember(color) { mutableStateOf(color.removePrefix("#")) }
    var isError by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(parseHexColor(color))
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
        )

        AmuletTextField(
            value = hexValue,
            onValueChange = { newValue ->
                val cleaned = newValue.removePrefix("#").uppercase()
                if (cleaned.length <= 6 && cleaned.all { it in "0123456789ABCDEF" }) {
                    hexValue = cleaned
                    isError = cleaned.length != 6
                    if (cleaned.length == 6) {
                        onColorChange("#$cleaned")
                    }
                }
            },
            label = stringResource(R.string.color_picker_hex_label),
            placeholder = "RRGGBB",
            modifier = Modifier.weight(1f),
            singleLine = true,
            prefix = { Text("#") },
            errorText = if (isError) stringResource(R.string.color_picker_hex_error) else null
        )
    }
}

/**
 * Парсинг HEX в Color
 */
private fun parseHexColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = cleanHex.toLong(16).toInt()
        
        when (cleanHex.length) {
            6 -> Color(0xFF000000 or colorInt.toLong())
            8 -> Color(colorInt.toLong())
            else -> Color.Gray
        }
    } catch (e: Exception) {
        Color.Gray
    }
}
