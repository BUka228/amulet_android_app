package com.example.amulet.shared.domain.practices

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeId
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeSessionId
import com.example.amulet.shared.domain.practices.model.PracticeSessionSource
import com.example.amulet.shared.domain.practices.model.PracticeSessionStatus
import com.example.amulet.shared.domain.practices.model.PracticeStep
import com.example.amulet.shared.domain.practices.usecase.GetActiveSessionStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetPracticeByIdUseCase
import com.example.amulet.shared.domain.patterns.usecase.ClearCurrentDevicePatternUseCase
import com.example.amulet.shared.domain.practices.usecase.StartPracticeUseCase
import com.example.amulet.shared.domain.practices.usecase.StopSessionUseCase
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PracticeSessionManagerImpl(
    private val startPractice: StartPracticeUseCase,
    private val stopSessionUseCase: StopSessionUseCase,
    private val getActiveSessionStreamUseCase: GetActiveSessionStreamUseCase,
    private val getPracticeById: GetPracticeByIdUseCase,
    private val clearCurrentDevicePattern: ClearCurrentDevicePatternUseCase,
    private val scriptOrchestrator: PracticeScriptOrchestrator,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val tickerIntervalMs: Long = 1000L
) : PracticeSessionManager {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    override val activeSession: Flow<PracticeSession?> = getActiveSessionStreamUseCase()

    override val progress: Flow<PracticeProgress?> =
        activeSession.flatMapLatest { session ->
            if (session == null) flowOf(null) else buildProgressFlow(session)
        }

    init {
        scope.launch {
            activeSession.collect { session ->
                if (session != null && session.status == PracticeSessionStatus.ACTIVE) {
                    val practice = getPracticeById(session.practiceId).firstOrNull()
                    if (practice != null) {
                        scriptOrchestrator.start(practice, session)
                    }
                } else {
                    scriptOrchestrator.stop()
                }
            }
        }
    }

    override suspend fun startSession(
        practiceId: PracticeId,
        source: PracticeSessionSource?,
        initialIntensity: Double?,
        initialBrightness: Double?
    ): AppResult<PracticeSession> = withContext(dispatcher) {
        val result = startPractice(
            practiceId = practiceId,
            intensity = initialIntensity,
            brightness = initialBrightness,
            vibrationLevel = null,
            audioMode = null,
            source = source,
        )
        // Оркестратор запустится автоматически через activeSession.collect
        result
    }

    override suspend fun stopSession(completed: Boolean): AppResult<PracticeSession> = withContext(dispatcher) {
        val session = activeSession.firstOrNull()
        val sessionId = session?.id ?: return@withContext Err(AppError.NotFound)
        val result = stopSessionUseCase(sessionId, completed)
        // Оркестратор остановится автоматически через activeSession.collect
        // Но для мгновенной реакции можно и тут дернуть, хотя не обязательно.
        // Оставим только очистку, если вдруг что-то пойдет не так с реактивностью,
        // но вообще clearCurrentDevicePattern вызывается и в orchestrator.stop().
        // Поэтому здесь просто возвращаем результат.
        result
    }

    private fun buildProgressFlow(session: PracticeSession): Flow<PracticeProgress?> {
        return getPracticeById(session.practiceId).flatMapLatest { practice ->
            if (practice == null) flowOf(null) else tickerProgressFlow(session, practice)
        }
    }

    private fun tickerProgressFlow(
        session: PracticeSession,
        practice: Practice
    ): Flow<PracticeProgress?> = flow {
        val scriptSteps = practice.script?.steps.orEmpty()
        val totalSteps = scriptSteps.size
        val totalDurationSec = session.durationSec ?: practice.durationSec

        while (currentCoroutineContext().isActive) {
            val now = System.currentTimeMillis()
            val elapsed = when (session.status) {
                PracticeSessionStatus.ACTIVE ->
                    (((now - session.startedAt) / 1000).toInt()).coerceAtLeast(0)
                PracticeSessionStatus.COMPLETED, PracticeSessionStatus.CANCELLED ->
                    (session.actualDurationSec ?: session.durationSec ?: 0).coerceAtLeast(0)
                else -> 0
            }

            val currentStepInfo = computeCurrentStep(scriptSteps, elapsed)

            emit(
                PracticeProgress(
                    sessionId = session.id,
                    elapsedSec = elapsed,
                    totalSec = totalDurationSec,
                    currentStepIndex = currentStepInfo?.first,
                    totalSteps = totalSteps,
                    currentStep = currentStepInfo?.second
                )
            )

            delay(tickerIntervalMs)
        }
    }

    private fun computeCurrentStep(
        steps: List<PracticeStep>,
        elapsedSec: Int
    ): Pair<Int, PracticeStep>? {
        if (steps.isEmpty()) return null
        if (elapsedSec <= 0) return 0 to steps.first()

        var acc = 0
        steps.forEachIndexed { index, step ->
            val stepDuration = step.durationSec ?: 0
            val nextAcc = acc + stepDuration
            if (stepDuration <= 0) {
                if (index == steps.lastIndex) {
                    return index to step
                }
            } else {
                if (elapsedSec < nextAcc) {
                    return index to step
                }
            }
            acc = nextAcc
        }
        return (steps.lastIndex) to steps.last()
    }
}
