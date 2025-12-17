package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.hugs.model.Hug
import com.example.amulet.shared.domain.patterns.usecase.EnsurePatternLoadedUseCase
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok

class SyncHugsAndEnsurePatternsUseCase(
    private val hugsRepository: HugsRepository,
    private val ensurePatternLoadedUseCase: EnsurePatternLoadedUseCase,
) {
    suspend operator fun invoke(
        direction: String,
        cursor: String? = null,
        limit: Int? = null,
    ): AppResult<Unit> {
        val remoteResult = hugsRepository.fetchHugsFromRemote(
            direction = direction,
            cursor = cursor,
            limit = limit,
        )

        val remoteHugs = remoteResult.component1().orEmpty()
        val remoteError = remoteResult.component2()

        if (remoteHugs.isNotEmpty()) {
            val ensuredPatternIds = mutableSetOf<String>()
            val unavailablePatternIds = mutableSetOf<String>()
            val safeHugs: List<Hug> = remoteHugs.map { hug ->
                val patternId = hug.emotion.patternId ?: return@map hug
                if (unavailablePatternIds.contains(patternId.value)) {
                    return@map hug.copy(emotion = hug.emotion.copy(patternId = null))
                }
                if (ensuredPatternIds.add(patternId.value)) {
                    val ensured = ensurePatternLoadedUseCase(patternId)
                    if (ensured.component2() != null) {
                        Logger.d(
                            "SyncHugsAndEnsurePatternsUseCase: pattern not available -> save hug without patternId patternId=${patternId.value}",
                            "SyncHugsAndEnsurePatternsUseCase"
                        )
                        unavailablePatternIds.add(patternId.value)
                        return@map hug.copy(emotion = hug.emotion.copy(patternId = null))
                    }
                }
                hug
            }

            val upsertResult = hugsRepository.upsertHugsLocal(safeHugs)
            val upsertError = upsertResult.component2()
            if (upsertError != null) return Err(upsertError)
        }

        return if (remoteError != null) Err(remoteError) else Ok(Unit)
    }
}
