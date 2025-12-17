package com.example.amulet.feature.hugs.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.hugs.ObserveHugsForPairUseCase
import com.example.amulet.shared.domain.hugs.ObservePairsUseCase
import com.example.amulet.shared.domain.hugs.SyncHugsAndEnsurePatternsUseCase
import com.example.amulet.shared.domain.hugs.model.Hug
import com.example.amulet.shared.domain.hugs.model.PairStatus
import com.example.amulet.shared.domain.user.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant as JavaInstant

@HiltViewModel
class HugsHistoryViewModel @Inject constructor(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val observePairsUseCase: ObservePairsUseCase,
    private val observeHugsForPairUseCase: ObserveHugsForPairUseCase,
    private val syncHugsAndEnsurePatternsUseCase: SyncHugsAndEnsurePatternsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HugsHistoryState())
    val state: StateFlow<HugsHistoryState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<HugsHistoryEffect>()
    val effects = _effects.asSharedFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val userFlow = observeCurrentUserUseCase()
            val pairsFlow = observePairsUseCase()

            userFlow
                .combine(pairsFlow) { user, pairs ->
                    val activePair = pairs.firstOrNull { it.status == PairStatus.ACTIVE } ?: pairs.firstOrNull()
                    BaseData(user = user, pair = activePair)
                }
                .flatMapLatest { base ->
                    val pair = base.pair
                    if (pair == null) {
                        flowOf(FullData(base = base, hugs = emptyList()))
                    } else {
                        observeHugsForPairUseCase(pair.id)
                            .stateIn(
                                scope = viewModelScope,
                                started = SharingStarted.WhileSubscribed(5000),
                                initialValue = emptyList()
                            )
                            .combine(flowOf(base)) { hugs, b ->
                                FullData(base = b, hugs = hugs)
                            }
                    }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = FullData(
                        base = BaseData(user = null, pair = null),
                        hugs = emptyList()
                    )
                )
                .collect { data ->
                    _state.update { current ->
                        val sorted = data.hugs.sortedByDescending { it.createdAt }
                        current.copy(
                            isLoading = false,
                            currentUser = data.base.user,
                            activePair = data.base.pair,
                            hugs = sorted,
                        )
                    }
                }
        }
    }

    fun onIntent(intent: HugsHistoryIntent) {
        when (intent) {
            HugsHistoryIntent.Refresh -> refresh()
            is HugsHistoryIntent.ChangeDirection -> changeDirection(intent.filter)
            is HugsHistoryIntent.ChangePeriod -> changePeriod(intent.filter)
            is HugsHistoryIntent.SelectEmotion -> selectEmotion(intent.key)
            is HugsHistoryIntent.TogglePin -> togglePin(intent.hugId)
            is HugsHistoryIntent.OpenDetails -> openDetails(intent.hugId)
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            val result = syncHugsAndEnsurePatternsUseCase(direction = "all")
            val error = result.component2()
            if (error != null) {
                _state.update { it.copy(error = error) }
                _effects.emit(HugsHistoryEffect.ShowError(error))
            }
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun changeDirection(filter: HugsHistoryDirectionFilter) {
        _state.update { it.copy(directionFilter = filter) }
    }

    private fun changePeriod(filter: HugsHistoryPeriodFilter) {
        _state.update { it.copy(periodFilter = filter) }
    }

    private fun selectEmotion(key: String?) {
        _state.update { it.copy(selectedEmotionKey = key) }
    }

    private fun togglePin(hugId: String) {
        _state.update { current ->
            val updated = current.pinnedIds.toMutableSet()
            if (updated.contains(hugId)) updated.remove(hugId) else updated.add(hugId)
            current.copy(pinnedIds = updated)
        }
    }

    private fun openDetails(hugId: String) {
        viewModelScope.launch {
            _effects.emit(HugsHistoryEffect.OpenDetails(hugId))
        }
    }

    fun visibleHugs(): List<Hug> {
        val s = _state.value
        val hugs = s.hugs
        val nowMs = JavaInstant.now().toEpochMilli()

        val filteredByDirection = when (s.directionFilter) {
            HugsHistoryDirectionFilter.ALL -> hugs
            HugsHistoryDirectionFilter.SENT -> hugs.filter { isSentByCurrentUser(it, s.currentUser?.id?.value) }
            HugsHistoryDirectionFilter.RECEIVED -> hugs.filter { !isSentByCurrentUser(it, s.currentUser?.id?.value) }
        }

        val filteredByPeriod = when (s.periodFilter) {
            HugsHistoryPeriodFilter.ALL_TIME -> filteredByDirection
            HugsHistoryPeriodFilter.LAST_7_DAYS -> filterByPeriod(filteredByDirection, nowMs, 7L * 24L * 60L * 60L * 1000L)
            HugsHistoryPeriodFilter.LAST_24_HOURS -> filterByPeriod(filteredByDirection, nowMs, 24L * 60L * 60L * 1000L)
        }

        val filteredByEmotion = s.selectedEmotionKey?.let { key ->
            filteredByPeriod.filter { buildEmotionKey(it) == key }
        } ?: filteredByPeriod

        val pinned = filteredByEmotion.filter { s.pinnedIds.contains(it.id.value) }
        val others = filteredByEmotion.filterNot { s.pinnedIds.contains(it.id.value) }

        return pinned + others
    }

    fun availableEmotionKeys(): List<String> {
        return _state.value.hugs
            .map { buildEmotionKey(it) }
            .distinct()
    }

    private fun isSentByCurrentUser(hug: Hug, currentUserId: String?): Boolean {
        if (currentUserId == null) return false
        return hug.fromUserId.value == currentUserId
    }

    private fun filterByPeriod(hugs: List<Hug>, nowMs: Long, windowMs: Long): List<Hug> {
        val threshold = nowMs - windowMs
        return hugs.filter { it.createdAt.toEpochMilliseconds() >= threshold }
    }

    private fun buildEmotionKey(hug: Hug): String {
        val color = hug.emotion.colorHex ?: ""
        val patternId = hug.emotion.patternId?.value ?: ""
        return "$color|$patternId"
    }

    private data class BaseData(
        val user: com.example.amulet.shared.domain.user.model.User?,
        val pair: com.example.amulet.shared.domain.hugs.model.Pair?,
    )

    private data class FullData(
        val base: BaseData,
        val hugs: List<Hug>,
    )
}
