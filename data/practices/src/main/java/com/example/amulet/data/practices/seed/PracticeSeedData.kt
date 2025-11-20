package com.example.amulet.data.practices.seed

import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeGoal
import com.example.amulet.shared.domain.practices.model.PracticeLevel
import com.example.amulet.shared.domain.practices.model.PracticeType
import com.example.amulet.shared.domain.patterns.model.PatternId

/**
 * Seed данные практик с привязкой к паттернам
 */
object PracticeSeedData {
    
    fun getPractices(): List<Practice> = listOf(
        // === ДЫХАТЕЛЬНЫЕ ПРАКТИКИ ===
        
        Practice(
            id = "practice_breathing_478",
            type = PracticeType.BREATH,
            title = "Дыхание 4-7-8",
            description = "Классическая техника для быстрого засыпания. Вдох на 4 счета, задержка на 7, выдох на 8. Помогает успокоить нервную систему.",
            durationSec = 300, // 5 минут
            level = PracticeLevel.BEGINNER,
            goal = PracticeGoal.SLEEP,
            tags = listOf("сон", "успокоение", "вечер"),
            contraindications = listOf("Астма в острой фазе", "ХОБЛ"),
            patternId = PatternId("pattern_breathing_calm"),
            audioUrl = null,
            isFavorite = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        
        Practice(
            id = "practice_breathing_box",
            type = PracticeType.BREATH,
            title = "Квадратное дыхание",
            description = "Техника Navy SEAL для снятия стресса. Равные фазы по 4 секунды: вдох, задержка, выдох, задержка. Идеально для быстрого восстановления спокойствия.",
            durationSec = 420, // 7 минут
            level = PracticeLevel.BEGINNER,
            goal = PracticeGoal.STRESS,
            tags = listOf("стресс", "концентрация", "баланс"),
            contraindications = listOf(),
            patternId = PatternId("pattern_antistress_quick"),
            audioUrl = null,
            isFavorite = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        
        Practice(
            id = "practice_breathing_energizing",
            type = PracticeType.BREATH,
            title = "Бодрящее дыхание",
            description = "Активная дыхательная практика для пробуждения. Быстрые вдохи-выдохи повышают уровень энергии и ясность ума.",
            durationSec = 180, // 3 минуты
            level = PracticeLevel.INTERMEDIATE,
            goal = PracticeGoal.ENERGY,
            tags = listOf("энергия", "утро", "бодрость"),
            contraindications = listOf("Гипертония", "Беременность", "Головокружения"),
            patternId = PatternId("pattern_breathing_energy"),
            audioUrl = null,
            isFavorite = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        
        // === МЕДИТАЦИИ ===
        
        Practice(
            id = "practice_meditation_mindfulness",
            type = PracticeType.MEDITATION,
            title = "Осознанность для начинающих",
            description = "Базовая практика майндфулнесс. Фокус на дыхании и наблюдение за мыслями без суждения. Идеально для первого знакомства с медитацией.",
            durationSec = 600, // 10 минут
            level = PracticeLevel.BEGINNER,
            goal = PracticeGoal.RELAXATION,
            tags = listOf("осознанность", "начинающие", "базовая"),
            contraindications = listOf(),
            patternId = PatternId("pattern_meditation_focus"),
            audioUrl = null,
            isFavorite = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        
        Practice(
            id = "practice_meditation_body_scan",
            type = PracticeType.MEDITATION,
            title = "Сканирование тела",
            description = "Постепенное расслабление каждой части тела от головы до пят. Снимает физическое и эмоциональное напряжение.",
            durationSec = 900, // 15 минут
            level = PracticeLevel.BEGINNER,
            goal = PracticeGoal.RELAXATION,
            tags = listOf("расслабление", "тело", "напряжение"),
            contraindications = listOf(),
            patternId = PatternId("pattern_relax_gentle"),
            audioUrl = null,
            isFavorite = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        
        Practice(
            id = "practice_meditation_sleep",
            type = PracticeType.MEDITATION,
            title = "Медитация перед сном",
            description = "Специальная практика для глубокого засыпания. Визуализации и расслабляющие техники помогают отпустить день и погрузиться в сон.",
            durationSec = 1200, // 20 минут
            level = PracticeLevel.INTERMEDIATE,
            goal = PracticeGoal.SLEEP,
            tags = listOf("сон", "вечер", "визуализация"),
            contraindications = listOf(),
            patternId = PatternId("pattern_meditation_sleep"),
            audioUrl = null,
            isFavorite = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        
        Practice(
            id = "practice_meditation_focus",
            type = PracticeType.MEDITATION,
            title = "Медитация для фокуса",
            description = "Тренировка концентрации через фокус на одной точке. Улучшает способность к длительной сосредоточенности.",
            durationSec = 900, // 15 минут
            level = PracticeLevel.INTERMEDIATE,
            goal = PracticeGoal.FOCUS,
            tags = listOf("концентрация", "фокус", "работа"),
            contraindications = listOf(),
            patternId = PatternId("pattern_focus_work"),
            audioUrl = null,
            isFavorite = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        
        // === ЗВУКОВЫЕ ЛАНДШАФТЫ ===
        
        Practice(
            id = "practice_soundscape_ocean",
            type = PracticeType.SOUND,
            title = "Звуки океана",
            description = "Погружение в атмосферу морского прибоя. Ритм волн естественным образом синхронизируется с дыханием, создавая глубокое расслабление.",
            durationSec = 1800, // 30 минут
            level = PracticeLevel.BEGINNER,
            goal = PracticeGoal.RELAXATION,
            tags = listOf("природа", "океан", "расслабление"),
            contraindications = listOf(),
            patternId = PatternId("pattern_soundscape_ocean"),
            audioUrl = "soundscapes/ocean.mp3",
            isFavorite = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        
        Practice(
            id = "practice_soundscape_forest",
            type = PracticeType.SOUND,
            title = "Лесные звуки",
            description = "Пение птиц, шелест листвы, журчание ручья. Создает ощущение присутствия в лесу, снимает городскую усталость.",
            durationSec = 1800, // 30 минут
            level = PracticeLevel.BEGINNER,
            goal = PracticeGoal.STRESS,
            tags = listOf("природа", "лес", "птицы"),
            contraindications = listOf(),
            patternId = PatternId("pattern_soundscape_nature"),
            audioUrl = "soundscapes/forest.mp3",
            isFavorite = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        
        // === СМЕШАННЫЕ ===
        
        Practice(
            id = "practice_mixed_morning",
            type = PracticeType.MEDITATION,
            title = "Утренний ритуал",
            description = "Комбинация бодрящего дыхания, легкой медитации и аффирмаций. Идеальное начало продуктивного дня.",
            durationSec = 600, // 10 минут
            level = PracticeLevel.BEGINNER,
            goal = PracticeGoal.ENERGY,
            tags = listOf("утро", "энергия", "ритуал"),
            contraindications = listOf(),
            patternId = PatternId("pattern_energy_morning"),
            audioUrl = null,
            isFavorite = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        
        Practice(
            id = "practice_mixed_stress_relief",
            type = PracticeType.MEDITATION,
            title = "Экспресс-снятие стресса",
            description = "Быстрая комбинация дыхания и точечного расслабления. Эффективно снимает острый стресс за 5 минут.",
            durationSec = 300, // 5 минут
            level = PracticeLevel.BEGINNER,
            goal = PracticeGoal.STRESS,
            tags = listOf("стресс", "быстро", "работа"),
            contraindications = listOf(),
            patternId = PatternId("pattern_antistress_quick"),
            audioUrl = null,
            isFavorite = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        
        Practice(
            id = "practice_mixed_anxiety",
            type = PracticeType.MEDITATION,
            title = "Работа с тревожностью",
            description = "Специализированная практика для снижения тревоги. Сочетает успокаивающее дыхание, заземление и когнитивные техники.",
            durationSec = 720, // 12 минут
            level = PracticeLevel.INTERMEDIATE,
            goal = PracticeGoal.ANXIETY,
            tags = listOf("тревога", "заземление", "спокойствие"),
            contraindications = listOf(),
            patternId = PatternId("pattern_breathing_calm"),
            audioUrl = null,
            isFavorite = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        
        Practice(
            id = "practice_mixed_mood",
            type = PracticeType.MEDITATION,
            title = "Улучшение настроения",
            description = "Позитивная практика для поднятия духа. Визуализации, дыхание и приятные звуки помогают выйти из подавленного состояния.",
            durationSec = 600, // 10 минут
            level = PracticeLevel.BEGINNER,
            goal = PracticeGoal.MOOD,
            tags = listOf("настроение", "позитив", "радость"),
            contraindications = listOf(),
            patternId = PatternId("pattern_relax_gentle"),
            audioUrl = null,
            isFavorite = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        
        // === ПРОДВИНУТЫЕ ===
        
        Practice(
            id = "practice_advanced_deep_meditation",
            type = PracticeType.MEDITATION,
            title = "Глубокая медитация",
            description = "Продолжительная практика для опытных медитирующих. Глубокие состояния сознания и внутреннее исследование.",
            durationSec = 1800, // 30 минут
            level = PracticeLevel.ADVANCED,
            goal = PracticeGoal.FOCUS,
            tags = listOf("глубокая", "продвинутая", "исследование"),
            contraindications = listOf("Психические расстройства без контроля специалиста"),
            patternId = PatternId("pattern_meditation_focus"),
            audioUrl = null,
            isFavorite = false,
            usageCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    )
}
