package com.example.amulet.feature.auth.presentation

import app.cash.turbine.test
import com.example.amulet.feature.auth.MainDispatcherRule
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.auth.model.UserCredentials
import com.example.amulet.shared.domain.auth.usecase.EnableGuestModeUseCase
import com.example.amulet.shared.domain.auth.usecase.SignInUseCase
import com.example.amulet.shared.domain.auth.usecase.SignInWithGoogleUseCase
import com.example.amulet.shared.domain.auth.usecase.SignUpUseCase
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var signInUseCase: SignInUseCase

    @MockK
    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase

    @MockK
    private lateinit var signUpUseCase: SignUpUseCase

    @MockK
    private lateinit var enableGuestModeUseCase: EnableGuestModeUseCase

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = AuthViewModel(signInUseCase, signInWithGoogleUseCase, signUpUseCase, enableGuestModeUseCase)
    }

    @Test
    fun `successful sign in emits side effect`() = runTest {
        val credentials = UserCredentials(email = "user@example.com", password = "secret")
        coEvery { signInUseCase.invoke(credentials) } returns Ok(Unit)

        viewModel.handleEvent(AuthUiEvent.EmailChanged("user@example.com"))
        viewModel.handleEvent(AuthUiEvent.PasswordChanged("secret"))

        viewModel.sideEffects.test {
            viewModel.handleEvent(AuthUiEvent.Submit)
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
            assertEquals(AuthSideEffect.SignInSuccess, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(!viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `successful sign up emits side effect`() = runTest {
        val credentials = UserCredentials(email = "new@example.com", password = "secret")
        coEvery { signUpUseCase.invoke(credentials) } returns Ok(Unit)

        viewModel.handleEvent(AuthUiEvent.AuthModeSwitchRequested)
        viewModel.handleEvent(AuthUiEvent.EmailChanged("new@example.com"))
        viewModel.handleEvent(AuthUiEvent.PasswordChanged("secret"))
        viewModel.handleEvent(AuthUiEvent.ConfirmPasswordChanged("secret"))

        viewModel.sideEffects.test {
            viewModel.handleEvent(AuthUiEvent.Submit)
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
            assertEquals(AuthSideEffect.SignInSuccess, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(!viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `blank credentials produce validation error`() = runTest {
        viewModel.handleEvent(AuthUiEvent.Submit)

        val state = viewModel.uiState.value
        assertTrue(state.error is AppError.Validation)
    }

    @Test
    fun `sign up with mismatched passwords produces validation error`() = runTest {
        viewModel.handleEvent(AuthUiEvent.AuthModeSwitchRequested)
        viewModel.handleEvent(AuthUiEvent.EmailChanged("new@example.com"))
        viewModel.handleEvent(AuthUiEvent.PasswordChanged("secret"))
        viewModel.handleEvent(AuthUiEvent.ConfirmPasswordChanged("different"))

        viewModel.handleEvent(AuthUiEvent.Submit)

        val state = viewModel.uiState.value
        assertTrue(state.error is AppError.Validation)
    }

    @Test
    fun `sign in failure exposes error`() = runTest {
        val credentials = UserCredentials(email = "user@example.com", password = "secret")
        coEvery { signInUseCase.invoke(credentials) } returns Err(AppError.Network)

        viewModel.handleEvent(AuthUiEvent.EmailChanged("user@example.com"))
        viewModel.handleEvent(AuthUiEvent.PasswordChanged("secret"))
        viewModel.handleEvent(AuthUiEvent.Submit)

        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(AppError.Network, viewModel.uiState.value.error)
    }

    @Test
    fun `google sign in request emits launch side effect`() = runTest {
        viewModel.sideEffects.test {
            viewModel.handleEvent(AuthUiEvent.GoogleSignInRequested)
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
            assertEquals(AuthSideEffect.LaunchGoogleSignIn, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `google sign in success emits success side effect`() = runTest {
        coEvery { signInWithGoogleUseCase.invoke("token", "rawNonce") } returns Ok(Unit)

        viewModel.handleEvent(AuthUiEvent.GoogleSignInRequested)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        viewModel.sideEffects.test {
            viewModel.handleEvent(AuthUiEvent.GoogleIdTokenReceived("token", "rawNonce"))
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
            assertEquals(AuthSideEffect.SignInSuccess, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(!viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `google sign in failure updates error`() = runTest {
        coEvery { signInWithGoogleUseCase.invoke("token", "rawNonce") } returns Err(AppError.Network)

        viewModel.handleEvent(AuthUiEvent.GoogleSignInRequested)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        viewModel.handleEvent(AuthUiEvent.GoogleIdTokenReceived("token", "rawNonce"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(AppError.Network, viewModel.uiState.value.error)
        assertTrue(!viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `google sign in error maps api exception`() = runTest {
        viewModel.handleEvent(AuthUiEvent.GoogleSignInRequested)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val exception = ApiException(Status(CommonStatusCodes.NETWORK_ERROR))
        viewModel.handleEvent(AuthUiEvent.GoogleSignInError(exception))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(AppError.Network, viewModel.uiState.value.error)
        assertTrue(!viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `google sign in cancelled clears submitting`() = runTest {
        viewModel.handleEvent(AuthUiEvent.GoogleSignInRequested)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        viewModel.handleEvent(AuthUiEvent.GoogleSignInCancelled)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(!viewModel.uiState.value.isSubmitting)
    }
}
