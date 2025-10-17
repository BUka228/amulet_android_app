package com.example.amulet.data.auth.datasource.remote

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.auth.model.UserCredentials
import com.example.amulet.shared.domain.user.model.UserId

/**
 * Источник удалённых данных авторизации.
 * Инкапсулирует работу с Supabase Auth и маппинг ошибок.
 */
interface AuthRemoteDataSource {

    /**
     * Создаёт нового пользователя и возвращает его уникальный идентификатор.
     */
    suspend fun signUp(credentials: UserCredentials): AppResult<UserId>

    /**
     * Выполняет аутентификацию пользователя и возвращает его уникальный идентификатор.
     */
    suspend fun signIn(credentials: UserCredentials): AppResult<UserId>

    /**
     * Авторизация пользователя по Google ID token. Возвращает user id.
     */
    suspend fun signInWithGoogle(idToken: String, rawNonce: String?): AppResult<UserId>

    /**
     * Завершает сессию пользователя на удалённом источнике.
     */
    suspend fun signOut(): AppResult<Unit>
}
