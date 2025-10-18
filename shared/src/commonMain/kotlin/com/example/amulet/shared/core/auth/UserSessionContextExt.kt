package com.example.amulet.shared.core.auth

/**
 * Проверяет, является ли текущая сессия гостевой
 */
val UserSessionContext.isGuest: Boolean
    get() = this is UserSessionContext.Guest

/**
 * Проверяет, является ли пользователь авторизованным (не гость и не LoggedOut)
 */
val UserSessionContext.isAuthenticated: Boolean
    get() = this is UserSessionContext.LoggedIn

/**
 * Проверяет, требуется ли авторизация (LoggedOut или Loading)
 */
val UserSessionContext.requiresAuth: Boolean
    get() = this is UserSessionContext.LoggedOut || this is UserSessionContext.Loading

/**
 * Возвращает отображаемое имя пользователя или null
 */
val UserSessionContext.displayNameOrNull: String?
    get() = when (this) {
        is UserSessionContext.Guest -> displayName
        is UserSessionContext.LoggedIn -> displayName
        else -> null
    }

/**
 * Возвращает язык пользователя или null
 */
val UserSessionContext.languageOrNull: String?
    get() = when (this) {
        is UserSessionContext.Guest -> language
        is UserSessionContext.LoggedIn -> language
        else -> null
    }

/**
 * Проверяет, имеет ли пользователь доступ к функции (авторизован, но не гость)
 */
val UserSessionContext.hasFullAccess: Boolean
    get() = this is UserSessionContext.LoggedIn
