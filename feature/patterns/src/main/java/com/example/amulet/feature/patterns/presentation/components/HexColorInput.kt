package com.example.amulet.feature.patterns.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.amulet.feature.patterns.R

/**
 * HEX color input component with validation and preview
 */
@Composable
fun HexColorInput(
    color: String,
    onColorChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var hexInput by remember(color) { mutableStateOf(color) }
    val isValid = remember(hexInput) { isValidHexColor(hexInput) }
    val previewColor = remember(hexInput) {
        if (isValid) parseHexColor(hexInput) else Color.Gray
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.color_picker_custom),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Color preview circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(previewColor)
                    .border(
                        width = 2.dp,
                        color = if (isValid) {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Accessibility: content description for preview
            }

            // HEX input field
            OutlinedTextField(
                value = hexInput,
                onValueChange = { newValue ->
                    // Allow only valid HEX characters
                    val filtered = newValue.filter { 
                        it in "0123456789ABCDEFabcdef#" 
                    }.take(7) // Max length: #RRGGBB
                    
                    hexInput = filtered
                    
                    // Update parent only if valid
                    if (isValidHexColor(filtered)) {
                        onColorChange(filtered.uppercase())
                    }
                },
                label = { Text(stringResource(R.string.color_picker_hex_label)) },
                placeholder = { Text(stringResource(R.string.color_picker_hex_hint)) },
                isError = hexInput.isNotEmpty() && !isValid,
                supportingText = {
                    if (hexInput.isNotEmpty() && !isValid) {
                        Text(
                            text = stringResource(R.string.error_invalid_hex_color),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    capitalization = KeyboardCapitalization.Characters
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Validate HEX color format
 */
private fun isValidHexColor(hex: String): Boolean {
    if (hex.isEmpty()) return false
    
    val cleanHex = hex.removePrefix("#")
    
    // Must be 6 characters (RRGGBB) or 8 characters (AARRGGBB)
    if (cleanHex.length != 6 && cleanHex.length != 8) return false
    
    // Must contain only valid hex characters
    return cleanHex.all { it in "0123456789ABCDEFabcdef" }
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
