package com.example.amulet.shared.domain.devices.model

import kotlinx.serialization.Serializable

/**
 * Value класс для идентификатора устройства.
 */
@Serializable
@JvmInline
value class DeviceId(val value: String) {
    init {
        require(value.isNotBlank()) { "Device ID cannot be blank" }
    }
    
    override fun toString(): String = value
}
