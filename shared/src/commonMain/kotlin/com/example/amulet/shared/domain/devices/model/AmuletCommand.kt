package com.example.amulet.shared.domain.devices.model

/**
 * Команды для управления амулетом.
 * Это доменная модель, не зависящая от BLE протокола.
 * Преобразование в BLE строки происходит в :core:ble модуле.
 */
sealed interface AmuletCommand {
    
    // Базовые анимации
    data class Breathing(
        val color: Rgb,
        val durationMs: Int
    ) : AmuletCommand
    
    data class Pulse(
        val color: Rgb,
        val intervalMs: Int,
        val repeats: Int
    ) : AmuletCommand
    
    data class Chase(
        val color: Rgb,
        val direction: ChaseDirection,
        val speedMs: Int
    ) : AmuletCommand
    
    data class Fill(
        val color: Rgb,
        val durationMs: Int
    ) : AmuletCommand
    
    data class Spinner(
        val colors: List<Rgb>,
        val speedMs: Int
    ) : AmuletCommand
    
    data class Progress(
        val color: Rgb,
        val activeLeds: Int // 0-8
    ) : AmuletCommand
    
    // Управление светодиодами
    data class SetRing(
        val colors: List<Rgb> // 8 элементов
    ) : AmuletCommand
    
    data class SetLed(
        val index: Int, // 0-7
        val color: Rgb
    ) : AmuletCommand
    
    data object ClearAll : AmuletCommand
    
    data class Delay(
        val durationMs: Int
    ) : AmuletCommand
    
    // Встроенные анимации
    data class Play(
        val patternId: String
    ) : AmuletCommand
    
    // Wi-Fi OTA команды
    data class SetWifiCred(
        val ssidBase64: String,
        val passwordBase64: String
    ) : AmuletCommand
    
    data class WifiOtaStart(
        val url: String,
        val version: String,
        val checksum: String
    ) : AmuletCommand
    
    // Версия протокола
    data object GetProtocolVersion : AmuletCommand
    
    // Произвольная команда
    data class Custom(
        val command: String,
        val parameters: List<String> = emptyList()
    ) : AmuletCommand
}

enum class ChaseDirection {
    CLOCKWISE,
    COUNTER_CLOCKWISE
}

data class Rgb(
    val red: Int,
    val green: Int,
    val blue: Int
) {
    init {
        require(red in 0..255) { "Red must be in 0..255" }
        require(green in 0..255) { "Green must be in 0..255" }
        require(blue in 0..255) { "Blue must be in 0..255" }
    }
    
    fun toHex(): String = "#%02X%02X%02X".format(red, green, blue)
    
    companion object {
        fun fromHex(hex: String): Rgb {
            val cleanHex = hex.removePrefix("#")
            require(cleanHex.length == 6) { "Hex color must be 6 characters" }
            
            return Rgb(
                red = cleanHex.substring(0, 2).toInt(16),
                green = cleanHex.substring(2, 4).toInt(16),
                blue = cleanHex.substring(4, 6).toInt(16)
            )
        }
    }
}
