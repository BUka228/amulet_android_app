package com.example.amulet.shared.domain.practices

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.model.PracticeId
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeSessionId
import com.example.amulet.shared.domain.practices.model.PracticeSessionSource
import com.example.amulet.shared.domain.practices.model.PracticeStep
import kotlinx.coroutines.flow.Flow

data class PracticeProgress(
    val sessionId: PracticeSessionId?,
    val elapsedSec: Int,
    val totalSec: Int?,
    val currentStepIndex: Int?,
    val totalSteps: Int,
    val currentStep: PracticeStep?
)

interface PracticeSessionManager {

    val activeSession: Flow<PracticeSession?>

    val progress: Flow<PracticeProgress?>

    suspend fun startSession(
        practiceId: PracticeId,
        source: PracticeSessionSource? = PracticeSessionSource.Manual,
        initialIntensity: Double? = null,
        initialBrightness: Double? = null
    ): AppResult<PracticeSession>

    suspend fun stopSession(completed: Boolean): AppResult<PracticeSession>
}
