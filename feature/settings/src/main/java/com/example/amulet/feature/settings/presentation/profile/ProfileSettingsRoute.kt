package com.example.amulet.feature.settings.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.shape.CircleShape
import com.example.amulet.core.design.components.avatar.AmuletAvatar
import com.example.amulet.core.design.components.avatar.AvatarSize
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.core.design.components.textfield.AmuletTextField
import com.example.amulet.feature.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsRoute(
    onNavigateBack: () -> Unit,
    onChangePassword: () -> Unit,
    viewModel: ProfileSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scaffoldState = LocalScaffoldState.current

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selected ->
            viewModel.onIntent(ProfileSettingsIntent.AvatarChanged(selected.toString()))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProfileSettingsEffect.NavigateBack -> onNavigateBack()
                is ProfileSettingsEffect.ShowError -> {
                    // TODO: показать ошибку через Snackbar, когда будет инфраструктура
                }
                ProfileSettingsEffect.NavigateToChangePassword -> onChangePassword()
            }
        }
    }

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(id = R.string.settings_profile_section_title),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.onIntent(ProfileSettingsIntent.NavigateBack) }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                },
                floatingActionButton = {
                    SaveFab(
                        state = state,
                        onIntent = viewModel::onIntent,
                    )
                }
            )
        }
    }

    ProfileSettingsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onChangeAvatarClick = { avatarPickerLauncher.launch("image/*") },
    )
}

@Composable
private fun ProfileSettingsScreen(
    state: ProfileSettingsState,
    onIntent: (ProfileSettingsIntent) -> Unit,
    onChangeAvatarClick: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ProfileHeader(
            state = state,
            onChangeAvatarClick = onChangeAvatarClick,
        )

        ProfileMainInfoSection(
            state = state,
            onIntent = onIntent,
        )

        AccountManagementSection(onIntent = onIntent)
    }
}

@Composable
private fun AccountManagementSection(
    onIntent: (ProfileSettingsIntent) -> Unit,
) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(id = R.string.settings_profile_account_section_title),
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = stringResource(id = R.string.settings_profile_change_password),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIntent(ProfileSettingsIntent.ChangePasswordClicked) },
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    state: ProfileSettingsState,
    onChangeAvatarClick: () -> Unit,
) {
    val displayName = state.displayNameInput.ifBlank {
        stringResource(id = R.string.settings_profile_fallback_name)
    }

    val avatarUrl = state.avatarUrlInput.ifBlank { state.currentUser?.avatarUrl.orEmpty() }
    val hasAvatar = avatarUrl.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (hasAvatar) {
            AmuletAvatar(
                imageUrl = avatarUrl,
                initials = displayName.takeIf { it.isNotBlank() },
                size = AvatarSize.ExtraLarge,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                )
            }
        }

        Text(
            text = displayName,
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = stringResource(id = R.string.settings_profile_change_avatar),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onChangeAvatarClick),
        )
    }
}

@Composable
private fun ProfileMainInfoSection(
    state: ProfileSettingsState,
    onIntent: (ProfileSettingsIntent) -> Unit,
) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(id = R.string.settings_profile_section_title),
                style = MaterialTheme.typography.titleMedium,
            )

            AmuletTextField(
                value = state.displayNameInput,
                onValueChange = { onIntent(ProfileSettingsIntent.DisplayNameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(id = R.string.settings_profile_name_label),
                singleLine = true,
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(id = R.string.settings_profile_email_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = state.currentUser?.email
                        ?: stringResource(id = R.string.settings_profile_email_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            ProfileTimezoneRow(state = state, onIntent = onIntent)

            ProfileLanguageRow(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun ProfileTimezoneRow(
    state: ProfileSettingsState,
    onIntent: (ProfileSettingsIntent) -> Unit,
) {
    val options = listOf("UTC", "Europe/Moscow")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(id = R.string.settings_profile_timezone_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { tz ->
                val selected = state.timezoneInput.ifBlank { "UTC" } == tz
                androidx.compose.material3.AssistChip(
                    onClick = { onIntent(ProfileSettingsIntent.TimezoneChanged(tz)) },
                    label = {
                        Text(
                            text = when (tz) {
                                "Europe/Moscow" -> "Europe/Moscow"
                                else -> "UTC"
                            },
                        )
                    },
                    colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ProfileLanguageRow(
    state: ProfileSettingsState,
    onIntent: (ProfileSettingsIntent) -> Unit,
) {
    val options = listOf("en", "ru")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(id = R.string.settings_profile_language_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { code ->
                val selected = state.languageInput.ifBlank { "en" } == code
                androidx.compose.material3.AssistChip(
                    onClick = { onIntent(ProfileSettingsIntent.LanguageChanged(code)) },
                    label = {
                        Text(
                            text = when (code) {
                                "ru" -> "Русский"
                                else -> "English"
                            },
                        )
                    },
                    colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

@Composable
private fun SaveFab(
    state: ProfileSettingsState,
    onIntent: (ProfileSettingsIntent) -> Unit,
) {
    val currentUser = state.currentUser
    val isDirty = currentUser != null && (
        state.displayNameInput != (currentUser.displayName ?: "") ||
            state.avatarUrlInput.isNotBlank() ||
            state.timezoneInput != (currentUser.timezone ?: "") ||
            state.languageInput != (currentUser.language ?: "")
        )

    val canSave = !state.isSaving && isDirty

    FloatingActionButton(
        onClick = {
            if (canSave) {
                onIntent(ProfileSettingsIntent.SaveClicked)
            }
        },
        modifier = Modifier.alpha(if (canSave) 1f else 0.5f),
    ) {
        if (state.isSaving) {
            CircularProgressIndicator(
                modifier = Modifier
                    .height(20.dp)
                    .width(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
            )
        }
    }
}
