package com.example.amulet.data.auth

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.auth.AuthRepository
import com.github.michaelbull.result.Ok
import org.koin.core.module.Module
import org.koin.dsl.module

class AuthRepositoryImpl : AuthRepository {
    override suspend fun refreshSession(): AppResult<Unit> = Ok(Unit)
}

val authDataModule: Module = module {
    single<AuthRepository> { AuthRepositoryImpl() }
}
