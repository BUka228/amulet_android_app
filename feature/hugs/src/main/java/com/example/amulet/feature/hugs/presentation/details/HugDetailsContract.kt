package com.example.amulet.feature.hugs.presentation.details

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.hugs.model.Hug

data class HugDetailsState(
    val isLoading: Boolean = true,
    val hug: Hug? = null,
    val error: AppError? = null
)

sealed class HugDetailsIntent {
    object Refresh : HugDetailsIntent()
    object Reply : HugDetailsIntent()
    object Delete : HugDetailsIntent()
}

sealed class HugDetailsEffect {
    object NavigateBack : HugDetailsEffect()
    object NavigateToReply : HugDetailsEffect()
    data class ShowError(val error: AppError) : HugDetailsEffect()
}
