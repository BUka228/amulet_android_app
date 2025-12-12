package com.example.amulet.shared.domain.user.usecase

import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.domain.user.model.UserId
import com.example.amulet.shared.domain.user.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class ObserveUserByIdUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: UserId): Flow<User?> =
        userRepository.observeUser(userId)
}
