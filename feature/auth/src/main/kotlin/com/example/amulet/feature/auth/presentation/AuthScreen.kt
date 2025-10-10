package com.example.amulet.feature.auth.presentation

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
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
import com.example.amulet.feature.auth.R
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
    val errorMessage = state.error?.toMessage()
    val titleRes = if (state.authMode == AuthMode.SignIn) R.string.auth_title_sign_in else R.string.auth_title_sign_up
    val primaryButtonTextRes = if (state.authMode == AuthMode.SignIn) R.string.auth_sign_in else R.string.auth_sign_up
    val switchTextRes = if (state.authMode == AuthMode.SignIn) R.string.auth_switch_to_sign_up else R.string.auth_switch_to_sign_in

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.xl, vertical = spacing.lg),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = spacing.xl, vertical = spacing.lg),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
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

                if (state.authMode == AuthMode.SignUp) {
                    Spacer(Modifier.height(spacing.md))
                    AuthConfirmPasswordField(
                        value = state.confirmPassword,
                        onValueChange = { onEvent(AuthUiEvent.ConfirmPasswordChanged(it)) },
                        onSubmit = { onEvent(AuthUiEvent.Submit) }
                    )
                }

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
                    text = stringResource(primaryButtonTextRes),
                        onClick = { onEvent(AuthUiEvent.Submit) },
                        loading = state.isSubmitting,
                        enabled = !state.isSubmitting,
                        variant = ButtonVariant.Primary
                    )

                if (state.authMode == AuthMode.SignIn) {
                    Spacer(Modifier.height(spacing.md))

                    AmuletButton(
                        text = stringResource(R.string.auth_google_sign_in),
                        onClick = { onEvent(AuthUiEvent.GoogleSignInRequested) },
                        loading = state.isSubmitting,
                        enabled = !state.isSubmitting && isGoogleSignInAvailable,
                        variant = ButtonVariant.Outline
                    )

                    if (!isGoogleSignInAvailable) {
                        Spacer(Modifier.height(spacing.sm))
                        Text(
                            text = stringResource(R.string.auth_google_unavailable),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(spacing.sm))

                AmuletButton(
                    text = stringResource(switchTextRes),
                    onClick = { onEvent(AuthUiEvent.AuthModeSwitchRequested) },
                    enabled = !state.isSubmitting,
                    variant = ButtonVariant.Text,
                    fullWidth = false
                )
                }
            }
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
        label = stringResource(R.string.auth_email_label),
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
        label = stringResource(R.string.auth_password_label),
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

@Composable
private fun AuthConfirmPasswordField(
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
        label = stringResource(R.string.auth_confirm_password_label),
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

@Composable
private fun AppError.toMessage(): String = when (this) {
    is AppError.Validation -> this.errors.values.firstOrNull() ?: stringResource(R.string.auth_error_validation_default)
    AppError.Unauthorized -> stringResource(R.string.auth_error_credentials)
    AppError.Forbidden -> stringResource(R.string.auth_error_forbidden)
    AppError.Network -> stringResource(R.string.auth_error_network)
    AppError.Timeout -> stringResource(R.string.auth_error_timeout)
    is AppError.Server -> stringResource(R.string.auth_error_server, this.code)
    AppError.RateLimited -> stringResource(R.string.auth_error_rate_limited)
    is AppError.PreconditionFailed -> this.reason ?: stringResource(R.string.auth_error_precondition)
    AppError.NotFound -> stringResource(R.string.auth_error_not_found)
    AppError.Conflict, is AppError.VersionConflict -> stringResource(R.string.auth_error_conflict)
    AppError.DatabaseError -> stringResource(R.string.auth_error_database)
    is AppError.BleError -> stringResource(R.string.auth_error_ble)
    is AppError.OtaError -> stringResource(R.string.auth_error_ota)
    AppError.Unknown -> stringResource(R.string.auth_error_unknown)
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

@Preview(name = "Auth screen - light", showBackground = true)
@Composable
private fun AuthScreenPreviewLight() {
    AmuletTheme(darkTheme = false) {
        AuthScreen(
            state = AuthUiState(),
            onEvent = {},
            isGoogleSignInAvailable = true
        )
    }
}

@Preview(name = "Auth screen - dark", showBackground = true)
@Composable
private fun AuthScreenPreviewDark() {
    AmuletTheme(darkTheme = true) {
        AuthScreen(
            state = AuthUiState(),
            onEvent = {},
            isGoogleSignInAvailable = true
        )
    }
}

@Preview(name = "Auth screen - sign up", showBackground = true)
@Composable
private fun AuthScreenPreviewSignUp() {
    AmuletTheme(darkTheme = false) {
        AuthScreen(
            state = AuthUiState(authMode = AuthMode.SignUp),
            onEvent = {},
            isGoogleSignInAvailable = true
        )
    }
}
