package com.example.amulet.shared.domain.patterns.model

/**
 * Результат синхронизации паттернов с облаком.
 */
data class SyncResult(
    val patternsAdded: Int,
    val patternsUpdated: Int,
    val patternsDeleted: Int
)
