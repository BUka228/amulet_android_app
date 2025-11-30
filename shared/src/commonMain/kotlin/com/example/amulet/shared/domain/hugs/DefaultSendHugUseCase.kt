package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.hugs.model.PairQuickReply
import com.example.amulet.shared.domain.hugs.model.PairStatus
import com.example.amulet.shared.domain.user.model.UserId
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

/**
 * Реализация use case отправки «объятий» с учётом настроек пары и анти-спам лимитов.
 */
@OptIn(ExperimentalTime::class)
class DefaultSendHugUseCase(
    private val hugsRepository: HugsRepository,
    private val pairsRepository: PairsRepository,
) : SendHugUseCase {

    override suspend operator fun invoke(
        pairId: PairId?,
        fromUserId: UserId,
        toUserId: UserId?,
        quickReply: PairQuickReply?,
        payload: Map<String, Any?>?
    ): AppResult<Unit> {
        // Если нет пары, отправляем без pair-level ограничений.
        if (pairId != null) {
            val pair = pairsRepository.observePair(pairId).firstOrNull()

            // Блокируем отправку, если пара заблокирована.
            if (pair != null && pair.status == PairStatus.BLOCKED) {
                return Err(AppError.Forbidden)
            }

            val member = pair?.members?.firstOrNull { it.userId == fromUserId }
            val settings = member?.settings

            // Анти-спам: ограничение по количеству объятий в час для участника пары.
            val maxPerHour = settings?.maxHugsPerHour
            if (maxPerHour != null && maxPerHour > 0) {
                val now = Clock.System.now()
                val threshold = now - 1.hours

                val recentHugs = hugsRepository.observeHugsForUser(fromUserId).first()
                val sentToPairLastHour = recentHugs.count { hug ->
                    hug.fromUserId == fromUserId &&
                        hug.pairId == pairId &&
                        hug.createdAt >= threshold
                }

                if (sentToPairLastHour >= maxPerHour) {
                    return Err(AppError.RateLimited)
                }
            }
        }

        return hugsRepository.sendHug(
            pairId = pairId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            quickReply = quickReply,
            payload = payload,
        ).let { result ->
            // На всякий случай маппим ошибку PreconditionFailed (например, пара неактивна)
            // в более специфичный Forbidden, если нужно.
            when (val error = result.component2()) {
                is AppError.PreconditionFailed -> Err(AppError.Forbidden)
                else -> result
            }
        }
    }
}
