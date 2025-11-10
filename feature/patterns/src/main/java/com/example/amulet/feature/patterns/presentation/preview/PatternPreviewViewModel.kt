package com.example.amulet.feature.patterns.presentation.preview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.usecase.ObserveDevicesUseCase
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.usecase.GetPatternByIdUseCase
import com.example.amulet.shared.domain.patterns.usecase.PreviewPatternOnDeviceUseCase
import com.example.amulet.shared.domain.patterns.usecase.PreviewProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatternPreviewViewModel @Inject constructor(
    private val getPatternByIdUseCase: GetPatternByIdUseCase,
    private val observeDevicesUseCase: ObserveDevicesUseCase,
    private val previewPatternOnDeviceUseCase: PreviewPatternOnDeviceUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patternId: String? = savedStateHandle.get<String>("patternId")

    private val _uiState = MutableStateFlow(PatternPreviewState())
    val uiState: StateFlow<PatternPreviewState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<PatternPreviewSideEffect>()
    val sideEffect: SharedFlow<PatternPreviewSideEffect> = _sideEffect.asSharedFlow()

    init {
        loadPattern()
        loadDevices()
    }

    fun handleEvent(event: PatternPreviewEvent) {
        when (event) {
            is PatternPreviewEvent.LoadDevices -> loadDevices()
            is PatternPreviewEvent.SelectDevice -> selectDevice(event.deviceId)
            is PatternPreviewEvent.TogglePreviewMode -> togglePreviewMode()
            is PatternPreviewEvent.PlayPattern -> playPattern()
            is PatternPreviewEvent.PausePattern -> pausePattern()
            is PatternPreviewEvent.StopPattern -> stopPattern()
            is PatternPreviewEvent.SendToDevice -> sendToDevice()
            is PatternPreviewEvent.DismissError -> dismissError()
        }
    }

    private fun loadPattern() {
        if (patternId == null) {
            _uiState.update { it.copy(isLoading = false, pattern = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getPatternByIdUseCase(PatternId(patternId))
                .collect { pattern ->
                    _uiState.update {
                        it.copy(
                            pattern = pattern,
                            spec = pattern?.spec,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun loadDevices() {
        observeDevicesUseCase()
            .onEach { devices ->
                _uiState.update {
                    it.copy(
                        devices = devices,
                        selectedDevice = devices.firstOrNull()
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun selectDevice(deviceId: String) {
        _uiState.update {
            it.copy(selectedDevice = it.devices.find { device -> device.id.value == deviceId })
        }
    }

    private fun togglePreviewMode() {
        _uiState.update { it.copy(localPreview = !it.localPreview) }
    }

    private fun playPattern() {
        val currentState = _uiState.value

        if (!currentState.localPreview && currentState.selectedDevice == null) {
            viewModelScope.launch {
                _sideEffect.emit(PatternPreviewSideEffect.ShowDeviceRequired)
            }
            return
        }

        _uiState.update { it.copy(isPlaying = true, isPaused = false) }

        if (!currentState.localPreview) {
            sendToDevice()
        }
    }

    private fun pausePattern() {
        _uiState.update { it.copy(isPaused = true) }
    }

    private fun stopPattern() {
        _uiState.update {
            it.copy(
                isPlaying = false,
                isPaused = false,
                progress = null
            )
        }
    }

    private fun sendToDevice() {
        val currentState = _uiState.value
        val spec = currentState.spec ?: return
        val device = currentState.selectedDevice ?: return

        viewModelScope.launch {
            previewPatternOnDeviceUseCase(spec, device.id)
                .collect { progress ->
                    when (progress) {
                        is PreviewProgress.Compiling -> {
                            _uiState.update { it.copy(progress = progress) }
                        }
                        is PreviewProgress.Uploading -> {
                            _uiState.update { it.copy(progress = progress) }
                        }
                        is PreviewProgress.Playing -> {
                            _uiState.update { it.copy(progress = progress, isPlaying = true) }
                            _sideEffect.emit(PatternPreviewSideEffect.ShowSnackbar("Паттерн отправлен на устройство"))
                        }
                        is PreviewProgress.Failed -> {
                            _uiState.update {
                                it.copy(
                                    isPlaying = false,
                                    progress = null
                                )
                            }
                            progress.cause?.let { error ->
                                _sideEffect.emit(PatternPreviewSideEffect.ShowSnackbar("Ошибка: ${error.message}"))
                            }
                        }
                    }
                }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
