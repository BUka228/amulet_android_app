package com.example.amulet.shared.domain.devices.model

/**
 * Команды для управления амулетом.
 * Это доменная модель, не зависящая от BLE протокола.
 * Преобразование в BLE строки происходит в :core:ble модуле.
 */
sealed interface AmuletCommand {
    
    // Управление светодиодами (низкоуровневые команды)
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
    
    // Встроенные анимации (идентификатор паттерна на устройстве)
    data class Play(
        val patternId: String
    ) : AmuletCommand
    
    data class HasPlan(
        val patternId: String
    ) : AmuletCommand
    
    // Скрипты практик (PRACTICE_SCRIPT)
    data class BeginPracticeScript(
        val practiceId: String
    ) : AmuletCommand

    data class AddPracticeStep(
        val practiceId: String,
        val order: Int,
        val patternId: String,
    ) : AmuletCommand

    data class CommitPracticeScript(
        val practiceId: String
    ) : AmuletCommand

    data class HasPracticeScript(
        val practiceId: String
    ) : AmuletCommand

    data class PlayPracticeScript(
        val practiceId: String
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
