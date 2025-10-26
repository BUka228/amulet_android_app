package com.example.amulet.feature.devices.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.devices.usecase.ObserveConnectionStateUseCase
import com.example.amulet.shared.domain.devices.usecase.ObserveDevicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevicesListViewModel @Inject constructor(
    private val observeDevicesUseCase: ObserveDevicesUseCase,
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevicesListState())
    val uiState: StateFlow<DevicesListState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<DevicesListSideEffect>()
    val sideEffect: SharedFlow<DevicesListSideEffect> = _sideEffect.asSharedFlow()

    init {
        observeDevices()
        observeConnectionState()
    }

    fun handleEvent(event: DevicesListEvent) {
        when (event) {
            is DevicesListEvent.Refresh -> {
                // Локальная работа - ничего не синхронизируем
                _uiState.update { it.copy(isRefreshing = false) }
            }
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
}
