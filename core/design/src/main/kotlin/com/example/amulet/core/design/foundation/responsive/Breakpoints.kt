package com.example.amulet.core.design.foundation.responsive

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Breakpoints {
    val Small: Dp = 600.dp
    val Medium: Dp = 840.dp
    val Large: Dp = 1200.dp
}

@Composable
@ReadOnlyComposable
fun rememberScreenWidth(): Dp = LocalConfiguration.current.screenWidthDp.dp

@Composable
fun Modifier.adaptivePadding(
    small: PaddingValues = PaddingValues(16.dp),
    medium: PaddingValues = PaddingValues(24.dp),
    large: PaddingValues = PaddingValues(32.dp)
): Modifier {
    val width = rememberScreenWidth()
    val padding = remember(width) {
        when {
            width < Breakpoints.Small -> small
            width < Breakpoints.Medium -> medium
            else -> large
        }
    }
    return this.padding(padding)
}
