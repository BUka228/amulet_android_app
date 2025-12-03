package com.example.amulet.feature.hugs.presentation.history

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.hugs.model.Hug
import com.example.amulet.shared.domain.hugs.model.Pair
import com.example.amulet.shared.domain.user.model.User

enum class HugsHistoryDirectionFilter {
    ALL, SENT, RECEIVED
}

enum class HugsHistoryPeriodFilter {
    ALL_TIME, LAST_7_DAYS, LAST_24_HOURS
}

/**
 * Экран истории объятий для активной пары.
 */
data class HugsHistoryState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: AppError? = null,
    val currentUser: User? = null,
    val activePair: Pair? = null,
    val hugs: List<Hug> = emptyList(),
    val pinnedIds: Set<String> = emptySet(),
    val directionFilter: HugsHistoryDirectionFilter = HugsHistoryDirectionFilter.ALL,
    val periodFilter: HugsHistoryPeriodFilter = HugsHistoryPeriodFilter.ALL_TIME,
    val selectedEmotionKey: String? = null,
)

sealed class HugsHistoryIntent {
    object Refresh : HugsHistoryIntent()
    data class ChangeDirection(val filter: HugsHistoryDirectionFilter) : HugsHistoryIntent()
    data class ChangePeriod(val filter: HugsHistoryPeriodFilter) : HugsHistoryIntent()
    data class SelectEmotion(val key: String?) : HugsHistoryIntent()
    data class TogglePin(val hugId: String) : HugsHistoryIntent()
    data class OpenDetails(val hugId: String) : HugsHistoryIntent()
}

sealed class HugsHistoryEffect {
    data class ShowError(val error: AppError) : HugsHistoryEffect()
    data class OpenDetails(val hugId: String) : HugsHistoryEffect()
}
