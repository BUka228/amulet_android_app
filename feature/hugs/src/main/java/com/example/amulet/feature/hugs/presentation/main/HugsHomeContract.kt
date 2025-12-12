package com.example.amulet.feature.hugs.presentation.main

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.hugs.model.Hug
import com.example.amulet.shared.domain.hugs.model.Pair
import com.example.amulet.shared.domain.hugs.model.PairQuickReply
import com.example.amulet.shared.domain.user.model.User

data class HugsHomeState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val currentUser: User? = null,
    val activePair: Pair? = null,
    val partnerUser: User? = null,
    val hugs: List<Hug> = emptyList(),
    val quickReplies: List<PairQuickReply> = emptyList(),
    val isRefreshing: Boolean = false,
    val isSending: Boolean = false,
)

sealed class HugsHomeIntent {
    object Refresh : HugsHomeIntent()
    object SendHug : HugsHomeIntent()
    object OpenHistory : HugsHomeIntent()
    object OpenSettings : HugsHomeIntent()
    object OpenEmotions : HugsHomeIntent()
    object OpenSecretCodes : HugsHomeIntent()
    object OpenPairing : HugsHomeIntent()
    object UnblockPair : HugsHomeIntent()
}

sealed class HugsHomeEffect {
    object NavigateToHistory : HugsHomeEffect()
    object NavigateToSettings : HugsHomeEffect()
    object NavigateToEmotions : HugsHomeEffect()
    object NavigateToSecretCodes : HugsHomeEffect()
    object NavigateToPairing : HugsHomeEffect()
    data class ShowError(val error: AppError) : HugsHomeEffect()
}
