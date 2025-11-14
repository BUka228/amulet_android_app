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
    val Primary = Color(0xFFA78BFA)
    val PrimaryVariant = Color(0xFF7C3AED)
    val PrimaryLight = Color(0xFFD6BCFA)

    // Secondary
    val Secondary = Color(0xFF34D399)
    val SecondaryVariant = Color(0xFF059669)
    val SecondaryLight = Color(0xFFA7F3D0)

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
    val CategoryBreath = Secondary
    val CategoryMeditation = Primary
    val CategorySoundscape = InfoLight
    val DeviceConnected = Success
    val DeviceDisconnected = Error
    val DevicePairing = Color(0xFF0EA5E9)
    val DeviceCharging = Accent
    val DeviceUpdating = PrimaryVariant
    val EmotionHugWarm = Color(0xFFF6C453)
    val EmotionMissYou = Color(0xFF93C5FD)
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

@Immutable
data class DeviceStateColors(
    val connected: Color,
    val disconnected: Color,
    val pairing: Color,
    val charging: Color,
    val updating: Color
)

val LightDeviceStateColors = DeviceStateColors(
    connected = AmuletPalette.DeviceConnected,
    disconnected = AmuletPalette.DeviceDisconnected,
    pairing = AmuletPalette.DevicePairing,
    charging = AmuletPalette.DeviceCharging,
    updating = AmuletPalette.DeviceUpdating
)

val DarkDeviceStateColors = DeviceStateColors(
    connected = AmuletPalette.SuccessLight,
    disconnected = AmuletPalette.ErrorLight,
    pairing = AmuletPalette.InfoLight,
    charging = AmuletPalette.Accent,
    updating = AmuletPalette.PrimaryLight
)

@Immutable
data class ContentCategoryColors(
    val breath: Color,
    val meditation: Color,
    val soundscape: Color
)

val LightContentCategoryColors = ContentCategoryColors(
    breath = AmuletPalette.CategoryBreath,
    meditation = AmuletPalette.CategoryMeditation,
    soundscape = AmuletPalette.CategorySoundscape
)

val DarkContentCategoryColors = ContentCategoryColors(
    breath = AmuletPalette.SecondaryLight,
    meditation = AmuletPalette.PrimaryLight,
    soundscape = AmuletPalette.InfoLight
)

@Immutable
data class GradientColors(
    val start: Color,
    val end: Color
)

val BrandGradientLight = GradientColors(
    start = AmuletPalette.Primary,
    end = AmuletPalette.Secondary
)

val BrandGradientDark = GradientColors(
    start = AmuletPalette.PrimaryLight,
    end = AmuletPalette.SecondaryLight
)

val CalmGradientLight = GradientColors(
    start = Color(0xFF60A5FA),
    end = AmuletPalette.SecondaryLight
)

val EnergyGradientLight = GradientColors(
    start = AmuletPalette.Warning,
    end = AmuletPalette.Accent
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
    inversePrimary = AmuletPalette.PrimaryVariant,
    outline = AmuletPalette.Gray300,
    outlineVariant = AmuletPalette.Gray400,
    surfaceTint = AmuletPalette.Primary,
    error = AmuletPalette.Error,
    onError = AmuletPalette.White,
    errorContainer = AmuletPalette.ErrorLight,
    onErrorContainer = AmuletPalette.Gray900,
    scrim = AmuletPalette.BlackAlpha,
    surfaceBright = AmuletPalette.Gray50,
    surfaceDim = AmuletPalette.Gray100,
    surfaceContainerLowest = AmuletPalette.White,
    surfaceContainerLow = AmuletPalette.Gray50,
    surfaceContainer = AmuletPalette.Gray100,
    surfaceContainerHigh = AmuletPalette.Gray200,
    surfaceContainerHighest = AmuletPalette.Gray300
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
    inversePrimary = AmuletPalette.Primary,
    outline = AmuletPalette.Gray500,
    outlineVariant = AmuletPalette.Gray600,
    surfaceTint = AmuletPalette.PrimaryLight,
    error = AmuletPalette.ErrorLight,
    onError = AmuletPalette.Gray900,
    errorContainer = AmuletPalette.Error,
    onErrorContainer = AmuletPalette.White,
    scrim = AmuletPalette.BlackAlpha,
    surfaceBright = AmuletPalette.Gray700,
    surfaceDim = AmuletPalette.Gray900,
    surfaceContainerLowest = AmuletPalette.Gray900,
    surfaceContainerLow = AmuletPalette.Gray800,
    surfaceContainer = AmuletPalette.Gray700,
    surfaceContainerHigh = AmuletPalette.Gray600,
    surfaceContainerHighest = AmuletPalette.Gray500
)

