package com.example.amulet.shared.domain.practices

import com.example.amulet.shared.domain.patterns.PatternPlaybackService
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.usecase.GetPatternByIdUseCase
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeStep
import com.example.amulet.shared.domain.practices.model.PracticeStepType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Оркестратор исполнения PracticeScript: шаг за шагом меняет паттерн на амулете.
 */
interface PracticeScriptOrchestrator {

    /** Индекс текущего шага скрипта (или null, если ничего не играет). */
    val currentStepIndex: StateFlow<Int?>

    /**
     * Запустить исполнение скрипта практики для конкретной сессии.
     * Если скрипта нет или он пустой, ничего не делает.
     */
    fun start(practice: Practice, session: PracticeSession)

    /** Остановить исполнение скрипта и очистить амулет. */
    suspend fun stop()
}

class PracticeScriptOrchestratorImpl(
    private val getPatternByIdUseCase: GetPatternByIdUseCase,
    private val patternPlaybackService: PatternPlaybackService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : PracticeScriptOrchestrator {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _currentStepIndex = MutableStateFlow<Int?>(null)
    override val currentStepIndex: StateFlow<Int?> = _currentStepIndex.asStateFlow()

    private var currentJob: Job? = null

    override fun start(practice: Practice, session: PracticeSession) {
        val script = practice.script
        val steps: List<PracticeStep> = when {
            script != null && script.steps.isNotEmpty() ->
                script.steps.sortedBy { it.order }
            practice.patternId != null -> {
                // Фолбэк: одна «ступень» с паттерном практики и общей длительностью.
                val duration = practice.durationSec
                    ?: session.durationSec
                    ?: 0
                listOf(
                    PracticeStep(
                        order = 0,
                        type = PracticeStepType.CUSTOM,
                        title = null,
                        description = null,
                        durationSec = duration,
                        patternId = practice.patternId.value,
                    )
                )
            }
            else -> emptyList()
        }
        if (steps.isEmpty()) return

        currentJob?.cancel()

        currentJob = scope.launch {
            steps.forEachIndexed { index, step ->
                if (!currentCoroutineContext().isActive) return@launch

                _currentStepIndex.value = index

                val stepPatternId = step.patternId?.let { PatternId(it) } ?: practice.patternId

                if (stepPatternId != null) {
                    val pattern = getPatternByIdUseCase(stepPatternId).firstOrNull()
                    if (pattern != null) {
                        // Используем интенсивность сессии, если она задана
                        val intensity = session.intensity ?: 1.0
                        patternPlaybackService.playOnConnectedDevice(pattern.spec, intensity)
                    }
                }

                val durationSec = step.durationSec
                    ?: practice.durationSec
                    ?: session.durationSec
                    ?: 0

                val safeDuration = durationSec.coerceAtLeast(0)
                if (safeDuration > 0) {
                    var elapsed = 0
                    while (elapsed < safeDuration && currentCoroutineContext().isActive) {
                        delay(1000L)
                        elapsed++
                    }
                }
            }

            _currentStepIndex.value = null
        }
    }

    override suspend fun stop() {
        val job = currentJob
        if (job != null) {
            job.cancelAndJoin()
            currentJob = null
        }

        _currentStepIndex.value = null
        patternPlaybackService.clearCurrentDevice()
    }
}
