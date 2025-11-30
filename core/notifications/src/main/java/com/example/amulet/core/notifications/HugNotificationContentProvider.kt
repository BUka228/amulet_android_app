package com.example.amulet.core.notifications

data class HugNotificationContent(
    val title: String,
    val message: String,
)

object HugNotificationContentProvider {

    fun queuedHug(emotionName: String?, emotionColorHex: String?): HugNotificationContent {
        val title = "Объятие в очереди"

        val name = emotionName?.lowercase()?.trim()
        val color = emotionColorHex?.lowercase()?.trim()

        val message = when {
            name != null && ("люб" in name || "love" in name || "heart" in name) ->
                "Тёплое объятие в очереди, дойдёт при подключении амулета."

            name != null && ("поддерж" in name || "support" in name) ->
                "Объятие поддержки в очереди, дойдёт при подключении амулета."

            name != null && ("тих" in name || "calm" in name || "спок" in name) ->
                "Тихое объятие в очереди, дойдёт при подключении амулета."

            color != null && (color.startsWith("#ff") || color.startsWith("#e")) ->
                "Яркое объятие в очереди, дойдёт при подключении амулета."

            else ->
                "Объятие будет воспроизведено, когда амулет подключится."
        }

        return HugNotificationContent(
            title = title,
            message = message,
        )
    }
}
