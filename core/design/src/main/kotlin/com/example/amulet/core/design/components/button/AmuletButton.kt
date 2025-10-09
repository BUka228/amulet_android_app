package com.example.amulet.core.design.components.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.amulet.core.design.foundation.theme.AmuletTheme

enum class ButtonVariant { Primary, Secondary, Outline, Text, Ghost }

enum class ButtonSize(val height: Int) {
    Small(height = 36),
    Medium(height = 44),
    Large(height = 52)
}

@Composable
fun AmuletButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    fullWidth: Boolean = true,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.onPrimary
) {
    val spacing = AmuletTheme.spacing
    val heightModifier = modifier.height(size.height.dp)
    val shape: Shape = MaterialTheme.shapes.medium

    val containerColor: Color
    val contentColor: Color
    val border = when (variant) {
        ButtonVariant.Outline -> ButtonDefaults.outlinedButtonBorder(enabled = enabled && !loading)
        ButtonVariant.Ghost -> null
        else -> null
    }

    when (variant) {
        ButtonVariant.Primary -> {
            containerColor = MaterialTheme.colorScheme.primary
            contentColor = MaterialTheme.colorScheme.onPrimary
        }
        ButtonVariant.Secondary -> {
            containerColor = MaterialTheme.colorScheme.secondary
            contentColor = MaterialTheme.colorScheme.onSecondary
        }
        ButtonVariant.Outline -> {
            containerColor = Color.Transparent
            contentColor = MaterialTheme.colorScheme.primary
        }
        ButtonVariant.Text -> {
            containerColor = Color.Transparent
            contentColor = MaterialTheme.colorScheme.primary
        }
        ButtonVariant.Ghost -> {
            containerColor = Color.Transparent
            contentColor = MaterialTheme.colorScheme.onSurface
        }
    }

    val content: @Composable RowScope.() -> Unit = {
        Row(
            modifier = Modifier
                .padding(horizontal = spacing.md)
                .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
            } else {
                icon?.let {
                    androidx.compose.material3.Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.padding(end = spacing.sm)
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    when (variant) {
        ButtonVariant.Primary, ButtonVariant.Secondary -> {
            Button(
                onClick = onClick,
                modifier = heightModifier,
                enabled = enabled && !loading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                    disabledContainerColor = containerColor.copy(alpha = 0.4f),
                    disabledContentColor = contentColor.copy(alpha = 0.4f)
                ),
                content = content
            )
        }

        ButtonVariant.Outline -> {
            OutlinedButton(
                onClick = onClick,
                modifier = heightModifier,
                enabled = enabled && !loading,
                shape = shape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = contentColor,
                    disabledContentColor = contentColor.copy(alpha = 0.4f)
                ),
                border = border,
                content = content
            )
        }

        ButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = heightModifier,
                enabled = enabled && !loading,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = contentColor,
                    disabledContentColor = contentColor.copy(alpha = 0.4f)
                ),
                content = content
            )
        }

        ButtonVariant.Ghost -> {
            TextButton(
                onClick = onClick,
                modifier = heightModifier,
                enabled = enabled && !loading,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = contentColor,
                    containerColor = containerColor
                ),
                content = content
            )
        }
    }
}
