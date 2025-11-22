package com.example.amulet.core.design.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class CardElevation(val value: Dp) {
    None(0.dp),
    Low(2.dp),
    Default(4.dp),
    High(8.dp)
}

@Composable
fun AmuletCard(
    modifier: Modifier = Modifier,
    elevation: CardElevation = CardElevation.Default,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = null,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Card(
        modifier = modifier,
        shape = shape,
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.value)
    ) {
        content()
    }
}
