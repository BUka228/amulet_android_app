package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult

/**
 * UseCase для явной синхронизации истории «объятий» с сервером.
 */
class SyncHugsUseCase(
    private val repository: HugsRepository,
) {
    suspend operator fun invoke(
        direction: String,
        cursor: String? = null,
        limit: Int? = null,
    ): AppResult<Unit> = repository.syncHugs(direction, cursor, limit)
}
