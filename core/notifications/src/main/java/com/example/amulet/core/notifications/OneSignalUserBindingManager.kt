package com.example.amulet.core.notifications

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.user.usecase.ObserveCurrentUserUseCase

/**
 * Менеджер, который автоматически привязывает/отвязывает OneSignal-профиль
 * к текущему авторизованному пользователю.
 *
 * Наблюдает за текущим пользователем через ObserveCurrentUserUseCase и вызывает
 * OneSignalManager.login/logout при смене сессии.
 */
@Singleton
class OneSignalUserBindingManager @Inject constructor(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val oneSignalManager: OneSignalManager,
) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        scope.launch {
            observeCurrentUserUseCase()
                .collectLatest { user ->
                    val userId = user?.id?.value
                    if (userId != null) {
                        Logger.d("Binding OneSignal to user=$userId", tag = TAG)
                        oneSignalManager.login(userId)
                    } else {
                        Logger.d("Unbinding OneSignal (no current user)", tag = TAG)
                        oneSignalManager.logout()
                    }
                }
        }
    }

    companion object {
        private const val TAG = "OneSignalUserBindingManager"
    }
}
