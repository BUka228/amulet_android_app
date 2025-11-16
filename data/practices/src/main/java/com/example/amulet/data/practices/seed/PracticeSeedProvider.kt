package com.example.amulet.data.practices.seed

import com.example.amulet.shared.domain.practices.model.PracticeType

/**
 * Провайдер предустановленных практик для сидирования.
 * Содержит профессиональные дыхательные, медитативные и звуковые практики.
 */
class PracticeSeedProvider {
    
    fun provideAll(): List<PracticeSeed> = buildList {
        
        // === ДЫХАТЕЛЬНЫЕ ПРАКТИКИ ===
        
        add(PracticeSeed(
            id = "breath_box_4_4_4_4",
            type = PracticeType.BREATH,
            title = "Квадратное дыхание 4-4-4-4",
            description = "Классическая техника для снижения стресса и улучшения концентрации. Вдох, задержка, выдох и задержка по 4 секунды.",
            durationSec = 300,
            patternId = "practice_box_breath",
            audioUrl = null,
            tags = listOf("breathing", "stress", "focus", "beginner"),
            difficulty = "beginner",
            category = "Дыхание"
        ))
        
        add(PracticeSeed(
            id = "breath_4_7_8_relax",
            type = PracticeType.BREATH,
            title = "Дыхание 4-7-8 для расслабления",
            description = "Техника доктора Эндрю Вейла для глубокого расслабления и быстрого засыпания. Вдох 4с, задержка 7с, выдох 8с.",
            durationSec = 420,
            patternId = "practice_breathing_478",
            audioUrl = null,
            tags = listOf("breathing", "relax", "sleep", "anxiety"),
            difficulty = "beginner",
            category = "Дыхание"
        ))
        
        add(PracticeSeed(
            id = "breath_wim_hof_basic",
            type = PracticeType.BREATH,
            title = "Дыхание Вима Хофа (базовое)",
            description = "Циклическое гипервентиляционное дыхание для повышения энергии и иммунитета. 30 циклов с задержками.",
            durationSec = 600,
            patternId = "practice_energy_breath",
            audioUrl = null,
            tags = listOf("breathing", "energy", "immune", "advanced"),
            difficulty = "advanced",
            category = "Дыхание"
        ))
        
        add(PracticeSeed(
            id = "breath_coherent",
            type = PracticeType.BREATH,
            title = "Когерентное дыхание 5-5",
            description = "Сбалансированное дыхание для гармонизации нервной системы. Идеально для HRV-тренинга.",
            durationSec = 600,
            patternId = "practice_coherent_breath",
            audioUrl = null,
            tags = listOf("breathing", "hrv", "balance", "intermediate"),
            difficulty = "intermediate",
            category = "Дыхание"
        ))
        
        add(PracticeSeed(
            id = "breath_belly_diaphragm",
            type = PracticeType.BREATH,
            title = "Диафрагмальное дыхание",
            description = "Глубокое брюшное дыхание для активации парасимпатической системы и снижения кортизола.",
            durationSec = 480,
            patternId = "practice_belly_breath",
            audioUrl = null,
            tags = listOf("breathing", "belly", "cortisol", "relax"),
            difficulty = "beginner",
            category = "Дыхание"
        ))
        
        add(PracticeSeed(
            id = "breath_alternate_nostril",
            type = PracticeType.BREATH,
            title = "Попеременное дыхание ноздрями (Нади Шодхана)",
            description = "Йогическая техника для баланса полушарий мозга и очищения энергетических каналов.",
            durationSec = 720,
            patternId = "practice_alternate_breath",
            audioUrl = null,
            tags = listOf("breathing", "yoga", "balance", "intermediate"),
            difficulty = "intermediate",
            category = "Дыхание"
        ))
        
        add(PracticeSeed(
            id = "breath_ujjayi",
            type = PracticeType.BREATH,
            title = "Дыхание Уджайи (Океаническое дыхание)",
            description = "Йогическая техника с легким сужением гортани для создания успокаивающего звука океана.",
            durationSec = 600,
            patternId = "practice_ocean_breath",
            audioUrl = null,
            tags = listOf("breathing", "yoga", "ocean", "meditation"),
            difficulty = "intermediate",
            category = "Дыхание"
        ))
        
        add(PracticeSeed(
            id = "breath_box_3_3_3_3_focus",
            type = PracticeType.BREATH,
            title = "Фокус-квадрат 3-3-3-3",
            description = "Быстрое квадратное дыхание для ментальной ясности и концентрации перед важными задачами.",
            durationSec = 180,
            patternId = "practice_focus_square",
            audioUrl = null,
            tags = listOf("breathing", "focus", "work", "quick"),
            difficulty = "beginner",
            category = "Дыхание"
        ))
        
        add(PracticeSeed(
            id = "breath_1_2_relax",
            type = PracticeType.BREATH,
            title = "Расслабляющее дыхание 1:2",
            description = "Выдох в два раза длиннее вдоха для активации парасимпатической нервной системы.",
            durationSec = 480,
            patternId = "practice_relax_ratio",
            audioUrl = null,
            tags = listOf("breathing", "relax", "parasympathetic", "stress"),
            difficulty = "beginner",
            category = "Дыхание"
        ))
        
        // === МЕДИТАЦИИ ===
        
        add(PracticeSeed(
            id = "meditation_body_scan",
            type = PracticeType.MEDITATION,
            title = "Сканирование тела (Body Scan)",
            description = "Поэтежная релаксация всех частей тела для глубокого осознания и снятия напряжения.",
            durationSec = 1200,
            patternId = "meditation_body_scan",
            audioUrl = "audio/body_scan.mp3",
            tags = listOf("meditation", "body", "relax", "sleep"),
            difficulty = "beginner",
            category = "Медитация"
        ))
        
        add(PracticeSeed(
            id = "meditation_mindfulness_breath",
            type = PracticeType.MEDITATION,
            title = "Осознанность дыхания",
            description = "Классическая медитация на фокусе дыхания для развития внимательности и присутствия.",
            durationSec = 900,
            patternId = "meditation_breath_awareness",
            audioUrl = "audio/mindfulness_breath.mp3",
            tags = listOf("meditation", "mindfulness", "breath", "presence"),
            difficulty = "beginner",
            category = "Медитация"
        ))
        
        add(PracticeSeed(
            id = "meditation_loving_kindness",
            type = PracticeType.MEDITATION,
            title = "Медитация любящей доброты (Metta)",
            description = "Практика развития сострадания к себе и другим через последовательное направление благих пожеланий.",
            durationSec = 1080,
            patternId = "meditation_metta",
            audioUrl = "audio/loving_kindness.mp3",
            tags = listOf("meditation", "compassion", "love", "relationships"),
            difficulty = "intermediate",
            category = "Медитация"
        ))
        
        add(PracticeSeed(
            id = "meditation_zazen_sitting",
            type = PracticeType.MEDITATION,
            title = "Дзадзэн (поза сидения)",
            description = "Дзен-медитация с фокусом на прямой осанке и наблюдении за мыслями без вовлечения.",
            durationSec = 1500,
            patternId = "meditation_zazen",
            audioUrl = "audio/zazen_sitting.mp3",
            tags = listOf("meditation", "zen", "posture", "observation"),
            difficulty = "advanced",
            category = "Медитация"
        ))
        
        add(PracticeSeed(
            id = "meditation_walking_mindful",
            type = PracticeType.MEDITATION,
            title = "Осознанная ходьба",
            description = "Медитация в движении с фокусом на ощущениях от шагов и контакте с землей.",
            durationSec = 600,
            patternId = "meditation_walking",
            audioUrl = "audio/walking_mindful.mp3",
            tags = listOf("meditation", "walking", "movement", "nature"),
            difficulty = "beginner",
            category = "Медитация"
        ))
        
        add(PracticeSeed(
            id = "meditation_chakra_balance",
            type = PracticeType.MEDITATION,
            title = "Балансировка чакр",
            description = "Визуализация и гармонизация семи основных энергетических центров тела.",
            durationSec = 1260,
            patternId = "meditation_chakra",
            audioUrl = "audio/chakra_balance.mp3",
            tags = listOf("meditation", "chakra", "energy", "visualization"),
            difficulty = "intermediate",
            category = "Медитация"
        ))
        
        add(PracticeSeed(
            id = "meditation_progressive_relaxation",
            type = PracticeType.MEDITATION,
            title = "Прогрессивная мышечная релаксация",
            description = "Поочередное напряжение и расслабление мышечных групп для глубокого снятия стресса.",
            durationSec = 900,
            patternId = "meditation_muscle_relax",
            audioUrl = "audio/progressive_relaxation.mp3",
            tags = listOf("meditation", "muscles", "relax", "stress"),
            difficulty = "beginner",
            category = "Медитация"
        ))
        
        add(PracticeSeed(
            id = "meditation_yoga_nidra",
            type = PracticeType.MEDITATION,
            title = "Йога-нидра (йогический сон)",
            description = "Глубокая релаксация на грани сна и бодрствования для восстановления психики.",
            durationSec = 1800,
            patternId = "meditation_yoga_nidra",
            audioUrl = "audio/yoga_nidra.mp3",
            tags = listOf("meditation", "sleep", "restoration", "deep"),
            difficulty = "intermediate",
            category = "Медитация"
        ))
        
        // === ЗВУКОВЫЕ ПРАКТИКИ ===
        
        add(PracticeSeed(
            id = "sound_rain_nature",
            type = PracticeType.SOUND,
            title = "Звук дождя",
            description = "Успокаивающий звук летнего дождя для релаксации и улучшения концентрации.",
            durationSec = 1800,
            patternId = "sound_rain",
            audioUrl = "audio/rain.mp3",
            tags = listOf("sound", "rain", "nature", "focus"),
            difficulty = "beginner",
            category = "Звуковые ландшафты"
        ))
        
        add(PracticeSeed(
            id = "sound_forest_birds",
            type = PracticeType.SOUND,
            title = "Лес с пением птиц",
            description = "Звуки утреннего леса для естественного пробуждения и гармонии с природой.",
            durationSec = 1200,
            patternId = "sound_forest",
            audioUrl = "audio/forest_birds.mp3",
            tags = listOf("sound", "forest", "birds", "morning"),
            difficulty = "beginner",
            category = "Звуковые ландшафты"
        ))
        
        add(PracticeSeed(
            id = "sound_ocean_waves",
            type = PracticeType.SOUND,
            title = "Океанские волны",
            description = "Ритмичный звук прибоя для глубокой релаксации и медитативного состояния.",
            durationSec = 1500,
            patternId = "sound_ocean",
            audioUrl = "audio/ocean_waves.mp3",
            tags = listOf("sound", "ocean", "waves", "meditation"),
            difficulty = "beginner",
            category = "Звуковые ландшафты"
        ))
        
        add(PracticeSeed(
            id = "sound_fireplace",
            type = PracticeType.SOUND,
            title = "Камин",
            description = "Теплый звук потрескивающего камина для уюта и безопасности.",
            durationSec = 900,
            patternId = "sound_fireplace",
            audioUrl = "audio/fireplace.mp3",
            tags = listOf("sound", "fire", "cozy", "evening"),
            difficulty = "beginner",
            category = "Звуковые ландшафты"
        ))
        
        add(PracticeSeed(
            id = "sound_space_ambient",
            type = PracticeType.SOUND,
            title = "Космический эмбиент",
            description = "Глубокие космические звуки для трансовых состояний и креативной визуализации.",
            durationSec = 1200,
            patternId = "sound_space",
            audioUrl = "audio/space_ambient.mp3",
            tags = listOf("sound", "space", "ambient", "creativity"),
            difficulty = "intermediate",
            category = "Звуковые ландшафты"
        ))
        
        add(PracticeSeed(
            id = "sound_tibetan_bowls",
            type = PracticeType.SOUND,
            title = "Тибетские чаши",
            description = "Целительные вибрации тибетских поющих чаш для гармонизации энергии.",
            durationSec = 600,
            patternId = "sound_tibetan",
            audioUrl = "audio/tibetan_bowls.mp3",
            tags = listOf("sound", "bowls", "healing", "vibration"),
            difficulty = "intermediate",
            category = "Звуковые ландшафты"
        ))
        
        add(PracticeSeed(
            id = "sound_binaural_beats",
            type = PracticeType.SOUND,
            title = "Бинауральные ритмы (тета-волны)",
            description = "Научно обоснованные звуковые частоты для глубокой медитации и обучения.",
            durationSec = 900,
            patternId = "sound_binaural",
            audioUrl = "audio/binaural_theta.mp3",
            tags = listOf("sound", "binaural", "theta", "learning"),
            difficulty = "intermediate",
            category = "Звуковые ландшафты"
        ))
        
        add(PracticeSeed(
            id = "sound_white_noise",
            type = PracticeType.SOUND,
            title = "Белый шум",
            description = "Нейтральный звуковой фон для улучшения концентрации и маскировки отвлекающих шумов.",
            durationSec = 1800,
            patternId = "sound_white_noise",
            audioUrl = "audio/white_noise.mp3",
            tags = listOf("sound", "white_noise", "focus", "sleep"),
            difficulty = "beginner",
            category = "Звуковые ландшафты"
        ))
        
        add(PracticeSeed(
            id = "sound_thunder_storm",
            type = PracticeType.SOUND,
            title = "Гроза",
            description = "Звуки далекой грозы для мощной трансформации и освобождения от блоков.",
            durationSec = 720,
            patternId = "sound_thunder",
            audioUrl = "audio/thunder_storm.mp3",
            tags = listOf("sound", "thunder", "storm", "transformation"),
            difficulty = "intermediate",
            category = "Звуковые ландшафты"
        ))
        
        add(PracticeSeed(
            id = "sound_wind_chimes",
            type = PracticeType.SOUND,
            title = "Ветровые колокольчики",
            description = "Нежные звуки ветряных колокольчиков для легкости и ясности ума.",
            durationSec = 600,
            patternId = "sound_wind_chimes",
            audioUrl = "audio/wind_chimes.mp3",
            tags = listOf("sound", "wind", "chimes", "clarity"),
            difficulty = "beginner",
            category = "Звуковые ландшафты"
        ))
    }
    
    fun getByCategory(category: String): List<PracticeSeed> {
        return provideAll().filter { it.category == category }
    }
    
    fun getByType(type: PracticeType): List<PracticeSeed> {
        return provideAll().filter { it.type == type }
    }
    
    fun getByDifficulty(difficulty: String): List<PracticeSeed> {
        return provideAll().filter { it.difficulty == difficulty }
    }
}
