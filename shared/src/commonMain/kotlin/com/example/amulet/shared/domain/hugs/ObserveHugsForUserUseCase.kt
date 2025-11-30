package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.domain.hugs.model.Hug
import com.example.amulet.shared.domain.user.model.UserId
import kotlinx.coroutines.flow.Flow

class ObserveHugsForUserUseCase(
    private val repository: HugsRepository
) {
    operator fun invoke(userId: UserId): Flow<List<Hug>> =
        repository.observeHugsForUser(userId)
}
