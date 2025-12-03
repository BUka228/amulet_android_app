package com.example.amulet.feature.hugs.presentation.pairing

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.hugs.model.Pair
import com.example.amulet.shared.domain.user.model.User

enum class HugsPairingStep {
    SHARE_LINK,
    WAITING_CONFIRMATION,
    CONFIRM_INVITE,
}

data class HugsPairingState(
    val isLoading: Boolean = false,
    val error: AppError? = null,

    val currentUser: User? = null,
    val activePair: Pair? = null,

    val step: HugsPairingStep = HugsPairingStep.SHARE_LINK,
    val inviteLink: String? = null,
    val inviteCode: String? = null,
    val qrCode: androidx.compose.ui.graphics.ImageBitmap? = null,
    val isInviteSent: Boolean = false,
    val isWaitingConfirmation: Boolean = false,
    val isConfirmed: Boolean = false,

    val inviterName: String? = null,
)

sealed class HugsPairingIntent {
    object GoToShareLink : HugsPairingIntent()
    object GoToWaiting : HugsPairingIntent()

    object GenerateInvite : HugsPairingIntent()
    object CopyInviteLink : HugsPairingIntent()
    object ShareInviteLink : HugsPairingIntent()

    object CancelInvite : HugsPairingIntent()

    object AcceptInvite : HugsPairingIntent()
    object DeclineInvite : HugsPairingIntent()
}

sealed class HugsPairingEffect {
    data class ShowError(val error: AppError) : HugsPairingEffect()
    data class CopyToClipboard(val text: String) : HugsPairingEffect()
    data class ShareText(val text: String) : HugsPairingEffect()
    object Close : HugsPairingEffect()
}
