package com.example.amulet.data.user

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.privacy.model.UserConsents
import com.example.amulet.shared.domain.user.model.User
import com.example.amulet.shared.domain.user.repository.UserRepository
import com.github.michaelbull.result.Ok
import org.koin.core.module.Module
import org.koin.dsl.module

class UserRepositoryImpl : UserRepository {
    override suspend fun fetchProfile(userId: String): AppResult<User> =
        Ok(
            User(
                id = userId,
                displayName = "Sample User",
                avatarUrl = null,
                consents = UserConsents(analytics = true)
            )
        )
}

val userDataModule: Module = module {
    single<UserRepository> { UserRepositoryImpl() }
}
