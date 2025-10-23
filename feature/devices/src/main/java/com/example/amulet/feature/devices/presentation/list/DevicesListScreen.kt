package com.example.amulet.feature.devices.presentation.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.feature.devices.R
import com.example.amulet.feature.devices.presentation.components.DeviceCard
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DevicesListRoute(
    viewModel: DevicesListViewModel = hiltViewModel(),
    onNavigateToPairing: () -> Unit,
    onNavigateToDeviceDetails: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is DevicesListSideEffect.NavigateToDeviceDetails -> {
                    onNavigateToDeviceDetails(effect.deviceId)
                }
                is DevicesListSideEffect.NavigateToPairing -> {
                    onNavigateToPairing()
                }
            }
        }
    }

    DevicesListScreen(
        state = uiState,
        onEvent = viewModel::handleEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesListScreen(
    state: DevicesListState,
    onEvent: (DevicesListEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.devices_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(DevicesListEvent.AddDeviceClicked) }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.devices_list_add_button))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.isEmpty -> {
                    EmptyDevicesState(
                        onAddDevice = { onEvent(DevicesListEvent.AddDeviceClicked) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.devices,
                            key = { it.id.value }
                        ) { device ->
                            DeviceCard(
                                device = device,
                                onClick = { onEvent(DevicesListEvent.DeviceClicked(device.id.value)) }
                            )
                        }
                    }
                }
            }

            // Error snackbar
            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { onEvent(DevicesListEvent.DismissError) }) {
                            Text(stringResource(R.string.common_ok))
                        }
                    }
                ) {
                    Text(error.toString())
                }
            }
        }
    }
}

@Composable
fun EmptyDevicesState(
    onAddDevice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.devices_list_empty_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.devices_list_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onAddDevice) {
            Text(stringResource(R.string.devices_list_add_button))
        }
    }
}
