package com.example.amulet.feature.settings.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.user.model.UpdateUserProfileRequest
import com.example.amulet.shared.domain.user.usecase.ObserveCurrentUserUseCase
import com.example.amulet.shared.domain.user.usecase.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileSettingsState())
    val state: StateFlow<ProfileSettingsState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ProfileSettingsEffect>()
    val effects = _effects.asSharedFlow()

    init {
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            observeCurrentUserUseCase()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = _state.value.currentUser,
                )
                .collect { user ->
                    _state.update { current ->
                        val newDisplayName =
                            if (current.displayNameInput.isEmpty()) user?.displayName.orEmpty() else current.displayNameInput
                        val newAvatarUrl =
                            if (current.avatarUrlInput.isEmpty()) user?.avatarUrl.orEmpty() else current.avatarUrlInput
                        val newTimezone =
                            if (current.timezoneInput.isEmpty()) user?.timezone.orEmpty() else current.timezoneInput
                        val newLanguage =
                            if (current.languageInput.isEmpty()) user?.language.orEmpty() else current.languageInput

                        current.copy(
                            isLoading = false,
                            currentUser = user,
                            displayNameInput = newDisplayName,
                            avatarUrlInput = newAvatarUrl,
                            timezoneInput = newTimezone,
                            languageInput = newLanguage,
                        )
                    }
                }
        }
    }

    fun onIntent(intent: ProfileSettingsIntent) {
        when (intent) {
            ProfileSettingsIntent.NavigateBack -> emitEffect(ProfileSettingsEffect.NavigateBack)

            is ProfileSettingsIntent.DisplayNameChanged ->
                _state.update { it.copy(displayNameInput = intent.value) }

            is ProfileSettingsIntent.AvatarChanged ->
                _state.update { it.copy(avatarUrlInput = intent.uri) }

            is ProfileSettingsIntent.TimezoneChanged ->
                _state.update { it.copy(timezoneInput = intent.value) }

            is ProfileSettingsIntent.LanguageChanged ->
                _state.update { it.copy(languageInput = intent.value) }

            ProfileSettingsIntent.SaveClicked -> saveProfile()
            ProfileSettingsIntent.ChangePasswordClicked ->
                emitEffect(ProfileSettingsEffect.NavigateToChangePassword)
        }
    }

    private fun saveProfile() {
        val currentState = _state.value
        val user = currentState.currentUser ?: return

        val trimmedName = currentState.displayNameInput.trim()
        val trimmedAvatarUrl = currentState.avatarUrlInput.trim()
        val trimmedTimezone = currentState.timezoneInput.trim()
        val trimmedLanguage = currentState.languageInput.trim()

        val request = UpdateUserProfileRequest(
            displayName = trimmedName.takeIf { it != (user.displayName ?: "") },
            avatarUrl = trimmedAvatarUrl.takeIf { it != (user.avatarUrl ?: "") },
            timezone = trimmedTimezone.takeIf { it != (user.timezone ?: "") },
            language = trimmedLanguage.takeIf { it != (user.language ?: "") },
        )

        if (
            request.displayName == null &&
            request.avatarUrl == null &&
            request.timezone == null &&
            request.language == null
        ) {
            emitEffect(ProfileSettingsEffect.NavigateBack)
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val result = updateUserProfileUseCase(request)
            _state.update { it.copy(isSaving = false) }
            handleResult(result)
        }
    }

    private fun handleResult(result: AppResult<*>) {
        val error = result.component2()
        if (error != null) {
            emitEffect(ProfileSettingsEffect.ShowError(error))
        } else {
            emitEffect(ProfileSettingsEffect.NavigateBack)
        }
    }

    private fun emitEffect(effect: ProfileSettingsEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
