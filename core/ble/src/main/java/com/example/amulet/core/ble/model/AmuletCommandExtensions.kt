package com.example.amulet.core.ble.model

import com.example.amulet.shared.domain.devices.model.AmuletCommand
import com.example.amulet.shared.domain.devices.model.ChaseDirection

/**
 * Extension функции для преобразования доменных команд в BLE протокол.
 * Согласно спецификации из docs/20_DATA_LAYER/03_BLE_PROTOCOL.md
 */

/**
 * Преобразование команды в строку протокола BLE.
 */
fun AmuletCommand.toCommandString(): String = when (this) {
    is AmuletCommand.Breathing -> "BREATHING:${color.toHex()}:${durationMs}ms"
    is AmuletCommand.Pulse -> "PULSE:${color.toHex()}:${intervalMs}ms:$repeats"
    is AmuletCommand.Chase -> "CHASE:${color.toHex()}:${direction.toCode()}:$speedMs"
    is AmuletCommand.Fill -> "FILL:${color.toHex()}:$durationMs"
    is AmuletCommand.Spinner -> "SPINNER:${colors.joinToString(",") { it.toHex() }}:$speedMs"
    is AmuletCommand.Progress -> "PROGRESS:${color.toHex()}:$activeLeds"
    is AmuletCommand.SetRing -> "SET_RING:${colors.joinToString(":") { it.toHex() }}"
    is AmuletCommand.SetLed -> "SET_LED:$index:${color.toHex()}"
    is AmuletCommand.ClearAll -> "CLEAR_ALL"
    is AmuletCommand.Delay -> "DELAY:$durationMs"
    is AmuletCommand.Play -> "PLAY:$patternId"
    is AmuletCommand.SetWifiCred -> "SET_WIFI_CRED:$ssidBase64:$passwordBase64"
    is AmuletCommand.WifiOtaStart -> "WIFI_OTA_START:$url:$version:$checksum"
    is AmuletCommand.GetProtocolVersion -> "GET_PROTOCOL_VERSION"
    is AmuletCommand.Custom -> if (parameters.isEmpty()) command else "$command:${parameters.joinToString(":")}"
}

/**
 * Преобразование направления в код протокола.
 */
private fun ChaseDirection.toCode(): String = when (this) {
    ChaseDirection.CLOCKWISE -> "CW"
    ChaseDirection.COUNTER_CLOCKWISE -> "CCW"
}
