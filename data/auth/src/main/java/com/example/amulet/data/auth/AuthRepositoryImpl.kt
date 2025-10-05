package com.example.amulet.data.auth

import com.example.amulet.shared.domain.auth.AuthRepository
import org.koin.core.module.Module
import org.koin.dsl.module

class AuthRepositoryImpl : AuthRepository {
    override suspend fun refreshSession(): Result<Unit> = Result.success(Unit)
}

val authDataModule: Module = module {
    single<AuthRepository> { AuthRepositoryImpl() }
}
