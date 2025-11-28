package com.example.amulet.data.practices.seed

import com.example.amulet.shared.domain.practices.model.PracticeScript
import com.example.amulet.shared.domain.practices.model.PracticeStep
import com.example.amulet.shared.domain.practices.model.PracticeStepType

/**
 * Seed данные для скриптов практик с детализированными шагами.
 * Каждый шаг имеет конкретную длительность и описание для анимации.
 */
object PracticeScriptSeedData {
    
    /**
     * Скрипт для практики "Дыхание 4-7-8"
     * Классическая техника: вдох 4 сек, задержка 7 сек, выдох 8 сек.
     * Общая длительность одного цикла: 4 + 7 + 8 = 19 секунд
     * За 5 минут (300 сек) = примерно 15-16 циклов
     */
    fun getBreathing478Script(): PracticeScript {
        val cycles = 16 // Количество полных циклов
        val steps = mutableListOf<PracticeStep>()
        
        // Добавляем вступительный шаг
        steps.add(
            PracticeStep(
                order = 0,
                type = PracticeStepType.TEXT_HINT,
                title = "Подготовка",
                description = "Устройтесь удобно, закройте глаза. Следуйте за светом амулета.",
                durationSec = 5,
                patternId = "pattern_breathing_calm"
            )
        )
        
        // Генерируем циклы дыхания
        repeat(cycles) { cycleIndex ->
            val baseOrder = 1 + (cycleIndex * 3)
            
            // Вдох - 4 секунды
            steps.add(
                PracticeStep(
                    order = baseOrder,
                    type = PracticeStepType.BREATH_STEP,
                    title = "Вдох",
                    description = "Медленно вдохните через нос",
                    durationSec = 4,
                    patternId = "pattern_breathing_calm"
                )
            )
            
            // Задержка - 7 секунд
            steps.add(
                PracticeStep(
                    order = baseOrder + 1,
                    type = PracticeStepType.BREATH_STEP,
                    title = "Задержка",
                    description = "Задержите дыхание",
                    durationSec = 7,
                    patternId = "pattern_breathing_calm"
                )
            )
            
            // Выдох - 8 секунд
            steps.add(
                PracticeStep(
                    order = baseOrder + 2,
                    type = PracticeStepType.BREATH_STEP,
                    title = "Выдох",
                    description = "Медленно выдохните через рот",
                    durationSec = 8,
                    patternId = "pattern_breathing_calm"
                )
            )
        }
        
        // Завершающий шаг
        steps.add(
            PracticeStep(
                order = steps.size,
                type = PracticeStepType.TEXT_HINT,
                title = "Завершение",
                description = "Отлично! Сделайте несколько свободных вдохов.",
                durationSec = 5,
                patternId = "pattern_breathing_calm"
            )
        )
        
        return PracticeScript(steps = steps)
    }
    
    /**
     * Скрипт для практики "Квадратное дыхание"
     * Все фазы по 4 секунды: вдох, задержка, выдох, задержка.
     * Общая длительность одного цикла: 4 * 4 = 16 секунд
     * За 7 минут (420 сек) = примерно 26 циклов
     */
    fun getBoxBreathingScript(): PracticeScript {
        val cycles = 26
        val steps = mutableListOf<PracticeStep>()
        
        steps.add(
            PracticeStep(
                order = 0,
                type = PracticeStepType.TEXT_HINT,
                title = "Подготовка",
                description = "Устройтесь удобно. Каждая фаза длится 4 секунды.",
                durationSec = 6,
                patternId = "pattern_antistress_quick"
            )
        )
        
        repeat(cycles) { cycleIndex ->
            val baseOrder = 1 + (cycleIndex * 4)
            
            steps.add(
                PracticeStep(
                    order = baseOrder,
                    type = PracticeStepType.BREATH_STEP,
                    title = "Вдох",
                    description = "Плавный вдох через нос",
                    durationSec = 4,
                    patternId = "pattern_antistress_quick"
                )
            )
            
            steps.add(
                PracticeStep(
                    order = baseOrder + 1,
                    type = PracticeStepType.BREATH_STEP,
                    title = "Задержка",
                    description = "Задержите дыхание",
                    durationSec = 4,
                    patternId = "pattern_antistress_quick"
                )
            )
            
            steps.add(
                PracticeStep(
                    order = baseOrder + 2,
                    type = PracticeStepType.BREATH_STEP,
                    title = "Выдох",
                    description = "Выдохните через нос или рот",
                    durationSec = 4,
                    patternId = "pattern_antistress_quick"
                )
            )
            
            steps.add(
                PracticeStep(
                    order = baseOrder + 3,
                    type = PracticeStepType.BREATH_STEP,
                    title = "Пауза",
                    description = "Пауза без вдоха",
                    durationSec = 4,
                    patternId = "pattern_antistress_quick"
                )
            )
        }
        
        steps.add(
            PracticeStep(
                order = steps.size,
                type = PracticeStepType.TEXT_HINT,
                title = "Завершение",
                description = "Практика завершена. Отличная работа!",
                durationSec = 6,
                patternId = "pattern_antistress_quick"
            )
        )
        
        return PracticeScript(steps = steps)
    }
    
    /**
     * Скрипт для практики "Бодрящее дыхание"
     * Активные быстрые циклы с паузами для восстановления.
     */
    fun getEnergizingBreathingScript(): PracticeScript {
        val rounds = 6 // 6 раундов по 30 секунд
        val steps = mutableListOf<PracticeStep>()
        
        steps.add(
            PracticeStep(
                order = 0,
                type = PracticeStepType.TEXT_HINT,
                title = "Подготовка",
                description = "Сядьте с прямой спиной. Приготовьтесь к активному дыханию.",
                durationSec = 5,
                patternId = "pattern_breathing_energy"
            )
        )
        
        repeat(rounds) { roundIndex ->
            val baseOrder = 1 + (roundIndex * 2)
            
            // Активное дыхание
            steps.add(
                PracticeStep(
                    order = baseOrder,
                    type = PracticeStepType.BREATH_STEP,
                    title = "Активное дыхание",
                    description = "Быстрые короткие вдохи-выдохи через нос",
                    durationSec = 20,
                    patternId = "pattern_breathing_energy"
                )
            )
            
            // Отдых
            steps.add(
                PracticeStep(
                    order = baseOrder + 1,
                    type = PracticeStepType.BREATH_STEP,
                    title = "Отдых",
                    description = "Свободное дыхание, расслабьтесь",
                    durationSec = 10,
                    patternId = "pattern_breathing_energy"
                )
            )
        }
        
        steps.add(
            PracticeStep(
                order = steps.size,
                type = PracticeStepType.TEXT_HINT,
                title = "Завершение",
                description = "Почувствуйте прилив энергии!",
                durationSec = 5,
                patternId = "pattern_breathing_energy"
            )
        )
        
        return PracticeScript(steps = steps)
    }
    
    /**
     * Получить скрипт практики по её ID
     */
    fun getScriptForPractice(practiceId: String): PracticeScript? {
        return when (practiceId) {
            "practice_breathing_478" -> getBreathing478Script()
            "practice_breathing_box" -> getBoxBreathingScript()
            "practice_breathing_energizing" -> getEnergizingBreathingScript()
            else -> null
        }
    }
}
