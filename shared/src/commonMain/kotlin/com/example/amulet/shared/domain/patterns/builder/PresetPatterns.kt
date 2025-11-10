package com.example.amulet.shared.domain.patterns.builder

import com.example.amulet.shared.domain.patterns.model.ChaseDirection
import com.example.amulet.shared.domain.patterns.model.PatternSpec

/**
 * Фабрика предустановленных паттернов.
 * Предоставляет готовые паттерны для быстрого использования.
 */
object PresetPatterns {
    
    /**
     * Паттерн "Спокойствие" - медленное дыхание синим цветом.
     */
    fun calm(hardwareVersion: Int = 100): PatternSpec = buildPattern {
        setHardwareVersion(hardwareVersion)
        setLoop(true)
        addBreathing("#0066FF", 4000)
    }
    
    /**
     * Паттерн "Энергия" - быстрые пульсации красным цветом.
     */
    fun energy(hardwareVersion: Int = 100): PatternSpec = buildPattern {
        setHardwareVersion(hardwareVersion)
        setLoop(true)
        addPulse("#FF0000", 300, 5)
    }
    
    /**
     * Паттерн "Радость" - вращающиеся желто-оранжевые огни.
     */
    fun joy(hardwareVersion: Int = 100): PatternSpec = buildPattern {
        setHardwareVersion(hardwareVersion)
        setLoop(true)
        addChase("#FFAA00", ChaseDirection.CLOCKWISE, 150)
    }
    
    /**
     * Паттерн "Любовь" - розовое дыхание с пульсацией.
     */
    fun love(hardwareVersion: Int = 100): PatternSpec = buildPattern {
        setHardwareVersion(hardwareVersion)
        setLoop(true)
        addBreathing("#FF69B4", 3000)
        addPulse("#FF1493", 500, 2)
    }
    
    /**
     * Паттерн "Медитация" - медленное фиолетовое дыхание.
     */
    fun meditation(hardwareVersion: Int = 100): PatternSpec = buildPattern {
        setHardwareVersion(hardwareVersion)
        setLoop(true)
        addBreathing("#9370DB", 6000)
    }
    
    /**
     * Паттерн "Фокус" - зеленое заполнение с прогрессом.
     */
    fun focus(hardwareVersion: Int = 100): PatternSpec = buildPattern {
        setHardwareVersion(hardwareVersion)
        addFill("#00FF00", 2000)
        addProgress("#00FF00", 8)
    }
    
    /**
     * Паттерн "Радуга" - последовательность разных цветов.
     */
    fun rainbow(hardwareVersion: Int = 100): PatternSpec = buildPattern {
        setHardwareVersion(hardwareVersion)
        setLoop(true)
        addBreathing("#FF0000", 1000) // Красный
        addBreathing("#FF7F00", 1000) // Оранжевый
        addBreathing("#FFFF00", 1000) // Желтый
        addBreathing("#00FF00", 1000) // Зеленый
        addBreathing("#0000FF", 1000) // Синий
        addBreathing("#4B0082", 1000) // Индиго
        addBreathing("#9400D3", 1000) // Фиолетовый
    }
    
    /**
     * Паттерн "Вечеринка" - быстрые разноцветные спиннеры.
     */
    fun party(hardwareVersion: Int = 100): PatternSpec = buildPattern {
        setHardwareVersion(hardwareVersion)
        setLoop(true)
        addSpinner("#FF00FF", "#00FFFF", 100)
        addSpinner("#FFFF00", "#FF0000", 100)
    }
    
    /**
     * Секретный код "Я скучаю" - двойная пульсация верхнего диода, потом одинарная нижнего.
     */
    fun secretCodeMissYou(hardwareVersion: Int = 100): PatternSpec = buildPattern {
        setHardwareVersion(hardwareVersion)
        addSequence {
            led(0, "#FF00FF", 150)
            delay(100)
            led(0, "#FF00FF", 150)
            delay(400)
            led(4, "#FFFF00", 200)
        }
    }
    
    /**
     * Секретный код "Думаю о тебе" - тройная пульсация по кругу.
     */
    fun secretCodeThinkingOfYou(hardwareVersion: Int = 100): PatternSpec = buildPattern {
        setHardwareVersion(hardwareVersion)
        addSequence {
            led(0, "#00FFFF", 100)
            delay(50)
            led(2, "#00FFFF", 100)
            delay(50)
            led(4, "#00FFFF", 100)
            delay(50)
            led(6, "#00FFFF", 100)
        }
    }
    
    /**
     * Секретный код "Люблю тебя" - сердцебиение (пульсация красным).
     */
    fun secretCodeLoveYou(hardwareVersion: Int = 100): PatternSpec = buildPattern {
        setHardwareVersion(hardwareVersion)
        addSequence {
            led(0, "#FF0000", 100)
            led(4, "#FF0000", 100)
            delay(200)
            led(0, "#FF0000", 100)
            led(4, "#FF0000", 100)
            delay(500)
        }
    }
    
    /**
     * Паттерн "Дыхательная практика 4-7-8" - визуализация дыхательной техники.
     */
    fun breathing478(hardwareVersion: Int = 100): PatternSpec = buildPattern {
        setHardwareVersion(hardwareVersion)
        setLoop(true)
        // Вдох 4 секунды
        addFill("#00AAFF", 4000)
        // Задержка 7 секунд
        addProgress("#00AAFF", 8)
        // Выдох 8 секунд
        addBreathing("#00AAFF", 8000)
    }
    
    /**
     * Получить все предустановленные паттерны с их названиями.
     */
    fun getAllPresets(hardwareVersion: Int = 100): Map<String, PatternSpec> = mapOf(
        "Спокойствие" to calm(hardwareVersion),
        "Энергия" to energy(hardwareVersion),
        "Радость" to joy(hardwareVersion),
        "Любовь" to love(hardwareVersion),
        "Медитация" to meditation(hardwareVersion),
        "Фокус" to focus(hardwareVersion),
        "Радуга" to rainbow(hardwareVersion),
        "Вечеринка" to party(hardwareVersion),
        "Дыхание 4-7-8" to breathing478(hardwareVersion)
    )
    
    /**
     * Получить все секретные коды с их названиями.
     */
    fun getAllSecretCodes(hardwareVersion: Int = 100): Map<String, PatternSpec> = mapOf(
        "Я скучаю" to secretCodeMissYou(hardwareVersion),
        "Думаю о тебе" to secretCodeThinkingOfYou(hardwareVersion),
        "Люблю тебя" to secretCodeLoveYou(hardwareVersion)
    )
}
