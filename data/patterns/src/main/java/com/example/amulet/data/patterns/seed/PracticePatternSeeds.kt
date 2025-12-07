package com.example.amulet.data.patterns.seed

import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternKind
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.model.PatternTimeline
import com.example.amulet.shared.domain.patterns.model.ReviewStatus
import com.example.amulet.shared.domain.patterns.model.TargetGroup
import com.example.amulet.shared.domain.patterns.model.TargetLed
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
        // Дыхательные практики (базовые циклические паттерны)
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

        // Дыхание 4-7-8: шаговые паттерны под каждый этап скрипта практики
        createBreathing478PreparePattern(),
        createBreathing478InhalePattern(),
        createBreathing478HoldPattern(),
        createBreathing478ExhalePattern(),
        createBreathing478FinishPattern(),
        
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
                durationMs = 19_000,
                loop = true,
                timeline = PatternTimeline(
                    durationMs = 19_000,
                    tracks = listOf(
                        TimelineTrack(
                            target = TargetRing,
                            clips = listOf(
                                // Цикл дыхания 4–7–8: вдох, задержка, выдох
                                TimelineClip(
                                    startMs = 0,
                                    durationMs = 4_000,
                                    color = "#4CAF50", // мягкий зелёный для фазы вдоха
                                    // Вдох: медленное нарастание яркости почти до конца шага
                                    fadeInMs = 3_700,
                                    fadeOutMs = 300
                                ),
                                TimelineClip(
                                    startMs = 11_000,
                                    durationMs = 8_000,
                                    color = "#2196F3", // более холодный оттенок для выдоха
                                    fadeInMs = 300,
                                    fadeOutMs = 7_700
                                )
                            )
                        ),
                        // Индикатор задержки дыхания: поочерёдное зажигание диодов по кругу в течение 7 секунд
                        TimelineTrack(
                            target = TargetLed(index = 0),
                            priority = 1,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 4_000,
                                    durationMs = 7_000,
                                    color = "#FFC107",
                                    fadeInMs = 300,
                                    fadeOutMs = 400
                                )
                            )
                        ),
                        TimelineTrack(
                            target = TargetLed(index = 1),
                            priority = 1,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 4_875,
                                    durationMs = 6_125,
                                    color = "#FFC107",
                                    fadeInMs = 300,
                                    fadeOutMs = 400
                                )
                            )
                        ),
                        TimelineTrack(
                            target = TargetLed(index = 2),
                            priority = 1,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 5_750,
                                    durationMs = 5_250,
                                    color = "#FFC107",
                                    fadeInMs = 300,
                                    fadeOutMs = 400
                                )
                            )
                        ),
                        TimelineTrack(
                            target = TargetLed(index = 3),
                            priority = 1,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 6_625,
                                    durationMs = 4_375,
                                    color = "#FFC107",
                                    fadeInMs = 300,
                                    fadeOutMs = 400
                                )
                            )
                        ),
                        TimelineTrack(
                            target = TargetLed(index = 4),
                            priority = 1,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 7_500,
                                    durationMs = 3_500,
                                    color = "#FFC107",
                                    fadeInMs = 300,
                                    fadeOutMs = 400
                                )
                            )
                        ),
                        TimelineTrack(
                            target = TargetLed(index = 5),
                            priority = 1,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 8_375,
                                    durationMs = 2_625,
                                    color = "#FFC107",
                                    fadeInMs = 300,
                                    fadeOutMs = 400
                                )
                            )
                        ),
                        TimelineTrack(
                            target = TargetLed(index = 6),
                            priority = 1,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 9_250,
                                    durationMs = 1_750,
                                    color = "#FFC107",
                                    fadeInMs = 300,
                                    fadeOutMs = 400
                                )
                            )
                        ),
                        TimelineTrack(
                            target = TargetLed(index = 7),
                            priority = 1,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 10_125,
                                    durationMs = 875,
                                    color = "#FFC107",
                                    fadeInMs = 300,
                                    fadeOutMs = 400
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
            tags = listOf("дыхание", "практика"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

    private fun createBreathing478PreparePattern(): Pattern =
        Pattern(
            id = PatternId("pattern_breathing_478_prepare"),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "BREATHING_478",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 5_000,
                loop = false,
                timeline = PatternTimeline(
                    durationMs = 5_000,
                    tracks = listOf(
                        TimelineTrack(
                            target = TargetRing,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 0,
                                    durationMs = 5_000,
                                    color = "#37474F",
                                    fadeInMs = 1_500,
                                    fadeOutMs = 1_500
                                )
                            )
                        )
                    )
                )
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = "Подготовка к дыханию 4-7-8",
            description = "Мягкое приглушённое свечение для настройки перед практикой",
            tags = listOf("internal_step"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

    private fun createBreathing478InhalePattern(): Pattern =
        Pattern(
            id = PatternId("pattern_breathing_478_inhale"),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "BREATHING_478",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 4_000,
                loop = false,
                timeline = PatternTimeline(
                    durationMs = 4_000,
                    tracks = listOf(
                        TimelineTrack(
                            target = TargetRing,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 0,
                                    durationMs = 4_000,
                                    color = "#4CAF50",
                                    fadeInMs = 3_700,
                                    fadeOutMs = 300
                                )
                            )
                        )
                    )
                )
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = "Дыхание 4-7-8: вдох",
            description = "Медленное нарастание яркости по кольцу на фазе вдоха",
            tags = listOf("internal_step"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

    private fun createBreathing478HoldPattern(): Pattern =
        Pattern(
            id = PatternId("pattern_breathing_478_hold"),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "BREATHING_478",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 7_000,
                loop = false,
                timeline = PatternTimeline(
                    durationMs = 7_000,
                    tracks = listOf(
                        // индикатор задержки: поочерёдное зажигание диодов за 7 секунд
                        TimelineTrack(
                            target = TargetLed(index = 0),
                            priority = 1,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 0,
                                    durationMs = 1_000,
                                    color = "#FFC107",
                                    fadeInMs = 200,
                                    fadeOutMs = 300
                                )
                            )
                        ),
                        TimelineTrack(
                            target = TargetLed(index = 1),
                            priority = 1,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 1_000,
                                    durationMs = 1_000,
                                    color = "#FFC107",
                                    fadeInMs = 200,
                                    fadeOutMs = 300
                                )
                            )
                        ),
                        TimelineTrack(
                            target = TargetLed(index = 2),
                            priority = 1,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 2_000,
                                    durationMs = 1_000,
                                    color = "#FFC107",
                                    fadeInMs = 200,
                                    fadeOutMs = 300
                                )
                            )
                        ),
                        TimelineTrack(
                            target = TargetLed(index = 3),
                            priority = 1,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 3_000,
                                    durationMs = 1_000,
                                    color = "#FFC107",
                                    fadeInMs = 200,
                                    fadeOutMs = 300
                                )
                            )
                        ),
                        TimelineTrack(
                            target = TargetLed(index = 4),
                            priority = 1,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 4_000,
                                    durationMs = 1_000,
                                    color = "#FFC107",
                                    fadeInMs = 200,
                                    fadeOutMs = 300
                                )
                            )
                        ),
                        TimelineTrack(
                            target = TargetLed(index = 5),
                            priority = 1,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 5_000,
                                    durationMs = 1_000,
                                    color = "#FFC107",
                                    fadeInMs = 200,
                                    fadeOutMs = 300
                                )
                            )
                        ),
                        TimelineTrack(
                            target = TargetLed(index = 6),
                            priority = 1,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 6_000,
                                    durationMs = 1_000,
                                    color = "#FFC107",
                                    fadeInMs = 200,
                                    fadeOutMs = 300
                                )
                            )
                        )
                    )
                )
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = "Дыхание 4-7-8: задержка",
            description = "Поочерёдное зажигание диодов в фазе задержки дыхания",
            tags = listOf("internal_step"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

    private fun createBreathing478ExhalePattern(): Pattern =
        Pattern(
            id = PatternId("pattern_breathing_478_exhale"),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "BREATHING_478",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 8_000,
                loop = false,
                timeline = PatternTimeline(
                    durationMs = 8_000,
                    tracks = listOf(
                        TimelineTrack(
                            target = TargetRing,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 0,
                                    durationMs = 8_000,
                                    color = "#2196F3",
                                    fadeInMs = 300,
                                    fadeOutMs = 7_700
                                )
                            )
                        )
                    )
                )
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = "Дыхание 4-7-8: выдох",
            description = "Плавное затухание света в фазе выдоха",
            tags = listOf("internal_step"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

    private fun createBreathing478FinishPattern(): Pattern =
        Pattern(
            id = PatternId("pattern_breathing_478_finish"),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "BREATHING_478",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 5_000,
                loop = false,
                timeline = PatternTimeline(
                    durationMs = 5_000,
                    tracks = listOf(
                        TimelineTrack(
                            target = TargetRing,
                            clips = listOf(
                                TimelineClip(
                                    startMs = 0,
                                    durationMs = 5_000,
                                    color = "#607D8B",
                                    fadeInMs = 500,
                                    fadeOutMs = 2_000
                                )
                            )
                        )
                    )
                )
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = "Дыхание 4-7-8: завершение",
            description = "Мягкое угасание свечения для выхода из практики",
            tags = listOf("internal_step"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    
    private fun createMeditationPattern(id: String, title: String, description: String): Pattern {
        val isSleep = id.contains("sleep")
        val durationMs = if (isSleep) 20_000 else 10_000
        val baseColor = if (isSleep) "#311B92" else "#3F51B5" // более тёмный и глубокий для сна
        val fadeInMs = if (isSleep) 3_000 else 2_000
        val fadeOutMs = if (isSleep) 5_000 else 2_000

        return Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "SOLID",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = durationMs,
                loop = true,
                timeline = PatternTimeline(
                    durationMs = durationMs,
                    tracks = listOf(
                        TimelineTrack(
                            target = TargetRing,
                            priority = 0,
                            clips = listOf(
                                // Плавное «вхождение» и мягкое удержание для фокуса / более длинное угасание для сна
                                TimelineClip(
                                    startMs = 0,
                                    durationMs = durationMs,
                                    color = baseColor,
                                    fadeInMs = fadeInMs,
                                    fadeOutMs = fadeOutMs
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
    }
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
                timeline = PatternTimeline(
                    durationMs = 8_000,
                    tracks = listOf(
                        TimelineTrack(
                            target = TargetRing,
                            clips = listOf(
                                // Нежные волны: мягкий подъём и спад двух близких по тону цветов
                                TimelineClip(
                                    startMs = 0,
                                    durationMs = 4_000,
                                    color = "#00BCD4", // бирюзовый для расслабления
                                    fadeInMs = 1_000,
                                    fadeOutMs = 500
                                ),
                                TimelineClip(
                                    startMs = 4_000,
                                    durationMs = 4_000,
                                    color = "#009688",
                                    fadeInMs = 500,
                                    fadeOutMs = 1_000
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
                timeline = PatternTimeline(
                    durationMs = 30_000,
                    tracks = listOf(
                        // Таймлайн с «перекатывающимися» мягкими пятнами по кольцу
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
                durationMs = 12_000,
                loop = true,
                timeline = PatternTimeline(
                    durationMs = 12_000,
                    tracks = listOf(
                        TimelineTrack(
                            target = TargetRing,
                            clips = listOf(
                                // Бодрящие пульсации: разогрев, пик энергии и мягкий спад
                                TimelineClip(
                                    startMs = 0,
                                    durationMs = 4_000,
                                    color = "#FFC107", // тёплый жёлтый разогрев
                                    fadeInMs = 300,
                                    fadeOutMs = 200
                                ),
                                TimelineClip(
                                    startMs = 4_000,
                                    durationMs = 4_000,
                                    color = "#FF9800", // более яркий оранжевый пик
                                    fadeInMs = 200,
                                    fadeOutMs = 200
                                ),
                                TimelineClip(
                                    startMs = 8_000,
                                    durationMs = 4_000,
                                    color = "#FF5722", // оранжево-красный акцент и мягкий спад
                                    fadeInMs = 200,
                                    fadeOutMs = 400
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
                durationMs = 16_000,
                loop = true,
                timeline = PatternTimeline(
                    durationMs = 16_000,
                    tracks = listOf(
                        TimelineTrack(
                            target = TargetRing,
                            clips = listOf(
                                // Квадратное дыхание: 4 фазы по 4 секунды (вдох, задержка, выдох, пауза)
                                TimelineClip(
                                    startMs = 0,
                                    durationMs = 4_000,
                                    color = "#03A9F4",
                                    // Вдох: медленный подъём яркости до конца шага
                                    fadeInMs = 4_000,
                                    fadeOutMs = 0
                                ),
                                TimelineClip(
                                    startMs = 4_000,
                                    durationMs = 4_000,
                                    color = "#009688",
                                    // Задержка: мягкое удержание и постепенное снижение
                                    fadeInMs = 500,
                                    fadeOutMs = 4_000
                                ),
                                TimelineClip(
                                    startMs = 8_000,
                                    durationMs = 4_000,
                                    color = "#3F51B5",
                                    // Выдох: плавное затухание
                                    fadeInMs = 0,
                                    fadeOutMs = 4_000
                                ),
                                TimelineClip(
                                    startMs = 12_000,
                                    durationMs = 4_000,
                                    color = "#01579B",
                                    // Пауза: свет практически гаснет к концу фазы
                                    fadeInMs = 0,
                                    fadeOutMs = 4_000
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
                timeline = PatternTimeline(
                    durationMs = 15_000,
                    tracks = listOf(
                        // Статичное, но слегка «живое» свечение для фокуса
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
