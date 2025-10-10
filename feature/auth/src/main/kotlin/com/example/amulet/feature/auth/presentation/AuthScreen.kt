package com.example.amulet.feature.auth.presentation

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.amulet.core.design.components.button.AmuletButton
import com.example.amulet.core.design.components.button.ButtonVariant
import com.example.amulet.core.design.components.textfield.AmuletTextField
import com.example.amulet.core.design.foundation.theme.AmuletTheme
import com.example.amulet.shared.core.AppError
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun AuthRoute(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val googleConfig = remember(context) {
        val clientIdRes = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (clientIdRes == 0) {
            null
        } else {
            val clientId = context.getString(clientIdRes)
            if (clientId.isBlank()) {
                null
            } else {
                GoogleSignInConfig(
                    request = GetCredentialRequest.Builder()
                        .addCredentialOption(
                            GetGoogleIdOption.Builder()
                                .setServerClientId(clientId)
                                .setFilterByAuthorizedAccounts(false)
                                .setAutoSelectEnabled(false)
                                .build()
                        )
                        .build()
                )
            }
        }
    }

    val latestViewModel by rememberUpdatedState(viewModel)
    val latestGoogleConfig by rememberUpdatedState(googleConfig)

    LaunchedEffect(viewModel.sideEffects, googleConfig) {
        viewModel.sideEffects.collectLatest { sideEffect ->
            when (sideEffect) {
                AuthSideEffect.SignInSuccess -> onAuthSuccess()
                AuthSideEffect.LaunchGoogleSignIn -> {
                    val config = latestGoogleConfig
                    if (config == null) {
                        viewModel.handleEvent(AuthUiEvent.GoogleSignInError(IllegalStateException("Google Sign-In not configured")))
                    } else {
                        viewModel.viewModelScope.launch {
                            handleCredentialRequest(
                                context = context,
                                credentialManager = credentialManager,
                                request = config.request,
                                onResult = { response ->
                                    handleCredential(response.credential, latestViewModel)
                                },
                                onCancellation = {
                                    latestViewModel.handleEvent(AuthUiEvent.GoogleSignInCancelled)
                                },
                                onError = { throwable ->
                                    latestViewModel.handleEvent(AuthUiEvent.GoogleSignInError(throwable))
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    AuthScreen(
        state = uiState,
        onEvent = viewModel::handleEvent,
        isGoogleSignInAvailable = googleConfig != null
    )
}

@Composable
fun AuthScreen(
    state: AuthUiState,
    onEvent: (AuthUiEvent) -> Unit,
    isGoogleSignInAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    val spacing = AmuletTheme.spacing
    val errorMessage = remember(state.error) { state.error?.toMessage() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = spacing.xl, vertical = spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Вход в аккаунт",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(spacing.lg))

        AuthEmailField(
            value = state.email,
            onValueChange = { onEvent(AuthUiEvent.EmailChanged(it)) }
        )

        Spacer(Modifier.height(spacing.md))

        AuthPasswordField(
            value = state.password,
            onValueChange = { onEvent(AuthUiEvent.PasswordChanged(it)) },
            onSubmit = { onEvent(AuthUiEvent.Submit) }
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(spacing.sm))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(spacing.lg))

        AmuletButton(
            text = "Войти",
            onClick = { onEvent(AuthUiEvent.Submit) },
            loading = state.isSubmitting,
            enabled = !state.isSubmitting,
            variant = ButtonVariant.Primary
        )

        Spacer(Modifier.height(spacing.md))

        AmuletButton(
            text = "Войти через Google",
            onClick = { onEvent(AuthUiEvent.GoogleSignInRequested) },
            loading = state.isSubmitting,
            enabled = !state.isSubmitting && isGoogleSignInAvailable,
            variant = ButtonVariant.Outline
        )

        if (!isGoogleSignInAvailable) {
            Spacer(Modifier.height(spacing.sm))
            Text(
                text = "Google Sign-In недоступен",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AuthEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AmuletTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = "Email",
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
private fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    val visualTransformation: VisualTransformation =
        if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()

    AmuletTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = "Пароль",
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        visualTransformation = visualTransformation,
        trailingIconContent = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = null
                )
            }
        }
    )
}

private fun AppError.toMessage(): String = when (this) {
    is AppError.Validation -> this.errors.values.firstOrNull() ?: "Некорректные данные"
    AppError.Unauthorized -> "Неверный email или пароль"
    AppError.Forbidden -> "Нет доступа"
    AppError.Network, AppError.Timeout -> "Проблемы с подключением к сети"
    is AppError.Server -> "Ошибка сервера (${this.code})"
    AppError.RateLimited -> "Слишком много попыток. Попробуйте позже"
    is AppError.PreconditionFailed -> this.reason ?: "Невозможно выполнить запрос"
    AppError.NotFound -> "Пользователь не найден"
    AppError.Conflict, is AppError.VersionConflict -> "Конфликт данных, повторите попытку"
    AppError.DatabaseError -> "Ошибка локального хранилища"
    is AppError.BleError -> "Ошибка подключения"
    is AppError.OtaError -> "Ошибка обновления"
    AppError.Unknown -> "Неизвестная ошибка"
}

private data class GoogleSignInConfig(
    val request: GetCredentialRequest
)

private suspend fun handleCredentialRequest(
    context: Context,
    credentialManager: CredentialManager,
    request: GetCredentialRequest,
    onResult: (GetCredentialResponse) -> Unit,
    onCancellation: () -> Unit,
    onError: (Throwable) -> Unit
) {
    try {
        val result = credentialManager.getCredential(context, request)
        onResult(result)
    } catch (cancellation: GetCredentialCancellationException) {
        onCancellation()
    } catch (interruption: GetCredentialInterruptedException) {
        onError(interruption)
    } catch (noCredential: NoCredentialException) {
        onError(noCredential)
    } catch (exception: GetCredentialException) {
        onError(exception)
    } catch (throwable: Throwable) {
        onError(throwable)
    }
}

private fun handleCredential(credential: Credential, viewModel: AuthViewModel) {
    when (credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val token = googleCredential.idToken
                if (!token.isNullOrBlank()) {
                    viewModel.handleEvent(AuthUiEvent.GoogleIdTokenReceived(token))
                } else {
                    viewModel.handleEvent(AuthUiEvent.GoogleSignInError(null))
                }
            } else {
                viewModel.handleEvent(AuthUiEvent.GoogleSignInError(IllegalStateException("Unsupported credential type")))
            }
        }
        else -> viewModel.handleEvent(AuthUiEvent.GoogleSignInError(IllegalStateException("Unsupported credential type")))
    }
}
