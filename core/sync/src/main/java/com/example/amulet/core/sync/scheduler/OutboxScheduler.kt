package com.example.amulet.core.sync.scheduler

interface OutboxScheduler {
    fun scheduleSync(expedited: Boolean = false)
}
