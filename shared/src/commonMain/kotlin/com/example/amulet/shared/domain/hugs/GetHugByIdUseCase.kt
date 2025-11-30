package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.model.Hug
import com.example.amulet.shared.domain.hugs.model.HugId

class GetHugByIdUseCase(
    private val repository: HugsRepository,
) {
    suspend operator fun invoke(hugId: HugId): AppResult<Hug> =
        repository.getHugById(hugId)
}
