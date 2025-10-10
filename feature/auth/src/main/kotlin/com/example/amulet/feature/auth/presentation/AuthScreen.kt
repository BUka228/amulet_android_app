package com.example.amulet.feature.auth.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.button.AmuletButton
import com.example.amulet.core.design.components.button.ButtonVariant
import com.example.amulet.core.design.components.textfield.AmuletTextField
import com.example.amulet.core.design.foundation.theme.AmuletTheme
import com.example.amulet.shared.core.AppError
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AuthRoute(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val googleClient = remember(context) {
        val clientIdRes = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (clientIdRes == 0) {
            null
        } else {
            val clientId = context.getString(clientIdRes)
            if (clientId.isNullOrBlank()) {
                null
            } else {
                val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(clientId)
                    .requestEmail()
                    .build()
                GoogleSignIn.getClient(context, options)
            }
        }
    }

    val latestViewModel by rememberUpdatedState(viewModel)

    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            latestViewModel.handleEvent(AuthUiEvent.GoogleSignInCancelled)
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrBlank()) {
                latestViewModel.handleEvent(AuthUiEvent.GoogleIdTokenReceived(idToken))
            } else {
                latestViewModel.handleEvent(AuthUiEvent.GoogleSignInError(null))
            }
        } catch (apiException: ApiException) {
            if (apiException.statusCode == CommonStatusCodes.CANCELED) {
                latestViewModel.handleEvent(AuthUiEvent.GoogleSignInCancelled)
            } else {
                latestViewModel.handleEvent(AuthUiEvent.GoogleSignInError(apiException))
            }
        } catch (throwable: Throwable) {
            latestViewModel.handleEvent(AuthUiEvent.GoogleSignInError(throwable))
        }
    }

    LaunchedEffect(viewModel.sideEffects, googleClient) {
        viewModel.sideEffects.collectLatest { sideEffect ->
            when (sideEffect) {
                AuthSideEffect.SignInSuccess -> onAuthSuccess()
                AuthSideEffect.LaunchGoogleSignIn -> {
                    val client: GoogleSignInClient? = googleClient
                    if (client == null) {
                        viewModel.handleEvent(AuthUiEvent.GoogleSignInError(IllegalStateException("Google Sign-In not configured")))
                    } else {
                        googleLauncher.launch(client.signInIntent)
                    }
                }
            }
        }
    }

    AuthScreen(
        state = uiState,
        onEvent = viewModel::handleEvent,
        isGoogleSignInAvailable = googleClient != null
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
    val errorMessage = remember(state.error) { state.error?.let { it.toMessage() } }

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
