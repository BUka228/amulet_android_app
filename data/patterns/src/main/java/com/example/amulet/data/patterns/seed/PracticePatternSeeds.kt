package com.example.amulet.data.patterns.seed

import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternElementBreathing
import com.example.amulet.shared.domain.patterns.model.PatternElementPulse
import com.example.amulet.shared.domain.patterns.model.PatternElementTimeline
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternKind
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.model.ReviewStatus
import com.example.amulet.shared.domain.patterns.model.TargetGroup
import com.example.amulet.shared.domain.patterns.model.TargetRing
import com.example.amulet.shared.domain.patterns.model.TimelineClip
import com.example.amulet.shared.domain.patterns.model.TimelineTrack
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
                elements = listOf(
                    // 4 цикла «вдох-выдох» по 3 секунды каждый
                    PatternElementBreathing(
                        color = "#4CAF50", // мягкий зелёный для успокоения
                        durationMs = 3_000
                    ),
                    PatternElementBreathing(
                        color = "#2196F3", // более холодный оттенок — углубление дыхания
                        durationMs = 3_000
                    ),
                    PatternElementBreathing(
                        color = "#4CAF50",
                        durationMs = 3_000
                    ),
                    PatternElementBreathing(
                        color = "#2196F3",
                        durationMs = 3_000
                    )
                )
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("дыхание", "практика"),
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
                elements = listOf(
                    // Плавное «вхождение» в состояние и удержание мягкого свечения
                    PatternElementTimeline(
                        durationMs = 10_000,
                        tickMs = 200,
                        tracks = listOf(
                            TimelineTrack(
                                target = TargetRing,
                                priority = 0,
                                clips = listOf(
                                    TimelineClip(
                                        startMs = 0,
                                        durationMs = 10_000,
                                        color = "#3F51B5", // глубокий синий для фокуса/медитации
                                        fadeInMs = 2_000,
                                        fadeOutMs = 2_000
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("медитация", "практика"),
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
                elements = listOf(
                    // Серия мягких пульсаций с постепенным замедлением
                    PatternElementPulse(
                        color = "#00BCD4", // бирюзовый для расслабления
                        speed = 400,
                        repeats = 6
                    ),
                    PatternElementPulse(
                        color = "#009688",
                        speed = 600,
                        repeats = 4
                    )
                )
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("расслабление", "практика"),
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
                elements = listOf(
                    // Таймлайн с «перекатывающимися» мягкими пятнами по кольцу
                    PatternElementTimeline(
                        durationMs = 30_000,
                        tickMs = 150,
                        tracks = listOf(
                            TimelineTrack(
                                target = TargetGroup(indices = listOf(0, 1, 2)),
                                priority = 0,
                                clips = listOf(
                                    TimelineClip(
                                        startMs = 0,
                                        durationMs = 10_000,
                                        color = "#4CAF50",
                                        fadeInMs = 1_000,
                                        fadeOutMs = 1_000
                                    )
                                )
                            ),
                            TimelineTrack(
                                target = TargetGroup(indices = listOf(3, 4, 5)),
                                priority = 1,
                                clips = listOf(
                                    TimelineClip(
                                        startMs = 8_000,
                                        durationMs = 10_000,
                                        color = "#2196F3",
                                        fadeInMs = 1_000,
                                        fadeOutMs = 1_000
                                    )
                                )
                            ),
                            TimelineTrack(
                                target = TargetGroup(indices = listOf(6, 7)),
                                priority = 2,
                                clips = listOf(
                                    TimelineClip(
                                        startMs = 16_000,
                                        durationMs = 10_000,
                                        color = "#8BC34A",
                                        fadeInMs = 1_000,
                                        fadeOutMs = 1_000
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("звуки", "практика"),
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
                elements = listOf(
                    // Быстрые бодрящие пульсации
                    PatternElementPulse(
                        color = "#FFC107", // тёплый жёлтый
                        speed = 250,
                        repeats = 12
                    ),
                    PatternElementPulse(
                        color = "#FF5722", // оранжево-красный акцент
                        speed = 200,
                        repeats = 10
                    )
                )
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("энергия", "практика"),
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
                elements = listOf(
                    // Более глубокий цикл дыхания для антистресс‑эффекта
                    PatternElementBreathing(
                        color = "#03A9F4",
                        durationMs = 4_000
                    ),
                    PatternElementBreathing(
                        color = "#009688",
                        durationMs = 4_000
                    ),
                    PatternElementBreathing(
                        color = "#3F51B5",
                        durationMs = 4_000
                    )
                )
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("стресс", "практика"),
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
                elements = listOf(
                    // Статичное, но слегка «живое» свечение для фокуса
                    PatternElementTimeline(
                        durationMs = 15_000,
                        tickMs = 250,
                        tracks = listOf(
                            TimelineTrack(
                                target = TargetRing,
                                priority = 0,
                                clips = listOf(
                                    TimelineClip(
                                        startMs = 0,
                                        durationMs = 15_000,
                                        color = "#2196F3",
                                        fadeInMs = 1_500,
                                        fadeOutMs = 1_500
                                    )
                                )
                            ),
                            // Лёгкие акценты на передней части кольца
                            TimelineTrack(
                                target = TargetGroup(indices = listOf(0, 1)),
                                priority = 1,
                                clips = listOf(
                                    TimelineClip(
                                        startMs = 5_000,
                                        durationMs = 3_000,
                                        color = "#64B5F6",
                                        fadeInMs = 500,
                                        fadeOutMs = 500
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("фокус", "практика"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
}
