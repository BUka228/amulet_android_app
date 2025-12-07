package com.example.amulet.feature.practices.presentation.session

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.practices.PracticeProgress
import com.example.amulet.shared.domain.practices.model.MoodKind
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeAudioMode
import com.example.amulet.shared.domain.devices.model.BleConnectionState

data class PracticeSessionState(
    val isLoading: Boolean = true,
    val isPatternPreloading: Boolean = false,
    val error: AppError? = null,
    val practiceId: String? = null,
    val session: PracticeSession? = null,
    val progress: PracticeProgress? = null,

    // Данные практики
    val practice: Practice? = null,
    val title: String? = null,
    val type: String? = null,
    val totalDurationSec: Int? = null,

    // Настройки
    val brightnessLevel: Double? = null,
    val vibrationLevel: Double? = null,
    val audioMode: PracticeAudioMode? = null,

    // Статус амулета
    val connectionState: BleConnectionState = BleConnectionState.Disconnected,
    val batteryLevel: Int? = null,
    val isCharging: Boolean = false,
    val isDeviceOnline: Boolean = false,
    val patternName: String? = null,

    // Скрипт практики
    val currentStepIndex: Int? = null,

    // Финальный блок
    val pendingRating: Int? = null,
    val pendingNote: String? = null,

    // Настроение до/после практики (доменная модель, UI сам маппит на иконки/цвета)
    val moodBefore: MoodKind? = null,
    val moodAfter: MoodKind? = null,
)

sealed class PracticeSessionIntent {
    object Start : PracticeSessionIntent()
    data class Stop(val completed: Boolean) : PracticeSessionIntent()
    data class ChangeBrightness(val level: Double) : PracticeSessionIntent()
    data class ChangeAudioMode(val mode: PracticeAudioMode) : PracticeSessionIntent()
    data class Rate(val rating: Int?, val note: String?) : PracticeSessionIntent()
    data class SelectMoodBefore(val mood: MoodKind) : PracticeSessionIntent()
    data class SelectMoodAfter(val mood: MoodKind) : PracticeSessionIntent()
    data class ChangeFeedbackNote(val note: String) : PracticeSessionIntent()
    object NavigateBack : PracticeSessionIntent()
}

sealed class PracticeSessionEffect {
    object NavigateBack : PracticeSessionEffect()
    data class ShowError(val error: AppError) : PracticeSessionEffect()
}
