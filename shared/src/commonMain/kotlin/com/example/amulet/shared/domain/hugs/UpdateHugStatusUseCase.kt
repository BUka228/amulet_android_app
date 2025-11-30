package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.model.HugId
import com.example.amulet.shared.domain.hugs.model.HugStatus

class UpdateHugStatusUseCase(
    private val repository: HugsRepository
) {
    suspend operator fun invoke(hugId: HugId, status: HugStatus): AppResult<Unit> =
        repository.updateHugStatus(hugId, status)
}
