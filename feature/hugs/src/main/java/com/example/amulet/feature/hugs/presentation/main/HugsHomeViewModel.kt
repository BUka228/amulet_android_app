package com.example.amulet.feature.hugs.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.hugs.ObserveHugsForPairUseCase
import com.example.amulet.shared.domain.hugs.ObservePairsUseCase
import com.example.amulet.shared.domain.hugs.ObservePairQuickRepliesUseCase
import com.example.amulet.shared.domain.hugs.SendHugUseCase
import com.example.amulet.shared.domain.hugs.SyncHugsUseCase
import com.example.amulet.shared.domain.hugs.model.PairStatus
import com.example.amulet.shared.domain.user.model.UserId
import com.example.amulet.shared.domain.user.usecase.FetchUserProfileUseCase
import com.example.amulet.shared.domain.user.usecase.ObserveCurrentUserUseCase
import com.example.amulet.shared.domain.user.usecase.ObserveUserByIdUseCase
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HugsHomeViewModel @Inject constructor(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val observePairsUseCase: ObservePairsUseCase,
    private val observeHugsForPairUseCase: ObserveHugsForPairUseCase,
    private val observePairQuickRepliesUseCase: ObservePairQuickRepliesUseCase,
    private val sendHugUseCase: SendHugUseCase,
    private val syncHugsUseCase: SyncHugsUseCase,
    private val observeUserByIdUseCase: ObserveUserByIdUseCase,
    private val fetchUserProfileUseCase: FetchUserProfileUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HugsHomeState())
    val state: StateFlow<HugsHomeState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<HugsHomeEffect>()
    val effects = _effects.asSharedFlow()

    init {
        observeData()
    }

    private val requestedPartnerUserIds = mutableSetOf<String>()

    private fun observeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val userFlow = observeCurrentUserUseCase()
            val pairsFlow = observePairsUseCase()

            userFlow
                .combine(pairsFlow) { user, pairs ->
                    val activePair = pairs.firstOrNull { it.status == PairStatus.ACTIVE } ?: pairs.firstOrNull()
                    BaseData(user, activePair)
                }
                .flatMapLatest { base ->
                    val pair = base.pair
                    val user = base.user

                    if (pair == null || user == null) {
                        flowOf(FullData(base, partnerUser = null, hugs = emptyList(), quickReplies = emptyList()))
                    } else {
                        val partnerUserId = pair.members
                            .map { it.userId }
                            .firstOrNull { it != user.id }

                        val hugsFlow = observeHugsForPairUseCase(pair.id)
                        val quickRepliesFlow = observePairQuickRepliesUseCase(pair.id, user.id)
                        val partnerUserFlow = if (partnerUserId != null) {
                            observeUserByIdUseCase(partnerUserId)
                        } else {
                            flowOf(null)
                        }

                        combine(hugsFlow, quickRepliesFlow, partnerUserFlow) { hugs, quickReplies, partnerUser ->
                            FullData(base, partnerUser, hugs, quickReplies)
                        }
                    }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = FullData(
                        base = BaseData(null, null),
                        partnerUser = null,
                        hugs = emptyList(),
                        quickReplies = emptyList()
                    )
                )
                .collect { data ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentUser = data.base.user,
                            activePair = data.base.pair,
                            partnerUser = data.partnerUser,
                            hugs = data.hugs.sortedByDescending { hug -> hug.createdAt }.take(20),
                            quickReplies = data.quickReplies,
                        )
                    }

                    val partnerUserId = data.base.pair?.members
                        ?.map { it.userId }
                        ?.firstOrNull { it != data.base.user?.id }

                    if (partnerUserId != null && data.partnerUser == null &&
                        requestedPartnerUserIds.add(partnerUserId.value)
                    ) {
                        viewModelScope.launch {
                            fetchUserProfileUseCase(UserId(partnerUserId.value))
                        }
                    }
                }
        }
    }

    fun onIntent(intent: HugsHomeIntent) {
        when (intent) {
            HugsHomeIntent.Refresh -> refresh()
            HugsHomeIntent.SendHug -> sendHug()
            HugsHomeIntent.OpenHistory -> emitEffect(HugsHomeEffect.NavigateToHistory)
            HugsHomeIntent.OpenSettings -> emitEffect(HugsHomeEffect.NavigateToSettings)
            HugsHomeIntent.OpenEmotions -> emitEffect(HugsHomeEffect.NavigateToEmotions)
            HugsHomeIntent.OpenSecretCodes -> emitEffect(HugsHomeEffect.NavigateToSecretCodes)
            HugsHomeIntent.OpenPairing -> emitEffect(HugsHomeEffect.NavigateToPairing)
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            val result = syncHugsUseCase(direction = "all")
            val error = result.component2()
            if (error != null) {
                emitEffect(HugsHomeEffect.ShowError(error))
            }
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun sendHug() {
        val pair = _state.value.activePair ?: return
        val currentUser = _state.value.currentUser ?: return
        val toUserId = pair.members.firstOrNull { it.userId != currentUser.id }?.userId

        viewModelScope.launch {
            _state.update { it.copy(isSending = true) }
            val result = sendHugUseCase(
                pairId = pair.id,
                fromUserId = currentUser.id,
                toUserId = toUserId,
            )
            val error = result.component2()
            if (error != null) {
                emitEffect(HugsHomeEffect.ShowError(error))
            }
            _state.update { it.copy(isSending = false) }
        }
    }

    private fun emitEffect(effect: HugsHomeEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }

    private data class BaseData(
        val user: com.example.amulet.shared.domain.user.model.User?,
        val pair: com.example.amulet.shared.domain.hugs.model.Pair?,
    )

    private data class FullData(
        val base: BaseData,
        val partnerUser: com.example.amulet.shared.domain.user.model.User?,
        val hugs: List<com.example.amulet.shared.domain.hugs.model.Hug>,
        val quickReplies: List<com.example.amulet.shared.domain.hugs.model.PairQuickReply>,
    )
}
