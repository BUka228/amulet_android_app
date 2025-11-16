package com.example.amulet.data.courses.seed

import com.example.amulet.shared.domain.courses.model.CourseItemType

/**
 * Провайдер предустановленных профессиональных курсов для сидирования.
 * Содержит структурированные программы обучения для разных уровней и целей.
 */
class CourseSeedProvider {
    
    fun provideAll(): List<CourseSeed> = buildList {
        
        // === КУРС ДЛЯ НАЧИНАЮЩИХ ===
        
        add(CourseSeed(
            id = "course_breathing_fundamentals",
            title = "Основы дыхательных практик",
            description = "Фундаментальный курс для освоения базовых дыхательных техник. Научитесь управлять стрессом, улучшать концентрацию и повышать уровень энергии через правильное дыхание.",
            tags = listOf("breathing", "beginner", "fundamentals", "stress"),
            totalDurationSec = 3600,
            difficulty = "beginner",
            coverUrl = "covers/breathing_fundamentals.jpg",
            category = "Дыхание",
            items = listOf(
                CourseItemSeed(
                    id = "item_breathing_intro",
                    courseId = "course_breathing_fundamentals",
                    order = 1,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Введение в дыхательные практики",
                    description = "Теория: как дыхание влияет на нервную систему, основные принципы и противопоказания.",
                    mandatory = true,
                    minDurationSec = 300,
                    contentUrl = "content/breathing_intro.html"
                ),
                CourseItemSeed(
                    id = "item_breath_box_4_4_4_4",
                    courseId = "course_breathing_fundamentals",
                    order = 2,
                    type = CourseItemType.PRACTICE,
                    practiceId = "breath_box_4_4_4_4",
                    title = "Квадратное дыхание 4-4-4-4",
                    description = "Практика: освоение классической техники для снижения стресса.",
                    mandatory = true,
                    minDurationSec = 300
                ),
                CourseItemSeed(
                    id = "item_breath_4_7_8_relax",
                    courseId = "course_breathing_fundamentals",
                    order = 3,
                    type = CourseItemType.PRACTICE,
                    practiceId = "breath_4_7_8_relax",
                    title = "Дыхание 4-7-8 для расслабления",
                    description = "Практика: техника для глубокого расслабления и улучшения сна.",
                    mandatory = true,
                    minDurationSec = 420
                ),
                CourseItemSeed(
                    id = "item_breath_belly_diaphragm",
                    courseId = "course_breathing_fundamentals",
                    order = 4,
                    type = CourseItemType.PRACTICE,
                    practiceId = "breath_belly_diaphragm",
                    title = "Диафрагмальное дыхание",
                    description = "Практика: активация парасимпатической системы через брюшное дыхание.",
                    mandatory = true,
                    minDurationSec = 480
                ),
                CourseItemSeed(
                    id = "item_breathing_integration",
                    courseId = "course_breathing_fundamentals",
                    order = 5,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Интеграция практик в повседневную жизнь",
                    description = "Теория: как создать регулярную практику и применять техники в стрессовых ситуациях.",
                    mandatory = true,
                    minDurationSec = 300,
                    contentUrl = "content/breathing_integration.html"
                )
            )
        ))
        
        // === КУРС ПО МЕДИТАЦИИ ===
        
        add(CourseSeed(
            id = "course_mindfulness_journey",
            title = "Путешествие в осознанность",
            description = "Комплексный курс по развитию осознанности (mindfulness) через медитацию. Изучите техники для присутствия в моменте, снижения тревожности и улучшения качества жизни.",
            tags = listOf("meditation", "mindfulness", "stress", "presence"),
            totalDurationSec = 5400,
            difficulty = "intermediate",
            coverUrl = "covers/mindfulness_journey.jpg",
            category = "Медитация",
            items = listOf(
                CourseItemSeed(
                    id = "item_mindfulness_intro",
                    courseId = "course_mindfulness_journey",
                    order = 1,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Что такое осознанность?",
                    description = "Теория: определение, научные основы, преимущества для психического здоровья.",
                    mandatory = true,
                    minDurationSec = 420,
                    contentUrl = "content/mindfulness_intro.html"
                ),
                CourseItemSeed(
                    id = "item_meditation_breath_awareness",
                    courseId = "course_mindfulness_journey",
                    order = 2,
                    type = CourseItemType.PRACTICE,
                    practiceId = "meditation_mindfulness_breath",
                    title = "Осознанность дыхания",
                    description = "Практика: фундаментальная техника для развития внимательности.",
                    mandatory = true,
                    minDurationSec = 900
                ),
                CourseItemSeed(
                    id = "item_meditation_body_scan",
                    courseId = "course_mindfulness_journey",
                    order = 3,
                    type = CourseItemType.PRACTICE,
                    practiceId = "meditation_body_scan",
                    title = "Сканирование тела",
                    description = "Практика: глубокая релаксация через поэтежное осознание тела.",
                    mandatory = true,
                    minDurationSec = 1200
                ),
                CourseItemSeed(
                    id = "item_meditation_walking_mindful",
                    courseId = "course_mindfulness_journey",
                    order = 4,
                    type = CourseItemType.PRACTICE,
                    practiceId = "meditation_walking_mindful",
                    title = "Осознанная ходьба",
                    description = "Практика: медитация в движении для интеграции в повседневность.",
                    mandatory = false,
                    minDurationSec = 600
                ),
                CourseItemSeed(
                    id = "item_meditation_loving_kindness",
                    courseId = "course_mindfulness_journey",
                    order = 5,
                    type = CourseItemType.PRACTICE,
                    practiceId = "meditation_loving_kindness",
                    title = "Медитация любящей доброты",
                    description = "Практика: развитие сострадания к себе и другим.",
                    mandatory = false,
                    minDurationSec = 1080
                ),
                CourseItemSeed(
                    id = "item_mindfulness_daily",
                    courseId = "course_mindfulness_journey",
                    order = 6,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Осознанность в повседневной жизни",
                    description = "Теория: микро-практики для работы, отношений и стрессовых ситуаций.",
                    mandatory = true,
                    minDurationSec = 420,
                    contentUrl = "content/mindfulness_daily.html"
                )
            )
        ))
        
        // === КУРС ПО СТРЕССУ И ТРЕВОГЕ ===
        
        add(CourseSeed(
            id = "course_stress_anxiety_relief",
            title = "Управление стрессом и тревогой",
            description = "Практический курс для борьбы со стрессом и тревожными состояниями. Комплекс техник дыхания, медитации и релаксации для восстановления эмоционального баланса.",
            tags = listOf("stress", "anxiety", "relief", "emotional_balance"),
            totalDurationSec = 4800,
            difficulty = "intermediate",
            coverUrl = "covers/stress_relief.jpg",
            category = "Психическое здоровье",
            items = listOf(
                CourseItemSeed(
                    id = "item_stress_physiology",
                    courseId = "course_stress_anxiety_relief",
                    order = 1,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Физиология стресса и тревоги",
                    description = "Теория: как работает стрессовая система, роль нервной системы и гормонов.",
                    mandatory = true,
                    minDurationSec = 360,
                    contentUrl = "content/stress_physiology.html"
                ),
                CourseItemSeed(
                    id = "item_breath_box_3_3_3_3_focus",
                    courseId = "course_stress_anxiety_relief",
                    order = 2,
                    type = CourseItemType.PRACTICE,
                    practiceId = "breath_box_3_3_3_3_focus",
                    title = "Быстрая помощь: фокус-квадрат",
                    description = "Практика: экстренная техника для снятия острого стресса.",
                    mandatory = true,
                    minDurationSec = 180
                ),
                CourseItemSeed(
                    id = "item_breath_1_2_relax",
                    courseId = "course_stress_anxiety_relief",
                    order = 3,
                    type = CourseItemType.PRACTICE,
                    practiceId = "breath_1_2_relax",
                    title = "Расслабляющее дыхание 1:2",
                    description = "Практика: активация парасимпатической системы для глубокого расслабления.",
                    mandatory = true,
                    minDurationSec = 480
                ),
                CourseItemSeed(
                    id = "item_meditation_progressive_relaxation",
                    courseId = "course_stress_anxiety_relief",
                    order = 4,
                    type = CourseItemType.PRACTICE,
                    practiceId = "meditation_progressive_relaxation",
                    title = "Прогрессивная мышечная релаксация",
                    description = "Практика: снятие физического напряжения через поочередное расслабление мышц.",
                    mandatory = true,
                    minDurationSec = 900
                ),
                CourseItemSeed(
                    id = "item_sound_rain_nature",
                    courseId = "course_stress_anxiety_relief",
                    order = 5,
                    type = CourseItemType.PRACTICE,
                    practiceId = "sound_rain_nature",
                    title = "Звуковая терапия: дождь",
                    description = "Практика: использование звуков природы для успокоения нервной системы.",
                    mandatory = false,
                    minDurationSec = 600
                ),
                CourseItemSeed(
                    id = "item_stress_prevention",
                    courseId = "course_stress_anxiety_relief",
                    order = 6,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Профилактика стресса",
                    description = "Теория: создание устойчивости к стрессу, распознавание триггеров, превентивные практики.",
                    mandatory = true,
                    minDurationSec = 420,
                    contentUrl = "content/stress_prevention.html"
                )
            )
        ))
        
        // === КУРС ПО УЛУЧШЕНИЮ СНА ===
        
        add(CourseSeed(
            id = "course_deep_sleep_restoration",
            title = "Глубокий сон и восстановление",
            description = "Комплексная программа для улучшения качества сна. Техники расслабления, дыхательные практики и звуковые ландшафты для естественного засыпания и глубокого восстановления.",
            tags = listOf("sleep", "restoration", "insomnia", "recovery"),
            totalDurationSec = 4200,
            difficulty = "beginner",
            coverUrl = "covers/deep_sleep.jpg",
            category = "Сон и восстановление",
            items = listOf(
                CourseItemSeed(
                    id = "item_sleep_science",
                    courseId = "course_deep_sleep_restoration",
                    order = 1,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Наука о качественном сне",
                    description = "Теория: циклы сна, роль нервной системы, факторы влияющие на качество отдыха.",
                    mandatory = true,
                    minDurationSec = 300,
                    contentUrl = "content/sleep_science.html"
                ),
                CourseItemSeed(
                    id = "item_breath_4_7_8_sleep",
                    courseId = "course_deep_sleep_restoration",
                    order = 2,
                    type = CourseItemType.PRACTICE,
                    practiceId = "breath_4_7_8_relax",
                    title = "Дыхание 4-7-8 для засыпания",
                    description = "Практика: классическая техника для быстрого засыпания.",
                    mandatory = true,
                    minDurationSec = 420
                ),
                CourseItemSeed(
                    id = "item_meditation_body_scan_sleep",
                    courseId = "course_deep_sleep_restoration",
                    order = 3,
                    type = CourseItemType.PRACTICE,
                    practiceId = "meditation_body_scan",
                    title = "Сканирование тела перед сном",
                    description = "Практика: глубокое расслабление для подготовки ко сну.",
                    mandatory = true,
                    minDurationSec = 900
                ),
                CourseItemSeed(
                    id = "item_meditation_yoga_nidra",
                    courseId = "course_deep_sleep_restoration",
                    order = 4,
                    type = CourseItemType.PRACTICE,
                    practiceId = "meditation_yoga_nidra",
                    title = "Йога-нидра для глубокого отдыха",
                    description = "Практика: йогический сон для максимального восстановления.",
                    mandatory = false,
                    minDurationSec = 1200
                ),
                CourseItemSeed(
                    id = "item_sound_white_noise",
                    courseId = "course_deep_sleep_restoration",
                    order = 5,
                    type = CourseItemType.PRACTICE,
                    practiceId = "sound_white_noise",
                    title = "Белый шум для засыпания",
                    description = "Практика: создание нейтрального звукового фона для улучшения сна.",
                    mandatory = false,
                    minDurationSec = 600
                ),
                CourseItemSeed(
                    id = "item_sleep_hygiene",
                    courseId = "course_deep_sleep_restoration",
                    order = 6,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Гигиена сна",
                    description = "Теория: создание оптимальных условий для качественного отдыха.",
                    mandatory = true,
                    minDurationSec = 360,
                    contentUrl = "content/sleep_hygiene.html"
                )
            )
        ))
        
        // === ПРОДВИНУТЫЙ КУРС ПО ДЫХАНИЮ ===
        
        add(CourseSeed(
            id = "course_advanced_breathing_mastery",
            title = "Мастерство продвинутого дыхания",
            description = "Продвинутый курс для опытных практиков. Изучите мощные техники Вима Хофа, пранаямы и когерентного дыхания для трансформации сознания и здоровья.",
            tags = listOf("breathing", "advanced", "wim_hof", "pranayama"),
            totalDurationSec = 6000,
            difficulty = "advanced",
            coverUrl = "covers/advanced_breathing.jpg",
            category = "Продвинутые практики",
            items = listOf(
                CourseItemSeed(
                    id = "item_advanced_breathing_intro",
                    courseId = "course_advanced_breathing_mastery",
                    order = 1,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Физиология продвинутого дыхания",
                    description = "Теория: влияние на pH крови, вегетативную нервную систему и сознание.",
                    mandatory = true,
                    minDurationSec = 480,
                    contentUrl = "content/advanced_breathing_intro.html"
                ),
                CourseItemSeed(
                    id = "item_breath_coherent",
                    courseId = "course_advanced_breathing_mastery",
                    order = 2,
                    type = CourseItemType.PRACTICE,
                    practiceId = "breath_coherent",
                    title = "Когерентное дыхание 5-5",
                    description = "Практика: оптимизация HRV и баланс нервной системы.",
                    mandatory = true,
                    minDurationSec = 600
                ),
                CourseItemSeed(
                    id = "item_breath_wim_hof_basic",
                    courseId = "course_advanced_breathing_mastery",
                    order = 3,
                    type = CourseItemType.PRACTICE,
                    practiceId = "breath_wim_hof_basic",
                    title = "Дыхание Вима Хофа",
                    description = "Практика: методика для повышения энергии и иммунитета.",
                    mandatory = true,
                    minDurationSec = 600
                ),
                CourseItemSeed(
                    id = "item_breath_alternate_nostril",
                    courseId = "course_advanced_breathing_mastery",
                    order = 4,
                    type = CourseItemType.PRACTICE,
                    practiceId = "breath_alternate_nostril",
                    title = "Попеременное дыхание ноздрями",
                    description = "Практика: нади шодхана для баланса полушарий мозга.",
                    mandatory = true,
                    minDurationSec = 720
                ),
                CourseItemSeed(
                    id = "item_breath_ujjayi",
                    courseId = "course_advanced_breathing_mastery",
                    order = 5,
                    type = CourseItemType.PRACTICE,
                    practiceId = "breath_ujjayi",
                    title = "Дыхание Уджайи",
                    description = "Практика: океаническое дыхание для глубокой медитации.",
                    mandatory = false,
                    minDurationSec = 600
                ),
                CourseItemSeed(
                    id = "item_advanced_integration",
                    courseId = "course_advanced_breathing_mastery",
                    order = 6,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Интеграция и безопасность",
                    description = "Теория: противопоказания, построение личной практики, комбинации техник.",
                    mandatory = true,
                    minDurationSec = 420,
                    contentUrl = "content/advanced_integration.html"
                )
            )
        ))
        
        // === КУРС ПО ФОКУСУ И ПРОДУКТИВНОСТИ ===
        
        add(CourseSeed(
            id = "course_focus_productivity_flow",
            title = "Фокус, продуктивность и поток",
            description = "Практический курс для улучшения концентрации и входа в состояние потока. Техники дыхания и медитации для повышения когнитивных способностей и продуктивности.",
            tags = listOf("focus", "productivity", "flow", "performance"),
            totalDurationSec = 3600,
            difficulty = "intermediate",
            coverUrl = "covers/focus_flow.jpg",
            category = "Продуктивность",
            items = listOf(
                CourseItemSeed(
                    id = "item_focus_neuroscience",
                    courseId = "course_focus_productivity_flow",
                    order = 1,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Нейробиология фокуса и потока",
                    description = "Теория: как работает внимание, роль дофамина, нейронные корреляты состояния потока.",
                    mandatory = true,
                    minDurationSec = 300,
                    contentUrl = "content/focus_neuroscience.html"
                ),
                CourseItemSeed(
                    id = "item_breath_box_3_3_3_3_focus",
                    courseId = "course_focus_productivity_flow",
                    order = 2,
                    type = CourseItemType.PRACTICE,
                    practiceId = "breath_box_3_3_3_3_focus",
                    title = "Фокус-квадрат 3-3-3-3",
                    description = "Практика: быстрая техника для ментальной ясности.",
                    mandatory = true,
                    minDurationSec = 180
                ),
                CourseItemSeed(
                    id = "item_meditation_mindfulness_breath_focus",
                    courseId = "course_focus_productivity_flow",
                    order = 3,
                    type = CourseItemType.PRACTICE,
                    practiceId = "meditation_mindfulness_breath",
                    title = "Осознанность для концентрации",
                    description = "Практика: тренировка устойчивого внимания.",
                    mandatory = true,
                    minDurationSec = 600
                ),
                CourseItemSeed(
                    id = "item_breath_coherent_focus",
                    courseId = "course_focus_productivity_flow",
                    order = 4,
                    type = CourseItemType.PRACTICE,
                    practiceId = "breath_coherent",
                    title = "Когерентное дыхание для продуктивности",
                    description = "Практика: оптимизация мозговой активности через дыхание.",
                    mandatory = true,
                    minDurationSec = 450
                ),
                CourseItemSeed(
                    id = "item_sound_binaural_beats",
                    courseId = "course_focus_productivity_flow",
                    order = 5,
                    type = CourseItemType.PRACTICE,
                    practiceId = "sound_binaural_beats",
                    title = "Бинауральные ритмы для обучения",
                    description = "Практика: использование тета-волн для улучшения когнитивных функций.",
                    mandatory = false,
                    minDurationSec = 600
                ),
                CourseItemSeed(
                    id = "item_focus_productivity_integration",
                    courseId = "course_focus_productivity_flow",
                    order = 6,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Интеграция в рабочую рутину",
                    description = "Теория: микро-перерывы, управление энергией, создание продуктивной среды.",
                    mandatory = true,
                    minDurationSec = 300,
                    contentUrl = "content/focus_integration.html"
                )
            )
        ))
        
        // === КУРС ПО ЭМОЦИОНАЛЬНОМУ БАЛАНСУ ===
        
        add(CourseSeed(
            id = "course_emotional_balance_resilience",
            title = "Эмоциональный баланс и устойчивость",
            description = "Курс для развития эмоционального интеллекта и устойчивости. Научитесь распознавать, принимать и трансформировать эмоции через осознанные практики.",
            tags = listOf("emotions", "balance", "resilience", "intelligence"),
            totalDurationSec = 4800,
            difficulty = "intermediate",
            coverUrl = "covers/emotional_balance.jpg",
            category = "Эмоциональное здоровье",
            items = listOf(
                CourseItemSeed(
                    id = "item_emotions_intelligence_intro",
                    courseId = "course_emotional_balance_resilience",
                    order = 1,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Эмоциональный интеллект и устойчивость",
                    description = "Теория: природа эмоций, нейробиология, развитие устойчивости.",
                    mandatory = true,
                    minDurationSec = 360,
                    contentUrl = "content/emotions_intelligence_intro.html"
                ),
                CourseItemSeed(
                    id = "item_meditation_body_scan_emotions",
                    courseId = "course_emotional_balance_resilience",
                    order = 2,
                    type = CourseItemType.PRACTICE,
                    practiceId = "meditation_body_scan",
                    title = "Сканирование тела для эмоций",
                    description = "Практика: распознавание эмоций через телесные ощущения.",
                    mandatory = true,
                    minDurationSec = 900
                ),
                CourseItemSeed(
                    id = "item_meditation_loving_kindness_emotions",
                    courseId = "course_emotional_balance_resilience",
                    order = 3,
                    type = CourseItemType.PRACTICE,
                    practiceId = "meditation_loving_kindness",
                    title = "Любящая доброта для себя",
                    description = "Практика: развитие самосострадания и принятия.",
                    mandatory = true,
                    minDurationSec = 900
                ),
                CourseItemSeed(
                    id = "item_breath_box_4_4_4_4_emotions",
                    courseId = "course_emotional_balance_resilience",
                    order = 4,
                    type = CourseItemType.PRACTICE,
                    practiceId = "breath_box_4_4_4_4",
                    title = "Квадратное дыхание для баланса",
                    description = "Практика: стабилизация эмоционального состояния.",
                    mandatory = true,
                    minDurationSec = 300
                ),
                CourseItemSeed(
                    id = "item_meditation_chakra_balance",
                    courseId = "course_emotional_balance_resilience",
                    order = 5,
                    type = CourseItemType.PRACTICE,
                    practiceId = "meditation_chakra_balance",
                    title = "Балансировка эмоциональных центров",
                    description = "Практика: гармонизация энергетических центров связанных с эмоциями.",
                    mandatory = false,
                    minDurationSec = 900
                ),
                CourseItemSeed(
                    id = "item_emotions_daily_practice",
                    courseId = "course_emotional_balance_resilience",
                    order = 6,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Ежедневная эмоциональная гигиена",
                    description = "Теория: создание ритуалов для эмоционального благополучия.",
                    mandatory = true,
                    minDurationSec = 300,
                    contentUrl = "content/emotions_daily_practice.html"
                )
            )
        ))
        
        // === КУРС ПО ЗВУКОВОЙ ТЕРАПИИ ===
        
        add(CourseSeed(
            id = "course_sound_healing_therapy",
            title = "Звуковая терапия и исцеление",
            description = "Погружение в мир звуковых ландшафтов для исцеления и трансформации. Изучите, как разные звуки влияют на мозг и сознание, научитесь создавать целительные звуковые среды.",
            tags = listOf("sound", "healing", "therapy", "vibration"),
            totalDurationSec = 3600,
            difficulty = "beginner",
            coverUrl = "covers/sound_healing.jpg",
            category = "Звуковые практики",
            items = listOf(
                CourseItemSeed(
                    id = "item_sound_therapy_intro",
                    courseId = "course_sound_healing_therapy",
                    order = 1,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Наука о звуке и исцелении",
                    description = "Теория: влияние звука на мозг, резонансные частоты, история звуковой терапии.",
                    mandatory = true,
                    minDurationSec = 300,
                    contentUrl = "content/sound_therapy_intro.html"
                ),
                CourseItemSeed(
                    id = "item_sound_nature_elements",
                    courseId = "course_sound_healing_therapy",
                    order = 2,
                    type = CourseItemType.PRACTICE,
                    practiceId = "sound_rain_nature",
                    title = "Стихии природы: дождь и лес",
                    description = "Практика: погружение в звуки природы для восстановления.",
                    mandatory = true,
                    minDurationSec = 900
                ),
                CourseItemSeed(
                    id = "item_sound_water_elements",
                    courseId = "course_sound_healing_therapy",
                    order = 3,
                    type = CourseItemType.PRACTICE,
                    practiceId = "sound_ocean_waves",
                    title = "Водные стихии: океан и волны",
                    description = "Практика: ритм волн для медитативных состояний.",
                    mandatory = true,
                    minDurationSec = 900
                ),
                CourseItemSeed(
                    id = "item_sound_tibetan_bowls_healing",
                    courseId = "course_sound_healing_therapy",
                    order = 4,
                    type = CourseItemType.PRACTICE,
                    practiceId = "sound_tibetan_bowls",
                    title = "Тибетские чаши и вибрации",
                    description = "Практика: целительные вибрации для гармонизации энергии.",
                    mandatory = true,
                    minDurationSec = 600
                ),
                CourseItemSeed(
                    id = "item_sound_binaural_brain",
                    courseId = "course_sound_healing_therapy",
                    order = 5,
                    type = CourseItemType.PRACTICE,
                    practiceId = "sound_binaural_beats",
                    title = "Бинауральные ритмы для мозга",
                    description = "Практика: научный подход к изменению сознания.",
                    mandatory = false,
                    minDurationSec = 600
                ),
                CourseItemSeed(
                    id = "item_sound_personal_therapy",
                    courseId = "course_sound_healing_therapy",
                    order = 6,
                    type = CourseItemType.THEORY,
                    practiceId = null,
                    title = "Создание личной звуковой терапии",
                    description = "Теория: подбор звуков, создание плейлистов, интеграция в жизнь.",
                    mandatory = true,
                    minDurationSec = 300,
                    contentUrl = "content/sound_personal_therapy.html"
                )
            )
        ))
    }
    
    fun getByCategory(category: String): List<CourseSeed> {
        return provideAll().filter { it.category == category }
    }
    
    fun getByDifficulty(difficulty: String): List<CourseSeed> {
        return provideAll().filter { it.difficulty == difficulty }
    }
    
    fun getByTag(tag: String): List<CourseSeed> {
        return provideAll().filter { it.tags.contains(tag) }
    }
}
