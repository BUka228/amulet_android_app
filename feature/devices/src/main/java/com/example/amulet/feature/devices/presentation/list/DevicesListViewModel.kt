package com.example.amulet.feature.devices.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.devices.usecase.ObserveDeviceSessionStatusUseCase
import com.example.amulet.shared.domain.devices.usecase.ObserveDevicesUseCase
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
    private val observeDeviceSessionStatusUseCase: ObserveDeviceSessionStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevicesListState())
    val uiState: StateFlow<DevicesListState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<DevicesListSideEffect>()
    val sideEffect: SharedFlow<DevicesListSideEffect> = _sideEffect.asSharedFlow()

    init {
        observeDevices()
        observeDeviceSession()
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

    private fun observeDeviceSession() {
        observeDeviceSessionStatusUseCase()
            .onEach { sessionStatus ->
                _uiState.update { it.copy(connectionStatus = sessionStatus.connection) }
            }
            .launchIn(viewModelScope)
    }
}
