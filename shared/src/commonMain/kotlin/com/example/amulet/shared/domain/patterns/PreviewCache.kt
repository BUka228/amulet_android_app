package com.example.amulet.shared.domain.patterns

import com.example.amulet.shared.domain.patterns.model.PatternSpec
import java.util.concurrent.ConcurrentHashMap

/**
 * Временное хранилище для передачи PatternSpec между экранами.
 * Используется вместо передачи больших объектов через навигацию.
 * Ключи очищаются после первого получения.
 */
object PreviewCache {
    private val cache = ConcurrentHashMap<String, PatternSpec>()
    
    fun put(key: String, spec: PatternSpec) {
        cache[key] = spec
    }
    
    fun take(key: String): PatternSpec? {
        return cache.remove(key)
    }
}
