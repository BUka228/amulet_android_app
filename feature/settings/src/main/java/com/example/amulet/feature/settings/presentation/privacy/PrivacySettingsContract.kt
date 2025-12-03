package com.example.amulet.feature.settings.presentation.privacy

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.privacy.model.UserConsents

data class PrivacySettingsState(
    val isLoading: Boolean = true,
    val consents: UserConsents = UserConsents(),
    val isUpdatingConsents: Boolean = false,
    val isExportInProgress: Boolean = false,
    val isDeletionInProgress: Boolean = false,
)

sealed class PrivacySettingsIntent {
    object Refresh : PrivacySettingsIntent()

    data class SetAnalyticsConsent(val enabled: Boolean) : PrivacySettingsIntent()
    data class SetMarketingConsent(val enabled: Boolean) : PrivacySettingsIntent()
    data class SetNotificationsConsent(val enabled: Boolean) : PrivacySettingsIntent()

    object RequestDataExport : PrivacySettingsIntent()
    object ConfirmAccountDeletion : PrivacySettingsIntent()
}

sealed class PrivacySettingsEffect {
    data class ShowError(val error: AppError) : PrivacySettingsEffect()
    data class ShowMessage(val messageResId: Int) : PrivacySettingsEffect()
    object SignedOut : PrivacySettingsEffect()
}
