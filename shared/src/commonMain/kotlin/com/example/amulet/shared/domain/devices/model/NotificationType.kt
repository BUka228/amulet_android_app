package com.example.amulet.shared.domain.devices.model

/**
 * Типы BLE-уведомлений NOTIFY:TYPE:...
 */
enum class NotificationType {
    BATTERY,
    STATUS,
    OTA,
    WIFI_OTA,
    PATTERN,
    ANIMATION,
    CUSTOM
}
