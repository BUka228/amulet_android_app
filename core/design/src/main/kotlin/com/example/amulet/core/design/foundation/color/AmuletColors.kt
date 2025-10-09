package com.example.amulet.core.design.foundation.color

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Палитра Amulet. Значения соответствуют спецификации продукта.
 */
object AmuletPalette {
    // Primary
    val Primary = Color(0xFF6B73FF)
    val PrimaryVariant = Color(0xFF5A63E8)
    val PrimaryLight = Color(0xFF8B94FF)

    // Secondary
    val Secondary = Color(0xFFFF6B9D)
    val SecondaryVariant = Color(0xFFE85A8B)
    val SecondaryLight = Color(0xFFFF8BB4)

    // Accent
    val Accent = Color(0xFFFFD93D)
    val AccentVariant = Color(0xFFE8C235)

    // Semantic
    val Success = Color(0xFF4CAF50)
    val SuccessLight = Color(0xFF81C784)
    val SuccessDark = Color(0xFF388E3C)

    val Warning = Color(0xFFFF9800)
    val WarningLight = Color(0xFFFFB74D)
    val WarningDark = Color(0xFFF57C00)

    val Error = Color(0xFFF44336)
    val ErrorLight = Color(0xFFEF5350)
    val ErrorDark = Color(0xFFD32F2F)

    val Info = Color(0xFF2196F3)
    val InfoLight = Color(0xFF64B5F6)
    val InfoDark = Color(0xFF1976D2)

    // Neutral
    val Gray50 = Color(0xFFFAFAFA)
    val Gray100 = Color(0xFFF5F5F5)
    val Gray200 = Color(0xFFEEEEEE)
    val Gray300 = Color(0xFFE0E0E0)
    val Gray400 = Color(0xFFBDBDBD)
    val Gray500 = Color(0xFF9E9E9E)
    val Gray600 = Color(0xFF757575)
    val Gray700 = Color(0xFF616161)
    val Gray800 = Color(0xFF424242)
    val Gray900 = Color(0xFF212121)

    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
    val BlackAlpha = Color(0x80000000)
    val WhiteAlpha = Color(0x80FFFFFF)

    // Amulet states
    val AmuletBreathing = Color(0xFF4CAF50)
    val AmuletPulse = Color(0xFFFF6B9D)
    val AmuletChase = Color(0xFF6B73FF)
    val AmuletSpinner = Color(0xFFFFD93D)
    val AmuletProgress = Color(0xFF2196F3)

    // Emotions
    val EmotionLove = Color(0xFFE91E63)
    val EmotionCalm = Color(0xFF4CAF50)
    val EmotionJoy = Color(0xFFFFD93D)
    val EmotionSadness = Color(0xFF2196F3)
    val EmotionEnergy = Color(0xFFFF9800)
    val EmotionSupport = Color(0xFF5A63E8)
}

@Immutable
data class SemanticColors(
    val success: Color,
    val successLight: Color,
    val successDark: Color,
    val warning: Color,
    val warningLight: Color,
    val warningDark: Color,
    val error: Color,
    val errorLight: Color,
    val errorDark: Color,
    val info: Color,
    val infoLight: Color,
    val infoDark: Color
)

val LightSemanticColors = SemanticColors(
    success = AmuletPalette.Success,
    successLight = AmuletPalette.SuccessLight,
    successDark = AmuletPalette.SuccessDark,
    warning = AmuletPalette.Warning,
    warningLight = AmuletPalette.WarningLight,
    warningDark = AmuletPalette.WarningDark,
    error = AmuletPalette.Error,
    errorLight = AmuletPalette.ErrorLight,
    errorDark = AmuletPalette.ErrorDark,
    info = AmuletPalette.Info,
    infoLight = AmuletPalette.InfoLight,
    infoDark = AmuletPalette.InfoDark
)

val DarkSemanticColors = SemanticColors(
    success = AmuletPalette.SuccessLight,
    successLight = AmuletPalette.Success,
    successDark = AmuletPalette.SuccessDark,
    warning = AmuletPalette.WarningLight,
    warningLight = AmuletPalette.Warning,
    warningDark = AmuletPalette.WarningDark,
    error = AmuletPalette.ErrorLight,
    errorLight = AmuletPalette.Error,
    errorDark = AmuletPalette.ErrorDark,
    info = AmuletPalette.InfoLight,
    infoLight = AmuletPalette.Info,
    infoDark = AmuletPalette.InfoDark
)

val AmuletLightColorScheme = lightColorScheme(
    primary = AmuletPalette.Primary,
    onPrimary = AmuletPalette.White,
    primaryContainer = AmuletPalette.PrimaryLight,
    onPrimaryContainer = AmuletPalette.Gray900,
    secondary = AmuletPalette.Secondary,
    onSecondary = AmuletPalette.White,
    secondaryContainer = AmuletPalette.SecondaryLight,
    onSecondaryContainer = AmuletPalette.Gray900,
    tertiary = AmuletPalette.Accent,
    onTertiary = AmuletPalette.Gray900,
    tertiaryContainer = AmuletPalette.AccentVariant,
    onTertiaryContainer = AmuletPalette.Gray900,
    background = AmuletPalette.Gray50,
    onBackground = AmuletPalette.Gray900,
    surface = AmuletPalette.White,
    onSurface = AmuletPalette.Gray900,
    surfaceVariant = AmuletPalette.Gray100,
    onSurfaceVariant = AmuletPalette.Gray700,
    inverseSurface = AmuletPalette.Gray900,
    inverseOnSurface = AmuletPalette.Gray100,
    outline = AmuletPalette.Gray300,
    error = AmuletPalette.Error,
    onError = AmuletPalette.White,
    errorContainer = AmuletPalette.ErrorLight,
    onErrorContainer = AmuletPalette.Gray900
)

val AmuletDarkColorScheme = darkColorScheme(
    primary = AmuletPalette.PrimaryLight,
    onPrimary = AmuletPalette.Gray900,
    primaryContainer = AmuletPalette.Primary,
    onPrimaryContainer = AmuletPalette.White,
    secondary = AmuletPalette.SecondaryLight,
    onSecondary = AmuletPalette.Gray900,
    secondaryContainer = AmuletPalette.Secondary,
    onSecondaryContainer = AmuletPalette.White,
    tertiary = AmuletPalette.Accent,
    onTertiary = AmuletPalette.Gray900,
    tertiaryContainer = AmuletPalette.AccentVariant,
    onTertiaryContainer = AmuletPalette.Gray900,
    background = AmuletPalette.Gray900,
    onBackground = AmuletPalette.Gray50,
    surface = AmuletPalette.Gray800,
    onSurface = AmuletPalette.Gray50,
    surfaceVariant = AmuletPalette.Gray700,
    onSurfaceVariant = AmuletPalette.Gray200,
    inverseSurface = AmuletPalette.Gray50,
    inverseOnSurface = AmuletPalette.Gray900,
    outline = AmuletPalette.Gray500,
    error = AmuletPalette.ErrorLight,
    onError = AmuletPalette.Gray900,
    errorContainer = AmuletPalette.Error,
    onErrorContainer = AmuletPalette.White
)
