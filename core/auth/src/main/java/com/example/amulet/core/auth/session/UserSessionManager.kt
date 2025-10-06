package com.example.amulet.core.auth.session

import com.example.amulet.shared.core.auth.UserSessionProvider
import com.example.amulet.shared.core.auth.UserSessionUpdater

interface UserSessionManager : UserSessionProvider, UserSessionUpdater
