package com.example.amulet_android_app.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.amulet.core.design.components.button.AmuletButton
import com.example.amulet.core.design.components.button.ButtonVariant
import com.example.amulet.core.design.foundation.theme.AmuletTheme

@Composable
fun MainScreen(
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AmuletTheme.spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Main Dashboard Placeholder",
            style = MaterialTheme.typography.titleLarge
        )
        AmuletButton(
            text = "Open Settings",
            onClick = onOpenSettings,
            variant = ButtonVariant.Secondary,
            fullWidth = false,
            modifier = Modifier.padding(top = AmuletTheme.spacing.lg)
        )
    }
}
