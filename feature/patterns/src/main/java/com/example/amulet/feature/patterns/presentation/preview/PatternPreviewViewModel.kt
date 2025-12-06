package com.example.amulet.feature.patterns.presentation.preview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.devices.usecase.ObserveDevicesUseCase
import com.example.amulet.shared.domain.patterns.PreviewCache
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.usecase.GetPatternByIdUseCase
import com.example.amulet.shared.domain.patterns.usecase.PreviewPatternOnDeviceUseCase
import com.example.amulet.shared.domain.patterns.usecase.PreviewProgress
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
class PatternPreviewViewModel @Inject constructor(
    private val getPatternByIdUseCase: GetPatternByIdUseCase,
    private val observeDevicesUseCase: ObserveDevicesUseCase,
    private val previewPatternOnDeviceUseCase: PreviewPatternOnDeviceUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patternId: String? = savedStateHandle.get<String>("patternId")
    private val previewKey: String? = savedStateHandle.get<String>("key")

    private val _uiState = MutableStateFlow(PatternPreviewState())
    val uiState: StateFlow<PatternPreviewState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<PatternPreviewSideEffect>()
    val sideEffect: SharedFlow<PatternPreviewSideEffect> = _sideEffect.asSharedFlow()

    init {
        when {
            patternId != null -> loadPatternById(patternId)
            previewKey != null -> loadSpecFromCache(previewKey)
            else -> _uiState.update { it.copy(isLoading = false, pattern = null) }
        }
        loadDevices()
    }

    fun handleEvent(event: PatternPreviewEvent) {
        when (event) {
            is PatternPreviewEvent.LoadDevices -> loadDevices()
            is PatternPreviewEvent.SelectDevice -> selectDevice(event.deviceId)
            is PatternPreviewEvent.PlayPause -> togglePlayPause()
            is PatternPreviewEvent.Restart -> restart()
            is PatternPreviewEvent.UpdateLoop -> updateLoop(event.loop)
            is PatternPreviewEvent.SendToDevice -> sendToDevice()
            is PatternPreviewEvent.DismissError -> dismissError()
        }
    }

    private fun loadPatternById(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getPatternByIdUseCase(PatternId(id))
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

    private fun loadSpecFromCache(key: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val spec = PreviewCache.take(key)
            if (spec != null) {
                _uiState.update {
                    it.copy(
                        pattern = null,
                        spec = spec,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
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

    private fun togglePlayPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    private fun restart() {
        _uiState.update {
            it.copy(
                isPlaying = true,
                progress = null
            )
        }
    }

    private fun updateLoop(loop: Boolean) {
        _uiState.update { it.copy(isLooping = loop) }
    }

    private fun sendToDevice() {
        val currentState = _uiState.value
        Logger.d("sendToDevice: currentState=$currentState", tag = TAG)

        val spec = currentState.spec
        if (spec == null) {
            Logger.d("sendToDevice: spec is null, aborting", tag = TAG)
            return
        }

        val device = currentState.selectedDevice
        if (device == null) {
            Logger.d("sendToDevice: selectedDevice is null, aborting", tag = TAG)
            return
        }

        Logger.d(
            "sendToDevice: starting preview for deviceId=${device.id.value}, patternType=${spec.type}",
            tag = TAG
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingToDevice = true) }
            Logger.d("sendToDevice: isSendingToDevice set to true", tag = TAG)

            previewPatternOnDeviceUseCase(spec, device.id)
                .collect { progress ->
                    Logger.d("sendToDevice: progress=$progress", tag = TAG)
                    when (progress) {
                        is PreviewProgress.Compiling -> {
                            _uiState.update { it.copy(progress = progress) }
                        }
                        is PreviewProgress.Uploading -> {
                            _uiState.update { it.copy(progress = progress) }
                        }
                        is PreviewProgress.Playing -> {
                            _uiState.update { 
                                it.copy(
                                    progress = progress, 
                                    isPlaying = true,
                                    isSendingToDevice = false
                                ) 
                            }
                            Logger.d("sendToDevice: pattern successfully sent and playing on device", tag = TAG)
                            _sideEffect.emit(PatternPreviewSideEffect.ShowSnackbar("Паттерн отправлен на устройство"))
                        }
                        is PreviewProgress.Failed -> {
                            _uiState.update {
                                it.copy(
                                    isPlaying = false,
                                    isSendingToDevice = false,
                                    progress = null
                                )
                            }
                            Logger.d("sendToDevice: failed with cause=${progress.cause}", tag = TAG)
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

    private companion object {
        private const val TAG = "PatternPreviewViewModel"
    }
}
