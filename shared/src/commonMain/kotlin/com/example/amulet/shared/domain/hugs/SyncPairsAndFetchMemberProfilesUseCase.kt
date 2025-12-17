package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.user.usecase.ObserveCurrentUserUseCase
import com.example.amulet.shared.domain.user.usecase.FetchUserProfileUseCase
import com.example.amulet.shared.domain.patterns.usecase.EnsurePatternLoadedUseCase
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

class SyncPairsAndFetchMemberProfilesUseCase(
    private val syncPairsUseCase: SyncPairsUseCase,
    private val pairsRepository: PairsRepository,
    private val observePairsUseCase: ObservePairsUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val fetchUserProfileUseCase: FetchUserProfileUseCase,
    private val ensurePatternLoadedUseCase: EnsurePatternLoadedUseCase,
) {
    suspend operator fun invoke(): AppResult<Unit> {
        val syncResult = syncPairsUseCase()
        val syncError = syncResult.component2()

        // Best-effort: даже при ошибке синка пробуем подтянуть профили из текущих локальных пар.
        runCatching {
            val currentUserId = observeCurrentUserUseCase().first()?.id
            val pairs = observePairsUseCase().first()

            val memberIds = pairs
                .flatMap { it.members }
                .map { it.userId }
                .distinct()
                .filterNot { it == currentUserId }

            if (memberIds.isNotEmpty()) {
                coroutineScope {
                    memberIds
                        .map { userId ->
                            async {
                                fetchUserProfileUseCase(userId)
                            }
                        }
                        .awaitAll()
                }
            }
        }.onFailure { t ->
            Logger.e(
                "SyncPairsAndFetchMemberProfilesUseCase: fetch profiles failed (best-effort)",
                throwable = t,
                tag = "SyncPairsAndFetchMemberProfilesUseCase"
            )
        }

        // Best-effort: синхронизируем эмоции пар и догружаем паттерны, чтобы не падать по FK.
        runCatching {
            val pairs = observePairsUseCase().first()
            for (pair in pairs) {
                val emotionsResult = pairsRepository.fetchPairEmotionsFromRemote(pair.id)
                val remoteEmotions = emotionsResult.component1() ?: continue

                val ensuredPatternIds = mutableSetOf<String>()
                val safeEmotions = remoteEmotions.map { emotion ->
                    val patternId = emotion.patternId ?: return@map emotion
                    if (ensuredPatternIds.add(patternId.value)) {
                        val ensured = ensurePatternLoadedUseCase(patternId)
                        if (ensured.component2() != null) {
                            return@map emotion.copy(patternId = null)
                        }
                    }
                    emotion
                }

                pairsRepository.upsertPairEmotionsLocal(pair.id, safeEmotions)
            }
        }.onFailure { t ->
            Logger.e(
                "SyncPairsAndFetchMemberProfilesUseCase: sync pair emotions/patterns failed (best-effort)",
                throwable = t,
                tag = "SyncPairsAndFetchMemberProfilesUseCase"
            )
        }

        return if (syncError != null) Err(syncError) else Ok(Unit)
    }
}
