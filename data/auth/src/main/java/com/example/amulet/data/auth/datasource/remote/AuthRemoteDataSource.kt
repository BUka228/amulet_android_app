package com.example.amulet.data.auth.datasource.remote

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.auth.model.AuthSession
import com.example.amulet.shared.domain.auth.model.UserCredentials

/**
 * Источник удалённых данных авторизации.
 * Инкапсулирует работу с FirebaseAuth и маппинг ошибок.
 */
interface AuthRemoteDataSource {

    /**
     * Создаёт нового пользователя и возвращает его уникальный идентификатор.
     */
    suspend fun signUp(credentials: UserCredentials): AppResult<AuthSession>

    /**
     * Выполняет аутентификацию пользователя и возвращает его уникальный идентификатор.
     */
    suspend fun signIn(credentials: UserCredentials): AppResult<AuthSession>

    /**
     * Авторизация пользователя по Google ID token. Возвращает firebase uid.
     */
    suspend fun signInWithGoogle(idToken: String): AppResult<AuthSession>

    /**
     * Завершает сессию пользователя на удалённом источнике.
     */
    suspend fun signOut(): AppResult<Unit>
}
