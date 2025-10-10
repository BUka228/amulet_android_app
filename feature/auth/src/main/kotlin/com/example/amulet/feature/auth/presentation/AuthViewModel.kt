package com.example.amulet.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.auth.model.UserCredentials
import com.example.amulet.shared.domain.auth.usecase.SignInUseCase
import com.example.amulet.shared.domain.auth.usecase.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import com.github.michaelbull.result.fold
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _sideEffects = MutableSharedFlow<AuthSideEffect>()
    val sideEffects: SharedFlow<AuthSideEffect> = _sideEffects.asSharedFlow()

    fun handleEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.EmailChanged -> _uiState.update { it.copy(email = event.value, error = null) }
            is AuthUiEvent.PasswordChanged -> _uiState.update { it.copy(password = event.value, error = null) }
            AuthUiEvent.Submit -> submit()
            AuthUiEvent.ErrorConsumed -> _uiState.update { it.copy(error = null) }
            AuthUiEvent.GoogleSignInRequested -> requestGoogleSignIn()
            is AuthUiEvent.GoogleIdTokenReceived -> signInWithGoogle(event.idToken)
            AuthUiEvent.GoogleSignInCancelled -> _uiState.update { it.copy(isSubmitting = false) }
            is AuthUiEvent.GoogleSignInError -> handleGoogleSignInError(event.throwable)
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update {
                it.copy(
                    error = AppError.Validation(mapOf("credentials" to "Введите email и пароль"))
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val result = signInUseCase(UserCredentials(state.email.trim(), state.password))
            result.fold(
                success = {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _sideEffects.emit(AuthSideEffect.SignInSuccess)
                },
                failure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, error = error) }
                }
            )
        }
    }

    private fun requestGoogleSignIn() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            _sideEffects.emit(AuthSideEffect.LaunchGoogleSignIn)
        }
    }

    private fun signInWithGoogle(idToken: String) {
        if (idToken.isBlank()) {
            _uiState.update { it.copy(isSubmitting = false, error = AppError.Unknown) }
            return
        }

        viewModelScope.launch {
            val result = signInWithGoogleUseCase(idToken)
            result.fold(
                success = {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _sideEffects.emit(AuthSideEffect.SignInSuccess)
                },
                failure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, error = error) }
                }
            )
        }
    }

    private fun handleGoogleSignInError(throwable: Throwable?) {
        val mappedError = when (throwable) {
            is ApiException -> when (throwable.statusCode) {
                CommonStatusCodes.CANCELED -> null
                CommonStatusCodes.NETWORK_ERROR -> AppError.Network
                CommonStatusCodes.SIGN_IN_REQUIRED -> AppError.Unauthorized
                else -> AppError.Unknown
            }
            else -> AppError.Unknown
        }

        _uiState.update {
            if (mappedError == null) it.copy(isSubmitting = false)
            else it.copy(isSubmitting = false, error = mappedError)
        }
    }
}
