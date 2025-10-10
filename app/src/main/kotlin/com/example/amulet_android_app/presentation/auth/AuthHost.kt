package com.example.amulet_android_app.presentation.auth

import androidx.compose.runtime.Composable
import com.example.amulet.feature.auth.presentation.AuthRoute

@Composable
fun AuthHost(
    onAuthSuccess: () -> Unit
) {
    AuthRoute(onAuthSuccess = onAuthSuccess)
}
