package com.example.amulet.shared.di

import com.example.amulet.shared.domain.auth.usecase.SignInUseCase
import com.example.amulet.shared.domain.auth.usecase.SignOutUseCase
import com.example.amulet.shared.domain.hugs.DefaultSendHugUseCase
import com.example.amulet.shared.domain.hugs.SendHugUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

private val sharedModule = module {
    factory<SendHugUseCase> { DefaultSendHugUseCase(get()) }
    factory { SignInUseCase(get(), get()) }
    factory { SignOutUseCase(get()) }
}

fun sharedKoinModules(): List<Module> = listOf(sharedModule)
