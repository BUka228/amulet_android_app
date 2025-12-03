package com.example.amulet.feature.hugs.presentation.secretcodes

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.patterns.model.Pattern

data class HugsSecretCodesState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val codes: List<Pattern> = emptyList(),
)

sealed class HugsSecretCodesIntent {
    object Refresh : HugsSecretCodesIntent()
    data class OpenCode(val patternId: String) : HugsSecretCodesIntent()
}

sealed class HugsSecretCodesEffect {
    data class ShowError(val error: AppError) : HugsSecretCodesEffect()
    data class OpenPatternDetails(val patternId: String) : HugsSecretCodesEffect()
}
