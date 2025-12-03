package com.example.amulet.feature.hugs.presentation.pairing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.hugs.ObservePairsUseCase
import com.example.amulet.shared.domain.hugs.InvitePairUseCase
import com.example.amulet.shared.domain.hugs.AcceptPairUseCase
import com.example.amulet.shared.domain.hugs.SyncPairsUseCase
import com.example.amulet.shared.domain.hugs.model.PairStatus
import com.example.amulet.shared.domain.user.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HugsPairingViewModel @Inject constructor(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val observePairsUseCase: ObservePairsUseCase,
    private val invitePairUseCase: InvitePairUseCase,
    private val acceptPairUseCase: AcceptPairUseCase,
    private val syncPairsUseCase: SyncPairsUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(HugsPairingState())
    val state: StateFlow<HugsPairingState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<HugsPairingEffect>()
    val effects = _effects.asSharedFlow()

    init {
        val inviterName = savedStateHandle.get<String>("inviterName")
        val inviteCode = savedStateHandle.get<String>("code")
        
        _state.update { current ->
            current.copy(
                inviterName = inviterName,
                inviteCode = inviteCode ?: current.inviteCode,
                step = if (inviterName != null) HugsPairingStep.CONFIRM_INVITE else HugsPairingStep.SHARE_LINK
            )
        }
        
        observeData()
        
        // Автоматически генерируем ссылку, если мы не принимаем приглашение
        if (inviterName == null) {
            generateInvite()
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val userFlow = observeCurrentUserUseCase()
            val pairsFlow = observePairsUseCase()

            userFlow
                .combine(pairsFlow) { user, pairs ->
                    val activePair = pairs.firstOrNull { it.status == PairStatus.ACTIVE } ?: pairs.firstOrNull()
                    BaseData(userName = user?.displayName, activePair = activePair)
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = BaseData(userName = null, activePair = null)
                )
                .collect { data ->
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            currentUser = current.currentUser?.copy(displayName = data.userName) ?: current.currentUser,
                            activePair = data.activePair,
                        )
                    }
                    // Если имя пользователя загрузилось и ссылки еще нет, перегенерируем (чтобы имя попало в ссылку)
                    if (data.userName != null && _state.value.inviteLink == null && _state.value.inviterName == null) {
                        generateInvite()
                    }
                }
        }
    }

    fun onIntent(intent: HugsPairingIntent) {
        when (intent) {
            HugsPairingIntent.GoToShareLink -> setStep(HugsPairingStep.SHARE_LINK)
            HugsPairingIntent.GoToWaiting -> goToWaiting()
            HugsPairingIntent.GenerateInvite -> generateInvite()
            HugsPairingIntent.CopyInviteLink -> copyInviteLink()
            HugsPairingIntent.ShareInviteLink -> shareInviteLink()
            HugsPairingIntent.CancelInvite -> cancelInvite()
            HugsPairingIntent.AcceptInvite -> acceptInvite()
            HugsPairingIntent.DeclineInvite -> declineInvite()
        }
    }

    private fun setStep(step: HugsPairingStep) {
        _state.update { it.copy(step = step) }
    }

    private fun generateInvite() {
        // Если уже есть ссылка, не генерируем заново, если только не принудительно (но пока принудительно нет)
        if (_state.value.inviteLink != null) return

        val inviterName = _state.value.currentUser?.displayName ?: ""
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            invitePairUseCase()
                .onSuccess { invite ->
                    val code = invite.inviteId
                    val encodedName = try {
                        java.net.URLEncoder.encode(inviterName.ifBlank { "" }, "UTF-8")
                    } catch (_: Exception) {
                        inviterName
                    }
                    val link = if (encodedName.isNotBlank()) {
                        "https://amulet.app/hugs/pair?code=$code&inviterName=$encodedName"
                    } else {
                        "https://amulet.app/hugs/pair?code=$code"
                    }
                    
                    val qrCode = QrCodeUtils.generateQrCode(link)

                    _state.update {
                        it.copy(
                            isLoading = false,
                            inviteCode = code,
                            inviteLink = link,
                            qrCode = qrCode,
                            isInviteSent = false,
                            isWaitingConfirmation = false,
                            step = HugsPairingStep.SHARE_LINK,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error) }
                    _effects.emit(HugsPairingEffect.ShowError(error))
                }
        }
    }

    private fun copyInviteLink() {
        val link = _state.value.inviteLink ?: return
        viewModelScope.launch {
            _effects.emit(HugsPairingEffect.CopyToClipboard(link))
        }
    }

    private fun shareInviteLink() {
        val link = _state.value.inviteLink ?: return
        viewModelScope.launch {
            _effects.emit(HugsPairingEffect.ShareText(link))
            _state.update { it.copy(isInviteSent = true) }
        }
    }

    private fun goToWaiting() {
        if (_state.value.inviteLink == null) return
        _state.update {
            it.copy(
                step = HugsPairingStep.WAITING_CONFIRMATION,
                isWaitingConfirmation = true,
            )
        }
    }

    private fun cancelInvite() {
        _state.update {
            it.copy(
                isWaitingConfirmation = false,
                isInviteSent = false,
                step = HugsPairingStep.SHARE_LINK,
            )
        }
    }

    private fun acceptInvite() {
        val inviteId = _state.value.inviteCode ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            acceptPairUseCase(inviteId)
                .onSuccess {
                    // После успешного подтверждения пары пробуем синхронизировать список пар
                    syncPairsUseCase()
                    _state.update { it.copy(isLoading = false, isConfirmed = true) }
                    _effects.emit(HugsPairingEffect.Close)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error) }
                    _effects.emit(HugsPairingEffect.ShowError(error))
                }
        }
    }

    private fun declineInvite() {
        viewModelScope.launch {
            _effects.emit(HugsPairingEffect.Close)
        }
    }

    private data class BaseData(
        val userName: String?,
        val activePair: com.example.amulet.shared.domain.hugs.model.Pair?,
    )
}
