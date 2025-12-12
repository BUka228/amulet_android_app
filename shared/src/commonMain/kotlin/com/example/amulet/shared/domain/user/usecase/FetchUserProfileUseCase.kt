package com.example.amulet.shared.domain.user.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.domain.user.model.UserId
import com.example.amulet.shared.domain.user.repository.UserRepository

class FetchUserProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: UserId): AppResult<User> =
        userRepository.fetchProfile(userId)
}
