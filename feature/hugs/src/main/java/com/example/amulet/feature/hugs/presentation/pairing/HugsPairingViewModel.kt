package com.example.amulet.feature.hugs.presentation.pairing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.logging.Logger
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
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

    private var waitingSyncJob: Job? = null

    init {
        val inviterName = savedStateHandle.get<String>("inviterName")
        val inviteCode = savedStateHandle.get<String>("code")

        _state.update { current ->
            current.copy(
                inviterName = inviterName,
                inviteCode = inviteCode ?: current.inviteCode,
                // Если пришли по deeplink с кодом, сразу показываем экран подтверждения,
                // даже если имени пригласившего нет.
                step = if (inviteCode != null) {
                    HugsPairingStep.CONFIRM_INVITE
                } else {
                    HugsPairingStep.SHARE_LINK
                }
            )
        }

        observeData()

        // Автоматически генерируем ссылку, только если мы не принимаем приглашение
        if (inviterName == null && inviteCode == null) {
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
                    // Если имя пользователя загрузилось и ссылки еще нет, перегенерируем (чтобы имя попало в ссылку),
                    // но только в режиме инициатора (когда у нас ещё нет кода приглашения).
                    if (
                        data.userName != null &&
                        _state.value.inviteLink == null &&
                        _state.value.inviterName == null &&
                        _state.value.inviteCode == null
                    ) {
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
        Logger.d("generateInvite: start, inviterName='$inviterName'", "HugsPairingViewModel")
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
                        "https://amuletinvite.vercel.app/hugs/pair?code=$code&inviterName=$encodedName"
                    } else {
                        "https://amuletinvite.vercel.app/hugs/pair?code=$code"
                    }

                    Logger.d(
                        "generateInvite: success, inviteId=$code, link=$link",
                        "HugsPairingViewModel"
                    )
                    
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
                    Logger.e(
                        "generateInvite: error=$error",
                        throwable = Exception(error.toString()),
                        tag = "HugsPairingViewModel"
                    )
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

        // Запускаем периодическую синхронизацию пар, пока ждём подтверждения.
        waitingSyncJob?.cancel()
        waitingSyncJob = viewModelScope.launch {
            while (true) {
                val current = state.value

                // Если пользователь вышел из экрана ожидания или отменил приглашение — выходим из цикла.
                if (!current.isWaitingConfirmation || current.step != HugsPairingStep.WAITING_CONFIRMATION) {
                    break
                }

                // Как только появилась активная пара — закрываем экран паринга.
                val activePair = current.activePair
                if (activePair != null && activePair.status == PairStatus.ACTIVE) {
                    _effects.emit(HugsPairingEffect.Close)
                    break
                }

                // Делаем запрос на синхронизацию списка пар.
                syncPairsUseCase()

                // Ждём немного перед следующей попыткой.
                delay(5_000)
            }
        }
    }

    private fun cancelInvite() {
        waitingSyncJob?.cancel()
        waitingSyncJob = null
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
        Logger.d("acceptInvite: start, inviteId=$inviteId", "HugsPairingViewModel")
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            acceptPairUseCase(inviteId)
                .onSuccess {
                    Logger.d("acceptInvite: success, inviteId=$inviteId", "HugsPairingViewModel")
                    // После успешного подтверждения пары пробуем синхронизировать список пар
                    syncPairsUseCase()
                    _state.update { it.copy(isLoading = false, isConfirmed = true) }
                    waitingSyncJob?.cancel()
                    waitingSyncJob = null
                    _effects.emit(HugsPairingEffect.Close)
                }
                .onFailure { error ->
                    Logger.e(
                        "acceptInvite: error=$error",
                        throwable = Exception(error.toString()),
                        tag = "HugsPairingViewModel"
                    )
                    _state.update { it.copy(isLoading = false, error = error) }
                    _effects.emit(HugsPairingEffect.ShowError(error))
                }
        }
    }

    private fun declineInvite() {
        viewModelScope.launch {
            waitingSyncJob?.cancel()
            waitingSyncJob = null
            _effects.emit(HugsPairingEffect.Close)
        }
    }

    private data class BaseData(
        val userName: String?,
        val activePair: com.example.amulet.shared.domain.hugs.model.Pair?,
    )
}
