package com.example.amulet.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.example.amulet.core.design.foundation.theme.AmuletTheme as FoundationTheme

@Composable
fun AmuletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    FoundationTheme(darkTheme = darkTheme, content = content)
}
