package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.model.Emotion
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.hugs.model.PairQuickReply
import com.example.amulet.shared.domain.hugs.model.PairStatus
import com.example.amulet.shared.core.logging.Logger
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
        emotion: Emotion?,
        quickReply: PairQuickReply?,
        payload: Map<String, Any?>?
    ): AppResult<Unit> {
        Logger.d(
            "DefaultSendHugUseCase: start pairId=${pairId?.value} from=${fromUserId.value} to=${toUserId?.value} hasEmotion=${emotion != null} hasQuickReply=${quickReply != null}",
            "DefaultSendHugUseCase"
        )
        // Если нет пары, отправляем без pair-level ограничений.
        if (pairId != null) {
            if (toUserId == null) {
                Logger.e(
                    "DefaultSendHugUseCase: validation error (toUserId is required when pairId is set) pairId=${pairId.value}",
                    throwable = IllegalArgumentException("toUserId is required"),
                    tag = "DefaultSendHugUseCase"
                )
                return Err(AppError.Validation(mapOf("toUserId" to "toUserId is required when pairId is set")))
            }

            val pair = pairsRepository.observePair(pairId).firstOrNull()

            // Блокируем отправку, если пара заблокирована.
            if (pair != null && pair.status == PairStatus.BLOCKED) {
                Logger.d(
                    "DefaultSendHugUseCase: blocked by pair status BLOCKED pairId=${pairId.value}",
                    "DefaultSendHugUseCase"
                )
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
                    Logger.d(
                        "DefaultSendHugUseCase: rate limited maxPerHour=$maxPerHour sentLastHour=$sentToPairLastHour pairId=${pairId.value}",
                        "DefaultSendHugUseCase"
                    )
                    return Err(AppError.RateLimited)
                }
            }
        }

        // На этом уровне домена выбираем эмоцию: явная emotion (из UI/default) приоритетнее quick reply.
        val resolvedEmotion: Emotion = emotion ?: quickReply?.let { reply ->
            if (pairId == null) {
                Logger.e(
                    "DefaultSendHugUseCase: validation error (pairId is required for quick reply)",
                    throwable = IllegalArgumentException("pairId is required for quick reply"),
                    tag = "DefaultSendHugUseCase"
                )
                return Err(AppError.Validation(mapOf("pairId" to "pairId is required for quick reply")))
            }

            val emotions = pairsRepository.observePairEmotions(pairId).first()
            val boundEmotion = emotions.firstOrNull { it.id == reply.emotionId }
                ?: return Err(AppError.Validation(mapOf("emotionId" to "Unknown emotion for quick reply")))

            Emotion(
                colorHex = boundEmotion.colorHex,
                patternId = boundEmotion.patternId,
            )
        } ?: run {
            Logger.e(
                "DefaultSendHugUseCase: validation error (emotion is null). emotion and quickReply are null -> request will NOT reach repository/network",
                throwable = IllegalStateException("emotion is null"),
                tag = "DefaultSendHugUseCase"
            )
            return Err(
                AppError.Validation(
                    mapOf("emotion" to "Hug emotion must not be null: use quick reply or provide explicit emotion"),
                ),
            )
        }

        return hugsRepository.sendHug(
            pairId = pairId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            emotion = resolvedEmotion,
            payload = payload,
        ).let { result ->
            result.component2()?.let { error ->
                Logger.e(
                    "DefaultSendHugUseCase: repository sendHug failed pairId=${pairId?.value} error=$error",
                    throwable = Exception(error.toString()),
                    tag = "DefaultSendHugUseCase"
                )
            } ?: Logger.d(
                "DefaultSendHugUseCase: repository sendHug success pairId=${pairId?.value}",
                "DefaultSendHugUseCase"
            )
            // На всякий случай маппим ошибку PreconditionFailed (например, пара неактивна)
            // в более специфичный Forbidden, если нужно.
            when (val error = result.component2()) {
                is AppError.PreconditionFailed -> Err(AppError.Forbidden)
                else -> result
            }
        }
    }
}
