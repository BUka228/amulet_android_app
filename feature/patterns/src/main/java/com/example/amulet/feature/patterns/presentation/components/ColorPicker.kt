package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.example.amulet.feature.patterns.R

/**
 * ColorPicker component with preset colors and HEX input
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Preset colors section
        Text(
            text = stringResource(R.string.color_picker_preset_colors),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PresetColorGrid(
            selectedColor = color,
            onColorSelected = onColorChange
        )

        // HEX color input
        HexColorInput(
            color = color,
            onColorChange = onColorChange
        )
    }
}

/**
 * Grid of preset color chips
 */
@Composable
private fun PresetColorGrid(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val presetColors = remember {
        listOf(
            "#FF0000", // Red
            "#00FF00", // Green
            "#0000FF", // Blue
            "#FFFF00", // Yellow
            "#FF00FF", // Magenta
            "#00FFFF", // Cyan
            "#FF8800", // Orange
            "#8800FF", // Purple
            "#00FF88", // Spring Green
            "#FFFFFF"  // White
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // First row (5 colors)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            presetColors.take(5).forEach { presetColor ->
                key(presetColor) {
                    ColorChip(
                        color = presetColor,
                        isSelected = selectedColor.equals(presetColor, ignoreCase = true),
                        onClick = { onColorSelected(presetColor) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Second row (5 colors)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            presetColors.drop(5).forEach { presetColor ->
                key(presetColor) {
                    ColorChip(
                        color = presetColor,
                        isSelected = selectedColor.equals(presetColor, ignoreCase = true),
                        onClick = { onColorSelected(presetColor) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Individual color chip with visual feedback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorChip(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipColor = remember(color) {
        parseHexColor(color)
    }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Color indicator circle
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(chipColor)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .semantics {
                            contentDescription = color
                        }
                )
                
                // Color code (last 6 characters)
                Text(
                    text = color.takeLast(6),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Parse HEX color string to Compose Color
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
