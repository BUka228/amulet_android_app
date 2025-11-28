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
    private var currentSessionId: String? = null

    override fun start(practice: Practice, session: PracticeSession) {
        // Если сессия та же самая и джоба активна, просто обновляем (или игнорируем, если не нужно)
        // Но для надежности при "start" (который теперь работает как sync) лучше перезапустить логику,
        // учитывая прошедшее время.
        // Однако, чтобы не прерывать воспроизведение при каждом чихе, проверим ID.
        if (currentSessionId == session.id && currentJob?.isActive == true) {
            return
        }

        currentSessionId = session.id
        currentJob?.cancel()

        currentJob = scope.launch {
            val script = practice.script
            val steps: List<PracticeStep> = when {
                script != null && script.steps.isNotEmpty() ->
                    script.steps.sortedBy { it.order }
                practice.patternId != null -> {
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

            if (steps.isEmpty()) return@launch

            val startTime = session.startedAt
            val now = System.currentTimeMillis()
            // Сколько времени уже прошло от начала сессии (в мс)
            val elapsedMs = (now - startTime).coerceAtLeast(0)
            
            // Найдем, на каком мы сейчас шаге
            var accumulatedDurationMs = 0L
            
            for ((index, step) in steps.withIndex()) {
                if (!isActive) return@launch

                val stepDurationSec = step.durationSec ?: 0
                val stepDurationMs = stepDurationSec * 1000L
                
                val stepEndTimeMs = accumulatedDurationMs + stepDurationMs
                
                if (elapsedMs < stepEndTimeMs) {
                    // Мы внутри этого шага.
                    // Нужно сыграть паттерн и подождать остаток времени.
                    
                    _currentStepIndex.value = index
                    
                    val stepPatternId = step.patternId?.let { PatternId(it) } ?: practice.patternId
                    if (stepPatternId != null) {
                        val pattern = getPatternByIdUseCase(stepPatternId).firstOrNull()
                        if (pattern != null) {
                            val intensity = session.intensity ?: 1.0
                            patternPlaybackService.playOnConnectedDevice(pattern.spec, intensity)
                        }
                    }

                    // Сколько осталось до конца шага
                    val timeRemainingInStep = stepEndTimeMs - elapsedMs
                    // Но elapsedMs мы считали в начале. Сейчас время могло чуть уйти.
                    // Пересчитаем точнее:
                    val currentNow = System.currentTimeMillis()
                    val currentElapsed = currentNow - startTime
                    val exactRemaining = stepEndTimeMs - currentElapsed
                    
                    if (exactRemaining > 0) {
                        delay(exactRemaining)
                    }
                } else {
                    // Этот шаг уже прошел. Пропускаем.
                    // Если это был последний шаг, и мы его проскочили - цикл закончится, джоба завершится.
                }
                
                accumulatedDurationMs += stepDurationMs
            }

            _currentStepIndex.value = null
            currentSessionId = null
        }
    }

    override suspend fun stop() {
        currentJob?.cancelAndJoin()
        currentJob = null
        currentSessionId = null
        _currentStepIndex.value = null
        patternPlaybackService.clearCurrentDevice()
    }
}
