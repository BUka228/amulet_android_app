package com.example.amulet.feature.settings.presentation.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.feature.settings.R
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.auth.usecase.SignOutUseCase
import com.example.amulet.shared.domain.privacy.model.UserConsents
import com.example.amulet.shared.domain.privacy.usecase.GetUserConsentsUseCase
import com.example.amulet.shared.domain.privacy.usecase.RequestAccountDeletionUseCase
import com.example.amulet.shared.domain.privacy.usecase.RequestDataExportUseCase
import com.example.amulet.shared.domain.privacy.usecase.UpdateUserConsentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PrivacySettingsViewModel @Inject constructor(
    private val getUserConsentsUseCase: GetUserConsentsUseCase,
    private val updateUserConsentsUseCase: UpdateUserConsentsUseCase,
    private val requestDataExportUseCase: RequestDataExportUseCase,
    private val requestAccountDeletionUseCase: RequestAccountDeletionUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PrivacySettingsState())
    val state: StateFlow<PrivacySettingsState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<PrivacySettingsEffect>()
    val effects = _effects.asSharedFlow()

    init {
        observeConsents()
    }

    private fun observeConsents() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getUserConsentsUseCase()
                .collect { consents ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            consents = consents,
                        )
                    }
                }
        }
    }

    fun onIntent(intent: PrivacySettingsIntent) {
        when (intent) {
            PrivacySettingsIntent.Refresh -> observeConsents()

            is PrivacySettingsIntent.SetAnalyticsConsent ->
                updateConsents { it.copy(analytics = intent.enabled) }

            is PrivacySettingsIntent.SetMarketingConsent ->
                updateConsents { it.copy(marketing = intent.enabled) }

            is PrivacySettingsIntent.SetNotificationsConsent ->
                updateConsents { it.copy(notifications = intent.enabled) }

            PrivacySettingsIntent.RequestDataExport -> requestDataExport()
            PrivacySettingsIntent.ConfirmAccountDeletion -> requestAccountDeletion()
        }
    }

    private fun updateConsents(transform: (UserConsents) -> UserConsents) {
        viewModelScope.launch {
            val current = _state.value.consents
            val updated = transform(current)
            _state.update { it.copy(isUpdatingConsents = true) }
            val result = updateUserConsentsUseCase(updated)
            _state.update { it.copy(isUpdatingConsents = false) }
            handleResult(result, onSuccessMessage = R.string.settings_privacy_consents_saved)
        }
    }

    private fun requestDataExport() {
        viewModelScope.launch {
            _state.update { it.copy(isExportInProgress = true) }
            val result = requestDataExportUseCase()
            _state.update { it.copy(isExportInProgress = false) }
            handleResult(result, onSuccessMessage = R.string.settings_privacy_export_requested)
        }
    }

    private fun requestAccountDeletion() {
        viewModelScope.launch {
            _state.update { it.copy(isDeletionInProgress = true) }
            val deletionResult = requestAccountDeletionUseCase()
            val deletionError = deletionResult.component2()
            if (deletionError != null) {
                _state.update { it.copy(isDeletionInProgress = false) }
                emitEffect(PrivacySettingsEffect.ShowError(deletionError))
                return@launch
            }

            val signOutResult = signOutUseCase()
            _state.update { it.copy(isDeletionInProgress = false) }
            val signOutError = signOutResult.component2()
            if (signOutError != null) {
                emitEffect(PrivacySettingsEffect.ShowError(signOutError))
            } else {
                emitEffect(PrivacySettingsEffect.SignedOut)
            }
        }
    }

    private fun handleResult(result: AppResult<Unit>, onSuccessMessage: Int) {
        val error = result.component2()
        if (error != null) {
            emitEffect(PrivacySettingsEffect.ShowError(error))
        } else {
            emitEffect(PrivacySettingsEffect.ShowMessage(onSuccessMessage))
        }
    }

    private fun emitEffect(effect: PrivacySettingsEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
