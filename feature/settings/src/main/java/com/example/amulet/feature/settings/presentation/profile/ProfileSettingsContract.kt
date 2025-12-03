package com.example.amulet.feature.settings.presentation.profile

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.user.model.User

data class ProfileSettingsState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val currentUser: User? = null,
    val displayNameInput: String = "",
    val avatarUrlInput: String = "",
    val timezoneInput: String = "",
    val languageInput: String = "",
    val isSaving: Boolean = false,
)

sealed class ProfileSettingsIntent {
    object NavigateBack : ProfileSettingsIntent()

    data class DisplayNameChanged(val value: String) : ProfileSettingsIntent()
    data class AvatarChanged(val uri: String) : ProfileSettingsIntent()
    data class TimezoneChanged(val value: String) : ProfileSettingsIntent()
    data class LanguageChanged(val value: String) : ProfileSettingsIntent()

    object SaveClicked : ProfileSettingsIntent()
    object ChangePasswordClicked : ProfileSettingsIntent()
}

sealed class ProfileSettingsEffect {
    object NavigateBack : ProfileSettingsEffect()
    data class ShowError(val error: AppError) : ProfileSettingsEffect()
    object NavigateToChangePassword : ProfileSettingsEffect()
}
