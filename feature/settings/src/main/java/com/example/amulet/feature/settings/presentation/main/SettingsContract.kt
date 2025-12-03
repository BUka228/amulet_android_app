package com.example.amulet.feature.settings.presentation.main

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.practices.model.PracticeAudioMode
import com.example.amulet.shared.domain.user.model.UserPreferences
import com.example.amulet.shared.domain.privacy.model.UserConsents
import com.example.amulet.shared.domain.user.model.User

data class SettingsState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val currentUser: User? = null,
    val preferences: UserPreferences = UserPreferences(),
    val consents: UserConsents = UserConsents(),
    val isSignOutInProgress: Boolean = false,
)

sealed class SettingsIntent {
    object Refresh : SettingsIntent()

    object OpenProfile : SettingsIntent()
    object OpenDevices : SettingsIntent()
    object OpenHugsSettings : SettingsIntent()
    object OpenPrivacy : SettingsIntent()

    object OpenAbout : SettingsIntent()

    object SignOutClicked : SettingsIntent()

    data class SetHugsDndEnabled(val enabled: Boolean) : SettingsIntent()

    data class SetDefaultIntensity(val value: Double) : SettingsIntent()
    data class SetDefaultBrightness(val value: Double) : SettingsIntent()
    data class SetDefaultAudioMode(val mode: PracticeAudioMode) : SettingsIntent()

    data class SetAnalyticsConsent(val enabled: Boolean) : SettingsIntent()
    data class SetMarketingConsent(val enabled: Boolean) : SettingsIntent()
    data class SetNotificationsConsent(val enabled: Boolean) : SettingsIntent()
}

sealed class SettingsEffect {
    object NavigateToProfile : SettingsEffect()
    object NavigateToDevices : SettingsEffect()
    object NavigateToHugsSettings : SettingsEffect()
    object NavigateToPrivacy : SettingsEffect()
    object NavigateToAbout : SettingsEffect()

    object SignedOut : SettingsEffect()

    data class ShowError(val error: AppError) : SettingsEffect()
}
