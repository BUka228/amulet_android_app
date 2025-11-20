package com.example.amulet.data.courses.seed

import com.example.amulet.shared.domain.courses.model.CourseItemType
import com.example.amulet.shared.domain.courses.model.CourseRhythm
import com.example.amulet.shared.domain.practices.model.PracticeGoal
import com.example.amulet.shared.domain.practices.model.PracticeLevel

/**
 * Seed данные курсов с привязкой к практикам
 */
object CourseSeedData {
    
    fun getCourses(): List<CourseSeed> = listOf(
        // === КУРС 1: Сон за 7 дней ===
        CourseSeed(
            id = "course_sleep_7days",
            title = "Сон за 7 дней",
            description = "Программа для восстановления здорового сна. За неделю вы освоите техники быстрого засыпания и глубокого сна.",
            goal = PracticeGoal.SLEEP,
            level = PracticeLevel.BEGINNER,
            rhythm = CourseRhythm.DAILY,
            tags = listOf("сон", "начинающие", "вечер"),
            totalDurationSec = 7200, // ~2 часа total
            modulesCount = 7,
            recommendedDays = 7,
            difficulty = "easy",
            coverUrl = null
        ),
        
        // === КУРС 2: Снижение стресса ===
        CourseSeed(
            id = "course_stress_relief",
            title = "Снижение стресса",
            description = "10-дневная программа для развития стрессоустойчивости. Научитесь быстро восстанавливаться и сохранять спокойствие.",
            goal = PracticeGoal.STRESS,
            level = PracticeLevel.BEGINNER,
            rhythm = CourseRhythm.DAILY,
            tags = listOf("стресс", "работа", "баланс"),
            totalDurationSec = 6000, // ~1.7 часа total
            modulesCount = 10,
            recommendedDays = 10,
            difficulty = "easy",
            coverUrl = null
        ),
        
        // === КУРС 3: Фокус и концентрация ===
        CourseSeed(
            id = "course_focus_concentration",
            title = "Фокус и концентрация",
            description = "Тренировка внимания и способности к глубокой концентрации. Идеально для работы и учебы в две недели.",
            goal = PracticeGoal.FOCUS,
            level = PracticeLevel.INTERMEDIATE,
            rhythm = CourseRhythm.THREE_TIMES_WEEK,
            tags = listOf("фокус", "работа", "продуктивность"),
            totalDurationSec = 5400, // ~1.5 часа total
            modulesCount = 6,
            recommendedDays = 14,
            difficulty = "medium",
            coverUrl = null
        ),
        
        // === КУРС 4: Энергия на весь день ===
        CourseSeed(
            id = "course_energy_boost",
            title = "Энергия на весь день",
            description = "5-дневный курс по повышению жизненной энергии. Утренние и дневные практики для бодрости без кофеина.",
            goal = PracticeGoal.ENERGY,
            level = PracticeLevel.BEGINNER,
            rhythm = CourseRhythm.DAILY,
            tags = listOf("энергия", "утро", "бодрость"),
            totalDurationSec = 2700, // ~45 минут total
            modulesCount = 5,
            recommendedDays = 5,
            difficulty = "easy",
            coverUrl = null
        ),
        
        // === КУРС 5: Работа с тревожностью ===
        CourseSeed(
            id = "course_anxiety_management",
            title = "Работа с тревожностью",
            description = "Комплексная программа для снижения тревоги и обретения внутреннего спокойствия. Гибкий график прохождения.",
            goal = PracticeGoal.ANXIETY,
            level = PracticeLevel.INTERMEDIATE,
            rhythm = CourseRhythm.FLEXIBLE,
            tags = listOf("тревога", "спокойствие", "баланс"),
            totalDurationSec = 7200, // ~2 часа total
            modulesCount = 8,
            recommendedDays = null,
            difficulty = "medium",
            coverUrl = null
        )
    )
    
    fun getCourseItems(): Map<String, List<CourseItemSeed>> = mapOf(
        // === Items для "Сон за 7 дней" ===
        "course_sleep_7days" to listOf(
            CourseItemSeed(
                id = "course_sleep_7days_day1",
                courseId = "course_sleep_7days",
                order = 1,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_breathing_478",
                title = "День 1: Знакомство с дыханием 4-7-8",
                description = "Базовая техника для засыпания",
                mandatory = true,
                minDurationSec = 300
            ),
            CourseItemSeed(
                id = "course_sleep_7days_day2",
                courseId = "course_sleep_7days",
                order = 2,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_breathing_478",
                title = "День 2: Углубление практики",
                description = "Второй день с дыханием 4-7-8",
                mandatory = true,
                minDurationSec = 300
            ),
            CourseItemSeed(
                id = "course_sleep_7days_day3",
                courseId = "course_sleep_7days",
                order = 3,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_meditation_body_scan",
                title = "День 3: Сканирование тела",
                description = "Расслабление каждой части тела",
                mandatory = true,
                minDurationSec = 900
            ),
            CourseItemSeed(
                id = "course_sleep_7days_day4",
                courseId = "course_sleep_7days",
                order = 4,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_soundscape_ocean",
                title = "День 4: Звуки океана",
                description = "Погружение в звуки природы",
                mandatory = true,
                minDurationSec = 1800
            ),
            CourseItemSeed(
                id = "course_sleep_7days_day5",
                courseId = "course_sleep_7days",
                order = 5,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_meditation_sleep",
                title = "День 5: Медитация перед сном",
                description = "Специализированная практика для сна",
                mandatory = true,
                minDurationSec = 1200
            ),
            CourseItemSeed(
                id = "course_sleep_7days_day6",
                courseId = "course_sleep_7days",
                order = 6,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_meditation_body_scan",
                title = "День 6: Комбинация техник",
                description = "Сканирование и дыхание",
                mandatory = true,
                minDurationSec = 900
            ),
            CourseItemSeed(
                id = "course_sleep_7days_day7",
                courseId = "course_sleep_7days",
                order = 7,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_meditation_sleep",
                title = "День 7: Финальная практика",
                description = "Закрепление всех техник",
                mandatory = true,
                minDurationSec = 1200
            )
        ),
        
        // === Items для "Снижение стресса" ===
        "course_stress_relief" to listOf(
            CourseItemSeed(
                id = "course_stress_day1",
                courseId = "course_stress_relief",
                order = 1,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_breathing_box",
                title = "День 1: Квадратное дыхание",
                description = "Основа стрессоустойчивости",
                mandatory = true,
                minDurationSec = 420
            ),
            CourseItemSeed(
                id = "course_stress_day2",
                courseId = "course_stress_relief",
                order = 2,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_mixed_stress_relief",
                title = "День 2: Экспресс-техника",
                description = "Быстрое снятие стресса",
                mandatory = true,
                minDurationSec = 300
            ),
            CourseItemSeed(
                id = "course_stress_day3",
                courseId = "course_stress_relief",
                order = 3,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_meditation_mindfulness",
                title = "День 3: Осознанность",
                description = "Наблюдение за стрессом без реакции",
                mandatory = true,
                minDurationSec = 600
            ),
            CourseItemSeed(
                id = "course_stress_day4",
                courseId = "course_stress_relief",
                order = 4,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_soundscape_forest",
                title = "День 4: Лесные звуки",
                description = "Природная терапия",
                mandatory = false,
                minDurationSec = 1800
            ),
            CourseItemSeed(
                id = "course_stress_day5",  
                courseId = "course_stress_relief",
                order = 5,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_breathing_box",
                title = "День 5: Углубление дыхания",
                description = "Продвинутая работа с дыханием",
                mandatory = true,
                minDurationSec = 420
            )
            // Остальные дни 6-10 аналогично...
        ),
        
        // === Items для "Фокус и концентрация" ===
        "course_focus_concentration" to listOf(
            CourseItemSeed(
                id = "course_focus_session1",
                courseId = "course_focus_concentration",
                order = 1,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_meditation_focus",
                title = "Сессия 1: Базовый фокус",
                description = "Тренировка концентрации",
                mandatory = true,
                minDurationSec = 900
            ),
            CourseItemSeed(
                id = "course_focus_session2",
                courseId = "course_focus_concentration",
                order = 2,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_meditation_focus",
                title = "Сессия 2: Углубление",
                description = "Более длительная концентрация",
                mandatory = true,
                minDurationSec = 900
            )
            // Остальные сессии 3-6 аналогично...
        ),
        
        // === Items для "Энергия на весь день" ===
        "course_energy_boost" to listOf(
            CourseItemSeed(
                id = "course_energy_day1",
                courseId = "course_energy_boost",
                order = 1,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_breathing_energizing",
                title = "День 1: Бодрящее дыхание",
                description = "Пробуждение энергии",
                mandatory = true,
                minDurationSec = 180
            ),
            CourseItemSeed(
                id = "course_energy_day2",
                courseId = "course_energy_boost",
                order = 2,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_mixed_morning",
                title = "День 2: Утренний ритуал",
                description = "Комплексная практика",
                mandatory = true,
                minDurationSec = 600
            )
            // Остальные дни 3-5 аналогично...
        ),
        
        // === Items для "Работа с тревожностью" ===
        "course_anxiety_management" to listOf(
            CourseItemSeed(
                id = "course_anxiety_module1",
                courseId = "course_anxiety_management",
                order = 1,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_mixed_anxiety",
                title = "Модуль 1: Основы",
                description = "Знакомство с тревогой",
                mandatory = true,
                minDurationSec = 720
            ),
            CourseItemSeed(
                id = "course_anxiety_module2",
                courseId = "course_anxiety_management",
                order = 2,
                type = CourseItemType.PRACTICE,
                practiceId = "practice_breathing_box",
                title = "Модуль 2: Дыхание",
                description = "Дыхательные техники для тревоги",
                mandatory = true,
                minDurationSec = 420
            )
            // Остальные модули 3-8 аналогично...
        )
    )
}

