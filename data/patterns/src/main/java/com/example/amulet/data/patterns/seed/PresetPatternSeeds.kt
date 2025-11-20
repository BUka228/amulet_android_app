package com.example.amulet.data.patterns.seed

import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternKind
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.model.ReviewStatus
import com.example.amulet.shared.domain.user.model.UserId

/**
 * Preset паттерны для общего использования амулета
 * (не связанные с практиками и курсами)
 */
object PresetPatternSeeds {
    
    private const val HARDWARE_VERSION = 1
    
    fun getPresetPatterns(): List<Pattern> = listOf(
        // === УВЕДОМЛЕНИЯ ===
        createNotification(
            id = "preset_notification_gentle",
            title = "Мягкое уведомление",
            description = "Деликатная пульсация для ненавязчивых напоминаний"
        ),
        createNotification(
            id = "preset_notification_urgent",
            title = "Важное уведомление",
            description = "Яркая быстрая пульсация для срочных сообщений"
        ),
        createNotification(
            id = "preset_notification_call",
            title = "Входящий звонок",
            description = "Ритмичная пульсация имитирующая вибрацию звонка"
        ),
        
        // === РАБОТА И ФОКУС ===
        createWorkFocus(
            id = "preset_work_pomodoro",
            title = "Помодоро таймер",
            description = "25-минутные интервалы работы с визуальной индикацией"
        ),
        createWorkFocus(
            id = "preset_work_deep_focus",
            title = "Глубокий фокус",
            description = "Спокойное синее свечение для длительной концентрации"
        ),
        createWorkFocus(
            id = "preset_work_break",
            title = "Перерыв",
            description = "Зелёное свечение - сигнал о начале отдыха"
        ),
        
        // === ПРАЗДНИК И РАДОСТЬ ===
        createCelebration(
            id = "preset_celebration_party",
            title = "Вечеринка",
            description = "Радужная смена цветов для праздничного настроения"
        ),
        createCelebration(
            id = "preset_celebration_achievement",
            title = "Достижение",
            description = "Золотая вспышка для отметки успехов"
        ),
        createCelebration(
            id = "preset_celebration_birthday",
            title = "День рождения",
            description = "Весёлое мерцание свечей на торте"
        ),
        
        // === НАСТРОЕНИЕ ===
        createMood(
            id = "preset_mood_calm",
            title = "Спокойствие",
            description = "Мягкое голубое свечение для умиротворения"
        ),
        createMood(
            id = "preset_mood_energetic",
            title = "Энергия",
            description = "Яркое оранжевое свечение для бодрости"
        ),
        createMood(
            id = "preset_mood_romantic",
            title = "Романтика",
            description = "Нежное розовое свечение для романтических моментов"
        ),
        createMood(
            id = "preset_mood_mysterious",
            title = "Мистика",
            description = "Фиолетовое мерцание для загадочной атмосферы"
        ),
        
        // === СОН ===
        createSleep(
            id = "preset_sleep_bedtime",
            title = "Время спать",
            description = "Постепенно угасающее тёплое свечение"
        ),
        createSleep(
            id = "preset_sleep_night_light",
            title = "Ночник",
            description = "Очень тусклое постоянное свечение на всю ночь"
        ),
        createSleep(
            id = "preset_sleep_wake_up",
            title = "Пробуждение",
            description = "Плавно нарастающий рассвет для естественного пробуждения"
        ),
        
        // === ТАЙМЕРЫ ===
        createTimer(
            id = "preset_timer_countdown",
            title = "Обратный отсчёт",
            description = "Визуальный таймер с изменением цвета"
        ),
        createTimer(
            id = "preset_timer_stopwatch",
            title = "Секундомер",
            description = "Пульсация на каждую минуту"
        ),
        
        // === СОЦИАЛЬНОЕ ===
        createSocial(
            id = "preset_social_meeting",
            title = "Встреча",
            description = "Напоминание о встрече или событии"
        ),
        createSocial(
            id = "preset_social_message",
            title = "Новое сообщение",
            description = "Быстрая вспышка при получении сообщения"
        ),
        
        // === ЗДОРОВЬЕ ===
        createHealth(
            id = "preset_health_drink_water",
            title = "Выпить воды",
            description = "Голубая пульсация - напоминание о гидратации"
        ),
        createHealth(
            id = "preset_health_stretch",
            title = "Размяться",
            description = "Зелёные волны - пора подвигаться"
        ),
        
        // === КРЕАТИВНОСТЬ ===
        createCreative(
            id = "preset_creative_inspiration",
            title = "Вдохновение",
            description = "Плавно меняющиеся цвета для креативного потока"
        ),
        createCreative(
            id = "preset_creative_flow",
            title = "Творческий поток",
            description = "Динамичные волны для погружения в творчество"
        )
    )
    
    // === NOTIFICATION PATTERNS ===
    
    private fun createNotification(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "NOTIFICATION",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 3_000,
                loop = true,
                elements = emptyList()
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("notification", "preset"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    
    // === WORK & FOCUS PATTERNS ===
    
    private fun createWorkFocus(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "WORK_FOCUS",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 25 * 60_000,
                loop = true,
                elements = emptyList()
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("work", "focus", "preset"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    
    // === CELEBRATION PATTERNS ===
    
    private fun createCelebration(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "CELEBRATION",
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
            tags = listOf("celebration", "party", "preset"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    
    // === MOOD PATTERNS ===
    
    private fun createMood(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "MOOD",
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
            tags = listOf("mood", "preset"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    
    // === SLEEP PATTERNS ===
    
    private fun createSleep(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "SLEEP",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 15 * 60_000,
                loop = false,
                elements = emptyList()
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("sleep", "preset"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    
    // === TIMER PATTERNS ===
    
    private fun createTimer(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "TIMER",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 60_000,
                loop = true,
                elements = emptyList()
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("timer", "preset"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    
    // === SOCIAL PATTERNS ===
    
    private fun createSocial(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "SOCIAL",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 5_000,
                loop = true,
                elements = emptyList()
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("social", "notification", "preset"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    
    // === HEALTH PATTERNS ===
    
    private fun createHealth(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "HEALTH",
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
            tags = listOf("health", "wellness", "preset"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    
    // === CREATIVE PATTERNS ===
    
    private fun createCreative(id: String, title: String, description: String): Pattern =
        Pattern(
            id = PatternId(id),
            version = 1,
            ownerId = null,
            kind = PatternKind.LIGHT,
            spec = PatternSpec(
                type = "CREATIVE",
                hardwareVersion = HARDWARE_VERSION,
                durationMs = 20_000,
                loop = true,
                elements = emptyList()
            ),
            public = true,
            reviewStatus = ReviewStatus.APPROVED,
            hardwareVersion = HARDWARE_VERSION,
            title = title,
            description = description,
            tags = listOf("creative", "inspiration", "preset"),
            usageCount = 0,
            sharedWith = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
}
