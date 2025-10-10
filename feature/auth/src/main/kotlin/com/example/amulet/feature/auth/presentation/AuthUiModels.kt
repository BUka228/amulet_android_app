package com.example.amulet.feature.auth.presentation

import com.example.amulet.shared.core.AppError

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val error: AppError? = null
)

sealed interface AuthUiEvent {
    data class EmailChanged(val value: String) : AuthUiEvent
    data class PasswordChanged(val value: String) : AuthUiEvent
    data object Submit : AuthUiEvent
    data object ErrorConsumed : AuthUiEvent
    data object GoogleSignInRequested : AuthUiEvent
    data class GoogleIdTokenReceived(val idToken: String) : AuthUiEvent
    data object GoogleSignInCancelled : AuthUiEvent
    data class GoogleSignInError(val throwable: Throwable?) : AuthUiEvent
}

sealed interface AuthSideEffect {
    data object SignInSuccess : AuthSideEffect
    data object LaunchGoogleSignIn : AuthSideEffect
}
