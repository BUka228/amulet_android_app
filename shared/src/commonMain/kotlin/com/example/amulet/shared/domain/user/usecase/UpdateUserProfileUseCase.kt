package com.example.amulet.shared.domain.user.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.user.model.UpdateUserProfileRequest
import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.domain.user.repository.UserRepository

/**
 * UseCase для обновления профиля текущего пользователя.
 *
 * Отвечает только за обновление профиля через UserRepository.
 * Актуальное состояние после апдейта UI получает через ObserveCurrentUserUseCase,
 * который читает данные из локального хранилища.
 */
class UpdateUserProfileUseCase(
    private val userRepository: UserRepository,
) {

    suspend operator fun invoke(request: UpdateUserProfileRequest): AppResult<User> {
        return userRepository.updateProfile(request)
    }
}
