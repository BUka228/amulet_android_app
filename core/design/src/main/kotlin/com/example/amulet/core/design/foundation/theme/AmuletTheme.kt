package com.example.amulet.core.design.foundation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.amulet.core.design.foundation.color.AmuletDarkColorScheme
import com.example.amulet.core.design.foundation.color.AmuletLightColorScheme
import com.example.amulet.core.design.foundation.color.DarkSemanticColors
import com.example.amulet.core.design.foundation.color.LightSemanticColors
import com.example.amulet.core.design.foundation.color.SemanticColors
import com.example.amulet.core.design.foundation.shape.AmuletShapes
import com.example.amulet.core.design.foundation.shape.LocalAmuletShapes
import com.example.amulet.core.design.foundation.spacing.AmuletSpacing
import com.example.amulet.core.design.foundation.spacing.LocalAmuletSpacing
import com.example.amulet.core.design.foundation.typography.AmuletTypography
import com.example.amulet.core.design.foundation.typography.LocalAmuletTypography

val LocalSemanticColors = staticCompositionLocalOf { LightSemanticColors }

@Composable
fun AmuletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    spacing: AmuletSpacing = AmuletSpacing(),
    semanticColors: SemanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AmuletDarkColorScheme else AmuletLightColorScheme

    CompositionLocalProvider(
        LocalAmuletSpacing provides spacing,
        LocalSemanticColors provides semanticColors,
        LocalAmuletShapes provides AmuletShapes,
        LocalAmuletTypography provides AmuletTypography
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AmuletTypography,
            shapes = AmuletShapes,
            content = content
        )
    }
}

object AmuletTheme {
    val colors: SemanticColors
        @Composable
        get() = LocalSemanticColors.current

    val spacing: AmuletSpacing
        @Composable
        get() = LocalAmuletSpacing.current
}
