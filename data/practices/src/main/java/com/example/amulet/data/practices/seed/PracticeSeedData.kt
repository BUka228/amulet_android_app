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
            updatedAt = System.currentTimeMillis(),
            steps = listOf(
                "Сделайте спокойный выдох через рот, полностью освобождая лёгкие",
                "Медленно вдохните через нос на 4 счета",
                "Задержите дыхание на 7 счетов",
                "Мягко выдыхайте через рот на 8 счетов",
                "Повторяйте цикл, удерживая внимание на ощущениях в теле"
            ),
            safetyNotes = listOf(
                "Лучше выполнять сидя или лёжа в безопасной обстановке",
                "Не выполнять во время вождения и работы с механизмами"
            )
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
            updatedAt = System.currentTimeMillis(),
            steps = listOf(
                "Сделайте плавный вдох через нос на 4 счета",
                "Задержите дыхание на 4 счета, сохраняя мягкое напряжение",
                "Выдохните через нос или рот на 4 счета",
                "Сделайте паузу без вдоха на 4 счета",
                "Повторяйте цикл, отслеживая снижение напряжения"
            ),
            safetyNotes = listOf(
                "Выполняйте сидя с опорой для спины",
                "Остановитесь при сильном головокружении или дискомфорте"
            )
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
            updatedAt = System.currentTimeMillis(),
            steps = listOf(
                "Сядьте с прямой спиной, сделайте несколько спокойных вдохов",
                "Начните делать быстрые короткие вдохи и выдохи через нос",
                "Держите дыхание ритмичным, не перенапрягая живот и грудь",
                "Через 20–30 секунд сделайте один глубокий очищающий выдох",
                "Сделайте небольшую паузу и повторите цикл, если чувствуете себя комфортно"
            ),
            safetyNotes = listOf(
                "Выполняйте только сидя или стоя, не лёжа",
                "Не выполнять при выраженном недомогании, болях в груди или головокружении"
            )
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
            updatedAt = System.currentTimeMillis(),
            steps = listOf(
                "Устройтесь удобно сидя, выровняйте спину, закройте или прикройте глаза",
                "Перенесите внимание на дыхание: ощущайте вдохи и выдохи в теле",
                "Замечайте возникающие мысли и звуки, не вовлекаясь, мягко возвращайтесь к дыханию",
                "Отмечайте ощущения в теле: напряжение, тепло, покалывание, не оценивая их",
                "В конце сделайте несколько более глубоких вдохов и мягко откройте глаза"
            ),
            safetyNotes = listOf(
                "Лучше выполнять сидя с устойчивой опорой",
                "При повышенной сонливости можно выполнять с открытыми глазами"
            )
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
            updatedAt = System.currentTimeMillis(),
            steps = listOf(
                "Лягте или сядьте удобно, дайте телу опереться на поверхность",
                "Перенесите внимание на стопы и постепенно поднимайтесь выше, отмечая ощущения",
                "Расслабляйте каждую область тела: ноги, таз, живот, грудь, руки, шею и лицо",
                "Если замечаете напряжение, сделайте мягкий выдох и представьте, как оно уходит",
                "В завершение охватите вниманием всё тело целиком и поблагодарите себя за практику"
            ),
            safetyNotes = listOf(
                "Оптимально выполнять лёжа или полулёжа в спокойной обстановке",
                "Не выполнять за рулём и при необходимости сохранять активное внимание"
            )
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
            updatedAt = System.currentTimeMillis(),
            steps = listOf(
                "Устройтесь в комфортной позе для сна, минимизируйте свет и шум",
                "Сделайте несколько медленных глубоких вдохов, отпуская события дня на выдохе",
                "Представьте мягкую волну расслабления, которая медленно проходит от стоп к голове",
                "Создайте в воображении спокойный образ: безопасное место, тёплый свет или природу",
                "Позвольте вниманию постепенно раствориться в ощущении покоя и тяжести тела"
            ),
            safetyNotes = listOf(
                "Выполнять только перед сном или в условиях, где можно заснуть",
                "Не использовать в ситуациях, требующих концентрации (вождение, работа)"
            )
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
            updatedAt = System.currentTimeMillis(),
            steps = listOf(
                "Выберите объект фокуса: точку на стене, дыхание или внутренний образ",
                "Сядьте с прямой спиной и удерживайте мягкий, устойчивый взгляд или внимание",
                "Когда замечаете отвлечение, спокойно возвращайте внимание к выбранному объекту",
                "Отслеживайте моменты, когда концентрация усиливается, не оценивая себя",
                "В конце сделайте несколько глубоких вдохов и перенесите фокус обратно в комнату"
            ),
            safetyNotes = listOf(
                "Лучше выполнять сидя за столом или в рабочей обстановке",
                "Избегать выполнения при сильной усталости или сонливости"
            )
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
            updatedAt = System.currentTimeMillis(),
            steps = listOf(
                "Устройтесь удобно, закройте глаза или смягчите взгляд",
                "Начните слушать звуки океана, отмечая ритм волн и паузы между ними",
                "Синхронизируйте дыхание с волнами: мягкий вдох на подъёме звука, выдох на спаде",
                "Позвольте мыслям свободно приходить и уходить, удерживая внимание на звуках",
                "В конце практики сделайте несколько осознанных вдохов и вернитесь к окружающему пространству"
            ),
            safetyNotes = listOf(
                "Можно выполнять сидя или лёжа, лучше в наушниках",
                "Не рекомендуется использовать во время управления транспортом"
            )
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
            updatedAt = System.currentTimeMillis(),
            steps = listOf(
                "Сядьте или лягте комфортно, почувствуйте опору под телом",
                "Начните прислушиваться ко всем слоям звука: птицы, листва, вода",
                "Выберите один звук и некоторое время удерживайте на нём внимание",
                "Расширьте фокус, охватывая сразу весь звуковой фон как единое целое",
                "Отметьте, как меняется состояние тела и ума по мере погружения в звук"
            ),
            safetyNotes = listOf(
                "Рекомендуется выполнять в спокойной, безопасной обстановке",
                "При повышенной тревожности можно оставить немного света или выполнять с открытыми глазами"
            )
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
            updatedAt = System.currentTimeMillis(),
            steps = listOf(
                "Сделайте несколько бодрящих вдохов и мягких потягиваний тела",
                "Выполните короткий цикл активного дыхания для пробуждения",
                "Перейдите к нескольким минутам спокойного наблюдения за дыханием",
                "Произнесите про себя или вслух 1–3 поддерживающие аффирмации на день",
                "Закончите практику, наметив один маленький осознанный шаг на ближайший час"
            ),
            safetyNotes = listOf(
                "Лучше выполнять утром, до кофе и плотного приёма пищи",
                "При проблемах с давлением делать дыхательные части практики мягче"
            )
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
            updatedAt = System.currentTimeMillis(),
            steps = listOf(
                "Сделайте несколько глубоких выдохов, отпуская напряжение плеч и шеи",
                "Выполните короткий цикл ровного дыхания (например, 4–4–4–4)",
                "Перенесите внимание на одну напряжённую область тела и мягко её расслабьте",
                "Сделайте ещё один цикл дыхания, наблюдая, как снижается эмоциональное напряжение",
                "Сформулируйте для себя один небольшой поддерживающий настрой на ближайшее время"
            ),
            safetyNotes = listOf(
                "Оптимально выполнять сидя в относительно спокойной обстановке (кабинет, переговорная)",
                "Можно использовать в коротких перерывах между задачами"
            )
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
            updatedAt = System.currentTimeMillis(),
            steps = listOf(
                "Заметьте уровень тревоги по субъективной шкале от 0 до 10",
                "Сделайте несколько циклов успокаивающего дыхания с удлинённым выдохом",
                "Переведите внимание на опору: стопы, поверхность стула или кровати",
                "Назовите про себя 3–5 объектов вокруг и 3–5 ощущений в теле, закрепляя себя в настоящем моменте",
                "Снова оцените уровень тревоги и отметьте, что изменилось"
            ),
            safetyNotes = listOf(
                "Лучше выполнять сидя, с возможностью опереться спиной и ногами",
                "При очень высокой тревоге можно сократить длительность и повторять чаще в течение дня"
            )
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
            updatedAt = System.currentTimeMillis(),
            steps = listOf(
                "Сделайте несколько мягких вдохов и выдохов, замечая текущее состояние",
                "Вспомните один приятный момент из недавнего прошлого и погрузитесь в его детали",
                "Отметьте в теле любые даже самые небольшие приятные ощущения",
                "Представьте светлый, поддерживающий образ или цвет, заполняющий пространство вокруг",
                "В завершение сформулируйте одно маленькое действие, которое может поддержать ваше настроение сегодня"
            ),
            safetyNotes = listOf(
                "Можно выполнять сидя или лёжа, в безопасной и спокойной обстановке",
                "Не использовать как замену профессиональной помощи при выраженных депрессивных состояниях"
            )
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
            updatedAt = System.currentTimeMillis(),
            steps = listOf(
                "Создайте устойчивые внешние условия: тишина, удобная поза, отсутствие отвлечений",
                "Проведите несколько минут в стабилизирующей практике дыхания или лёгкого сканирования тела",
                "Перейдите к устойчивому фокусу (объект медитации) и удерживайте внимание в выбранной точке",
                "Позвольте вниманию мягко углубляться, наблюдая возникающие состояния без оценки и анализа",
                "Завершите практику постепенным возвращением к ощущениям в теле и пространству комнаты"
            ),
            safetyNotes = listOf(
                "Рекомендуется опытным практикам, в условиях, где можно позволить себе длительное погружение",
                "При обострении психических состояний практику нужно согласовывать со специалистом"
            )
        )
    )
}
