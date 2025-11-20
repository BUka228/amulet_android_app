package com.example.amulet.data.patterns.seed

import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternKind
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.model.ReviewStatus
import com.example.amulet.shared.domain.user.model.UserId

/**
 * Seed паттерны для практик
 */
object PracticePatternSeeds {
    
    private val systemUserId = UserId("system")
    private const val HARDWARE_VERSION = 100

    fun getPatterns(): List<Pattern> = listOf(
        // Дыхательные практики
        createBreathingPattern(
            id = "pattern_breathing_calm",
            title = "Спокойное дыхание",
            description = "Мягкое успокаивающее свечение для дыхательных упражнений"
        ),
        createBreathingPattern(
            id = "pattern_breathing_energy",
            title = "Энергичное дыхание",
            description = "Яркие пульсации для бодрящих дыхательных практик"
        ),
        
        // Медитации
        createMeditationPattern(
            id = "pattern_meditation_focus",
            title = "Фокус медитация",
            description = "Постоянное мягкое свечение для концентрации"
        ),
        createMeditationPattern(
            id = "pattern_meditation_sleep",
            title = "Сон медитация",
            description = "Плавно угасающее свечение для засыпания"
        ),
        
        // Расслабление
        createRelaxationPattern(
            id = "pattern_relax_gentle",
            title = "Мягкое расслабление",
            description = "Нежные волны света для глубокого расслабления"
        ),
        
        // Звуковые ландшафты  
        createSoundscapePattern(
            id = "pattern_soundscape_nature",
            title = "Природа",
            description = "Органичные волны для звуков природы"
        ),
        createSoundscapePattern(
            id = "pattern_soundscape_ocean",
            title = "Океан",
            description = "Плавные волны как прибой океана"
        ),
        
        // Энергия
        createEnergyPattern(
            id = "pattern_energy_morning",
            title = "Утренняя энергия",
            description = "Яркое пробуждающее свечение"
        ),
        
        // Анти-стресс
        createAntiStressPattern(
            id = "pattern_antistress_quick",
            title = "Быстрое снятие стресса",
            description = "Ритмичные импульсы для быстрого снятия напряжения"
        ),
        
        // Фокус
        createFocusPattern(
            id = "pattern_focus_work",
            title = "Рабочий фокус",
            description = "Стабильное свечение для продуктивности"
        )
    )
    
    private fun createBreathingPattern(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null, // системный паттерн
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "BREATHING",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 12_000,
                loop = true,
                elements = emptyList()
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("breathing", "practice"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    
    private fun createMeditationPattern(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "SOLID",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 10_000,
                loop = true,
                elements = emptyList()
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("meditation", "practice"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    
    private fun createRelaxationPattern(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "PULSE",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 8_000,
                loop = true,
                elements = emptyList()
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("relaxation", "practice"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    
    private fun createSoundscapePattern(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "SOUNDSCAPE",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 30_000,
                loop = true,
                elements = emptyList()
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("soundscape", "practice"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    
    private fun createEnergyPattern(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "ENERGY",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 10_000,
                loop = true,
                elements = emptyList()
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("energy", "practice"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    
    private fun createAntiStressPattern(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "BREATHING_ANTI_STRESS",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 12_000,
                loop = true,
                elements = emptyList()
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("stress", "practice"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    
    private fun createFocusPattern(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "FOCUS",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 15_000,
                loop = true,
                elements = emptyList()
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("focus", "practice"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
}
