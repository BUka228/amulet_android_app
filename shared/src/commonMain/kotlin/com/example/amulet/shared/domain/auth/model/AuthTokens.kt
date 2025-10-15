package com.example.amulet.shared.domain.auth.model

import com.example.amulet.shared.domain.user.model.UserId

/**
 * Supabase-аутентификационные токены, используемые для авторизации REST-запросов.
 *
 * @param accessToken активный access JWT.
 * @param refreshToken refresh токен, используемый для обновления access токена.
 * @param expiresAtEpochSeconds момент истечения access токена в секундах UNIX (nullable, если сервер не вернул).
 * @param tokenType тип токена (обычно "bearer"), используется для формирования Authorization заголовка.
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAtEpochSeconds: Long?,
    val tokenType: String?
)

data class AuthSession(
    val userId: UserId,
    val tokens: AuthTokens
)
