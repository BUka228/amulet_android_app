package com.example.amulet.core.design.components.textfield

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmuletTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    trailingIconContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    minLines: Int = 1,
    prefix: (@Composable () -> Unit)? = null,
) {
    val isError = !errorText.isNullOrBlank()

    val trailing = when {
        trailingIconContent != null -> trailingIconContent
        trailingIcon != null -> {
            {
                androidx.compose.material3.Icon(
                    imageVector = trailingIcon,
                    contentDescription = null
                )
            }
        }
        else -> null
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label?.let { { Text(text = it) } },
        placeholder = placeholder?.let { { Text(text = it) } },
        supportingText = {
            when {
                !errorText.isNullOrBlank() -> Text(text = errorText, color = MaterialTheme.colorScheme.error)
                !helperText.isNullOrBlank() -> Text(text = helperText)
            }
        },
        leadingIcon = leadingIcon?.let { icon ->
            { androidx.compose.material3.Icon(imageVector = icon, contentDescription = null) }
        },
        trailingIcon = trailing,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        isError = isError,
        shape = MaterialTheme.shapes.large,
        minLines = minLines,
        prefix = prefix
    )
}
