package com.example.amulet.core.foreground

import javax.inject.Inject

/**
 * Оркестратор работы foreground-сервиса.
 * Агрегирует состояние разных активностей (практики, OTA, превью и т.д.)
 * и решает, когда сервис можно безопасно остановить.
 */
class AmuletForegroundOrchestrator @Inject constructor() {

    interface Host {
        fun stopService()
    }

    private var host: Host? = null

    private var hasActivePractice: Boolean = false
    private var hasActiveHug: Boolean = false
    // В будущем сюда добавятся флаги hasActiveOta, hasActivePreview и т.д.

    fun attachHost(host: Host) {
        this.host = host
    }

    fun setPracticeActive(active: Boolean) {
        hasActivePractice = active
        evaluateStop()
    }

    fun setHugActive(active: Boolean) {
        hasActiveHug = active
        evaluateStop()
    }

    private fun evaluateStop() {
        // Пока учитываем только практики и объятия. Позже добавим остальные активности.
        if (!hasActivePractice && !hasActiveHug) {
            host?.stopService()
        }
    }
}
