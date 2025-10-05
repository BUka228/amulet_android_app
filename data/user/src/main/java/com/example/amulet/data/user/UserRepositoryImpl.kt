package com.example.amulet.data.user

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.user.UserRepository
import com.github.michaelbull.result.Ok
import org.koin.core.module.Module
import org.koin.dsl.module

class UserRepositoryImpl : UserRepository {
    override suspend fun fetchProfile(): AppResult<Unit> = Ok(Unit)
}

val userDataModule: Module = module {
    single<UserRepository> { UserRepositoryImpl() }
}
