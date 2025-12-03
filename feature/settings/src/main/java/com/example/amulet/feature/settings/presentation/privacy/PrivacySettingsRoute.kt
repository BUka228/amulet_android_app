package com.example.amulet.feature.settings.presentation.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsRoute(
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: PrivacySettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scaffoldState = LocalScaffoldState.current
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(id = R.string.settings_privacy_section_title),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                },
                floatingActionButton = {},
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                },
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PrivacySettingsEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.error.toString(),
                        duration = SnackbarDuration.Long,
                    )
                }
                is PrivacySettingsEffect.ShowMessage -> {
                    val message = context.getString(effect.messageResId)
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short,
                    )
                }
                is PrivacySettingsEffect.SignedOut -> {
                    onAccountDeleted()
                }
            }
        }
    }

    PrivacySettingsScreen(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun PrivacySettingsScreen(
    state: PrivacySettingsState,
    onIntent: (PrivacySettingsIntent) -> Unit,
) {
    val scrollState = rememberScrollState()

    var showExportDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ConsentsSection(state = state, onIntent = onIntent)

        DataManagementSection(
            state = state,
            onRequestExport = { showExportDialog = true },
            onRequestDeletion = { showDeleteDialog = true },
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text(text = stringResource(id = R.string.settings_privacy_export_dialog_title))
            },
            text = {
                Text(
                    text = stringResource(id = R.string.settings_privacy_export_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportDialog = false
                        onIntent(PrivacySettingsIntent.RequestDataExport)
                    },
                    enabled = !state.isExportInProgress,
                ) {
                    Text(text = stringResource(id = R.string.settings_privacy_export_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text(text = stringResource(id = R.string.settings_privacy_dialog_cancel))
                }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            title = {
                Text(text = stringResource(id = R.string.settings_privacy_delete_dialog_title))
            },
            text = {
                Text(
                    text = stringResource(id = R.string.settings_privacy_delete_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onIntent(PrivacySettingsIntent.ConfirmAccountDeletion)
                    },
                    enabled = !state.isDeletionInProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(text = stringResource(id = R.string.settings_privacy_delete_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = stringResource(id = R.string.settings_privacy_dialog_cancel))
                }
            },
        )
    }
}

@Composable
private fun ConsentsSection(
    state: PrivacySettingsState,
    onIntent: (PrivacySettingsIntent) -> Unit,
) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Заголовок
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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

            // Analytics
            ConsentRow(
                title = stringResource(id = R.string.settings_privacy_analytics),
                description = stringResource(id = R.string.settings_privacy_analytics_desc),
                checked = state.consents.analytics,
                enabled = !state.isUpdatingConsents,
                onCheckedChange = { onIntent(PrivacySettingsIntent.SetAnalyticsConsent(it)) },
            )

            // Marketing / usage-like consent
            ConsentRow(
                title = stringResource(id = R.string.settings_privacy_marketing),
                description = stringResource(id = R.string.settings_privacy_marketing_desc),
                checked = state.consents.marketing,
                enabled = !state.isUpdatingConsents,
                onCheckedChange = { onIntent(PrivacySettingsIntent.SetMarketingConsent(it)) },
            )

            // Notifications / crash/diagnostics-like consent
            ConsentRow(
                title = stringResource(id = R.string.settings_privacy_notifications),
                description = stringResource(id = R.string.settings_privacy_notifications_desc),
                checked = state.consents.notifications,
                enabled = !state.isUpdatingConsents,
                onCheckedChange = { onIntent(PrivacySettingsIntent.SetNotificationsConsent(it)) },
            )
        }
    }
}

@Composable
private fun ConsentRow(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun DataManagementSection(
    state: PrivacySettingsState,
    onRequestExport: () -> Unit,
    onRequestDeletion: () -> Unit,
) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(id = R.string.settings_privacy_manage),
                style = MaterialTheme.typography.titleMedium,
            )

            // Export data
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(id = R.string.settings_privacy_export_title),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(id = R.string.settings_privacy_export_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onRequestExport,
                    enabled = !state.isExportInProgress,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.settings_privacy_export_button))
                }
            }

            // Delete account
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(id = R.string.settings_privacy_delete_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = stringResource(id = R.string.settings_privacy_delete_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onRequestDeletion,
                    enabled = !state.isDeletionInProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.settings_privacy_delete_button))
                }
            }
        }
    }
}

