package com.example.amulet.feature.settings.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.auth.usecase.SignOutUseCase
import com.example.amulet.shared.domain.hugs.SetHugsDndEnabledUseCase
import com.example.amulet.shared.domain.practices.model.PracticeAudioMode
import com.example.amulet.shared.domain.practices.usecase.GetUserPreferencesStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.UpdatePracticeDefaultsUseCase
import com.example.amulet.shared.domain.privacy.model.UserConsents
import com.example.amulet.shared.domain.privacy.usecase.GetUserConsentsUseCase
import com.example.amulet.shared.domain.privacy.usecase.UpdateUserConsentsUseCase
import com.example.amulet.shared.domain.user.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getUserPreferencesStreamUseCase: GetUserPreferencesStreamUseCase,
    private val updatePracticeDefaultsUseCase: UpdatePracticeDefaultsUseCase,
    private val setHugsDndEnabledUseCase: SetHugsDndEnabledUseCase,
    private val getUserConsentsUseCase: GetUserConsentsUseCase,
    private val updateUserConsentsUseCase: UpdateUserConsentsUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SettingsEffect>()
    val effects = _effects.asSharedFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            combine(
                observeCurrentUserUseCase(),
                getUserPreferencesStreamUseCase(),
                getUserConsentsUseCase(),
            ) { user, prefs, consents ->
                Triple(user, prefs, consents)
            }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = Triple(null, _state.value.preferences, _state.value.consents)
                )
                .collect { (user, prefs, consents) ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            preferences = prefs,
                            consents = consents,
                        )
                    }
                }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.Refresh -> refresh()

            SettingsIntent.OpenProfile -> emitEffect(SettingsEffect.NavigateToProfile)
            SettingsIntent.OpenDevices -> emitEffect(SettingsEffect.NavigateToDevices)
            SettingsIntent.OpenHugsSettings -> emitEffect(SettingsEffect.NavigateToHugsSettings)
            SettingsIntent.OpenPrivacy -> emitEffect(SettingsEffect.NavigateToPrivacy)
            SettingsIntent.OpenAbout -> emitEffect(SettingsEffect.NavigateToAbout)

            SettingsIntent.SignOutClicked -> signOut()

            is SettingsIntent.SetHugsDndEnabled -> setHugsDnd(intent.enabled)

            is SettingsIntent.SetDefaultIntensity -> updatePracticeDefaults(intensity = intent.value)
            is SettingsIntent.SetDefaultBrightness -> updatePracticeDefaults(brightness = intent.value)
            is SettingsIntent.SetDefaultAudioMode -> updatePracticeDefaults(audioMode = intent.mode)

            is SettingsIntent.SetAnalyticsConsent -> updateConsents { it.copy(analytics = intent.enabled) }
            is SettingsIntent.SetMarketingConsent -> updateConsents { it.copy(marketing = intent.enabled) }
            is SettingsIntent.SetNotificationsConsent -> updateConsents { it.copy(notifications = intent.enabled) }
        }
    }

    private fun refresh() {
        // На текущем этапе все данные приходят из постоянных стримов, поэтому Refresh можно оставить пустым
    }

    private fun setHugsDnd(enabled: Boolean) {
        viewModelScope.launch {
            val result = setHugsDndEnabledUseCase(enabled)
            val error = result.component2()
            if (error != null) {
                emitEffect(SettingsEffect.ShowError(error))
            } else {
                _state.update { current ->
                    current.copy(
                        preferences = current.preferences.copy(hugsDndEnabled = enabled)
                    )
                }
            }
        }
    }

    private fun updatePracticeDefaults(
        intensity: Double? = null,
        brightness: Double? = null,
        audioMode: PracticeAudioMode? = null,
    ) {
        viewModelScope.launch {
            val result = updatePracticeDefaultsUseCase(
                defaultIntensity = intensity,
                defaultBrightness = brightness,
                defaultAudioMode = audioMode,
            )
            handleResult(result)
        }
    }

    private fun updateConsents(transform: (UserConsents) -> UserConsents) {
        viewModelScope.launch {
            val current = _state.value.consents
            val updated = transform(current)
            val result = updateUserConsentsUseCase(updated)
            handleResult(result)
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _state.update { it.copy(isSignOutInProgress = true) }
            val result = signOutUseCase()
            _state.update { it.copy(isSignOutInProgress = false) }
            val error = result.component2()
            if (error != null) {
                emitEffect(SettingsEffect.ShowError(error))
            } else {
                emitEffect(SettingsEffect.SignedOut)
            }
        }
    }

    private fun handleResult(result: AppResult<Unit>) {
        val error = result.component2()
        if (error != null) {
            emitEffect(SettingsEffect.ShowError(error))
        }
    }

    private fun emitEffect(effect: SettingsEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
