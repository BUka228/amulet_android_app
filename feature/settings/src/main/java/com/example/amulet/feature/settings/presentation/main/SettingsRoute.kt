package com.example.amulet.feature.settings.presentation.main

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.avatar.AmuletAvatar
import com.example.amulet.core.design.components.avatar.AvatarSize
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.settings.R
import com.example.amulet.shared.domain.practices.model.PracticeAudioMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    onOpenProfile: () -> Unit = {},
    onOpenDevices: () -> Unit = {},
    onOpenHugsSettings: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onSignedOut: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scaffoldState = LocalScaffoldState.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SettingsEffect.NavigateToProfile -> onOpenProfile()
                SettingsEffect.NavigateToDevices -> onOpenDevices()
                SettingsEffect.NavigateToHugsSettings -> onOpenHugsSettings()
                SettingsEffect.NavigateToPrivacy -> onOpenPrivacy()
                SettingsEffect.NavigateToAbout -> onOpenAbout()
                SettingsEffect.SignedOut -> onSignedOut()
                is SettingsEffect.ShowError -> {
                    // TODO: показать ошибку через Snackbar, когда будет инфраструктура
                }
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
                                text = stringResource(id = R.string.settings_title),
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        actions = {}
                    )
                },
                floatingActionButton = {}
            )
        }
    }

    SettingsScreen(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun SettingsScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ProfileHeader(state = state)
        }

        item {
            ProfileSection(state = state, onIntent = onIntent)
        }

        item {
            DevicesSection(onIntent = onIntent)
        }

        item {
            PracticesSection(state = state, onIntent = onIntent)
        }

        item {
            HugsSection(state = state, onIntent = onIntent)
        }

        item {
            PrivacySection(state = state, onIntent = onIntent)
        }

        item {
            AboutSection(onIntent = onIntent)
        }

        item {
            SignOutSection(onIntent = onIntent)
        }
    }
}

@Composable
private fun ProfileHeader(
    state: SettingsState,
) {
    val displayName = state.currentUser?.displayName
        ?: stringResource(id = R.string.settings_profile_fallback_name)
    val avatarUrl = state.currentUser?.avatarUrl.orEmpty()
    val hasAvatar = avatarUrl.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
    }
}

@Composable
private fun ProfileSection(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = stringResource(id = R.string.settings_profile_section_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            SettingsRow(
                title = stringResource(id = R.string.settings_profile_row_title),
                subtitle = state.currentUser?.displayName,
                onClick = { onIntent(SettingsIntent.OpenProfile) }
            )
        }
    }
}

@Composable
private fun DevicesSection(
    onIntent: (SettingsIntent) -> Unit,
) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Devices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(id = R.string.settings_devices_section_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            SettingsRow(
                title = stringResource(id = R.string.settings_devices_row_devices),
                onClick = { onIntent(SettingsIntent.OpenDevices) }
            )
        }
    }
}

@Composable
private fun PracticesSection(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Spa,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(id = R.string.settings_practices_section_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            // Интенсивность по умолчанию
            val initialIntensity = (state.preferences.defaultIntensity ?: 0.5).coerceIn(0.0, 1.0)
            var intensity by remember(initialIntensity) { mutableStateOf(initialIntensity) }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_practices_default_intensity),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "${"%.0f".format(intensity * 100)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Slider(
                    value = intensity.toFloat(),
                    onValueChange = { intensity = it.toDouble().coerceIn(0.0, 1.0) },
                    onValueChangeFinished = {
                        onIntent(SettingsIntent.SetDefaultIntensity(intensity))
                    }
                )
            }

            // Яркость по умолчанию
            val initialBrightness = (state.preferences.defaultBrightness ?: 0.5).coerceIn(0.0, 1.0)
            var brightness by remember(initialBrightness) { mutableStateOf(initialBrightness) }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_practices_default_brightness),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "${"%.0f".format(brightness * 100)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Slider(
                    value = brightness.toFloat(),
                    onValueChange = { brightness = it.toDouble().coerceIn(0.0, 1.0) },
                    onValueChangeFinished = {
                        onIntent(SettingsIntent.SetDefaultBrightness(brightness))
                    }
                )
            }

            // Аудио по умолчанию
            AudioModeSection(
                currentMode = state.preferences.defaultAudioMode,
                onIntent = onIntent,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AudioModeSection(
    currentMode: PracticeAudioMode?,
    onIntent: (SettingsIntent) -> Unit,
) {
    val mode = currentMode ?: PracticeAudioMode.GUIDE

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(id = R.string.settings_practices_default_audio),
            style = MaterialTheme.typography.bodyLarge,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AudioModeChip(
                label = stringResource(id = R.string.settings_practices_audio_voice),
                selected = mode == PracticeAudioMode.GUIDE,
                onClick = { onIntent(SettingsIntent.SetDefaultAudioMode(PracticeAudioMode.GUIDE)) },
            )
            AudioModeChip(
                label = stringResource(id = R.string.settings_practices_audio_music),
                selected = mode == PracticeAudioMode.SOUND_ONLY,
                onClick = { onIntent(SettingsIntent.SetDefaultAudioMode(PracticeAudioMode.SOUND_ONLY)) },
            )
            AudioModeChip(
                label = stringResource(id = R.string.settings_practices_audio_silent),
                selected = mode == PracticeAudioMode.SILENT,
                onClick = { onIntent(SettingsIntent.SetDefaultAudioMode(PracticeAudioMode.SILENT)) },
            )
        }
    }
}

@Composable
private fun AudioModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    AssistChip(onClick = onClick,
        label = {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
        ) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = background,
            labelColor = contentColor,
        )
    )
}

@Composable
private fun HugsSection(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(id = R.string.settings_hugs_section_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.settings_hugs_dnd_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Switch(
                    checked = state.preferences.hugsDndEnabled,
                    onCheckedChange = { onIntent(SettingsIntent.SetHugsDndEnabled(it)) }
                )
            }

            SettingsRow(
                title = stringResource(id = R.string.settings_hugs_details),
                onClick = { onIntent(SettingsIntent.OpenHugsSettings) }
            )
        }
    }
}

@Composable
private fun PrivacySection(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(id = R.string.settings_privacy_section_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            ConsentRow(
                title = stringResource(id = R.string.settings_privacy_analytics),
                checked = state.consents.analytics,
                onCheckedChange = { onIntent(SettingsIntent.SetAnalyticsConsent(it)) },
                icon = Icons.Filled.BarChart,
            )

            ConsentRow(
                title = stringResource(id = R.string.settings_privacy_marketing),
                checked = state.consents.marketing,
                onCheckedChange = { onIntent(SettingsIntent.SetMarketingConsent(it)) },
                icon = Icons.Filled.Campaign,
            )

            ConsentRow(
                title = stringResource(id = R.string.settings_privacy_notifications),
                checked = state.consents.notifications,
                onCheckedChange = { onIntent(SettingsIntent.SetNotificationsConsent(it)) },
                icon = Icons.Filled.Notifications,
            )

            SettingsRow(
                title = stringResource(id = R.string.settings_privacy_manage),
                onClick = { onIntent(SettingsIntent.OpenPrivacy) }
            )
        }
    }
}

@Composable
private fun AboutSection(
    onIntent: (SettingsIntent) -> Unit,
) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(id = R.string.settings_about_section_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            SettingsRow(
                title = stringResource(id = R.string.settings_about_row_title),
                onClick = { onIntent(SettingsIntent.OpenAbout) }
            )
        }
    }
}

@Composable
private fun SignOutSection(
    onIntent: (SettingsIntent) -> Unit,
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsRow(
                title = stringResource(id = R.string.settings_sign_out),
                titleColor = MaterialTheme.colorScheme.error,
                onClick = { showConfirmDialog = true }
            )

            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = {
                        Text(text = stringResource(id = R.string.settings_sign_out_confirm_title))
                    },
                    text = {
                        Text(text = stringResource(id = R.string.settings_sign_out_confirm_message))
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showConfirmDialog = false
                                onIntent(SettingsIntent.SignOutClicked)
                            }
                        ) {
                            Text(text = stringResource(id = R.string.settings_sign_out_confirm_positive))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog = false }) {
                            Text(text = stringResource(id = R.string.settings_sign_out_confirm_negative))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (onClick != null) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ConsentRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
