package com.example.amulet.shared.core.auth

import kotlinx.coroutines.flow.StateFlow

interface UserSessionProvider {
    val sessionContext: StateFlow<UserSessionContext>
    val currentContext: UserSessionContext get() = sessionContext.value
}
