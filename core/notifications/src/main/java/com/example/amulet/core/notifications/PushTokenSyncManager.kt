package com.example.amulet.core.notifications

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.amulet.shared.domain.notifications.SyncPushTokenUseCase
import com.example.amulet.shared.core.logging.Logger

/**
 * Менеджер синхронизации push-токена с бэкендом.
 *
 * Зависит только от :shared (SyncPushTokenUseCase) и OneSignalManager, поэтому может
 * использоваться из core/app без прямой привязки к data/network.
 */
@Singleton
class PushTokenSyncManager @Inject constructor(
    private val oneSignalManager: OneSignalManager,
    private val syncPushTokenUseCase: SyncPushTokenUseCase,
) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        observePlayerId()
    }

    private fun observePlayerId() {
        scope.launch {
            oneSignalManager.playerId()
                .collectLatest { playerId ->
                    Logger.d("Syncing push token: $playerId", tag = TAG)
                    syncPushTokenUseCase(playerId)
                }
        }
    }

    /**
     * Явный триггер синхронизации (например, после смены пользователя).
     */
    fun refresh() {
        scope.launch {
            val current = oneSignalManager.playerId().value
            Logger.d("Manual push token sync: $current", tag = TAG)
            syncPushTokenUseCase(current)
        }
    }

    companion object {
        private const val TAG = "PushTokenSyncManager"
    }
}
