package com.example.amulet.feature.devices.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.devices.model.ConnectionStatus
import com.example.amulet.shared.domain.devices.model.Device
import com.example.amulet.shared.domain.devices.usecase.ObserveConnectionStateUseCase
import com.example.amulet.shared.domain.devices.usecase.ObserveDevicesUseCase
import com.example.amulet.shared.domain.devices.usecase.SyncDevicesUseCase
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevicesListViewModel @Inject constructor(
    private val observeDevicesUseCase: ObserveDevicesUseCase,
    private val syncDevicesUseCase: SyncDevicesUseCase,
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevicesListState())
    val uiState: StateFlow<DevicesListState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<DevicesListSideEffect>()
    val sideEffect: SharedFlow<DevicesListSideEffect> = _sideEffect.asSharedFlow()

    init {
        observeDevices()
        observeConnectionState()
        syncDevices()
    }

    fun handleEvent(event: DevicesListEvent) {
        when (event) {
            is DevicesListEvent.Refresh -> syncDevices()
            is DevicesListEvent.DeviceClicked -> {
                viewModelScope.launch {
                    _sideEffect.emit(DevicesListSideEffect.NavigateToDeviceDetails(event.deviceId))
                }
            }
            is DevicesListEvent.AddDeviceClicked -> {
                viewModelScope.launch {
                    _sideEffect.emit(DevicesListSideEffect.NavigateToPairing)
                }
            }
            is DevicesListEvent.DismissError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun observeDevices() {
        observeDevicesUseCase()
            .onEach { devices ->
                _uiState.update { 
                    it.copy(
                        devices = devices,
                        isLoading = false,
                        isEmpty = devices.isEmpty()
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeConnectionState() {
        observeConnectionStateUseCase()
            .onEach { connectionStatus ->
                _uiState.update { it.copy(connectionStatus = connectionStatus) }
            }
            .launchIn(viewModelScope)
    }

    private fun syncDevices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            syncDevicesUseCase()
                .onSuccess {
                    _uiState.update { it.copy(isRefreshing = false, error = null) }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(isRefreshing = false, error = error)
                    }
                }
        }
    }
}

data class DevicesListState(
    val devices: List<Device> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isEmpty: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val error: AppError? = null
)

sealed interface DevicesListEvent {
    data object Refresh : DevicesListEvent
    data class DeviceClicked(val deviceId: String) : DevicesListEvent
    data object AddDeviceClicked : DevicesListEvent
    data object DismissError : DevicesListEvent
}

sealed interface DevicesListSideEffect {
    data class NavigateToDeviceDetails(val deviceId: String) : DevicesListSideEffect
    data object NavigateToPairing : DevicesListSideEffect
}
