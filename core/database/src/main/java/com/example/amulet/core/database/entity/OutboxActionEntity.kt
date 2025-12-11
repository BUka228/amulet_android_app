package com.example.amulet.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class OutboxActionType(val apiEndpoint: String) {
    USER_INIT("/users.me.init"),
    USER_UPDATE("/users.me"),
    USER_DELETE("/privacy/delete"),
    DEVICE_CLAIM("/devices.claim"),
    DEVICE_UPDATE("/devices/{id}"),
    DEVICE_UNCLAIM("/devices/{id}/unclaim"),
    DEVICE_FIRMWARE_REPORT("/devices/{deviceId}/firmware/report"),
    HUG_SEND("/hugs.send"),
    HUG_DEVICE_PLAY("local/amulet/play"),
    PAIR_INVITE("/pairs.invite"),
    PAIR_ACCEPT("/pairs.accept"),
    PAIR_BLOCK("/pairs/{pairId}/block"),
    PAIR_UNBLOCK("/pairs/{pairId}/unblock"),
    PAIR_SETTINGS_UPDATE("/pairs/{pairId}/members/{userId}/settings"),
    PATTERN_CREATE("/patterns"),
    PATTERN_UPDATE("/patterns/{id}"),
    PATTERN_DELETE("/patterns/{id}"),
    PATTERN_SHARE("/patterns/{patternId}/share"),
    PATTERN_SEGMENTS_UPDATE("/patterns/{id}/segments"),
    PATTERN_MARKERS_UPDATE("/patterns/{id}/markers"),
    PRACTICE_START("/practices/{practiceId}/start"),
    PRACTICE_STOP("/practices.session/{sessionId}/stop"),
    FCM_TOKEN_ADD("/notifications.tokens"),
    FCM_TOKEN_DELETE("/notifications.tokens"),
    RULE_CREATE("/rules"),
    RULE_UPDATE("/rules/{ruleId}"),
    RULE_DELETE("/rules/{ruleId}"),
    PRIVACY_EXPORT("/privacy/export"),
    PRIVACY_DELETE("/privacy/delete")
}

enum class OutboxActionStatus {
    PENDING,
    IN_FLIGHT,
    FAILED,
    COMPLETED
}

@Entity(
    tableName = "outbox_actions",
    indices = [
        Index(value = ["status", "availableAt", "priority"]),
        Index(value = ["type", "status"]),
        Index(value = ["targetEntityId"]),
        Index(value = ["idempotencyKey"], unique = true)
    ]
)
data class OutboxActionEntity(
    @PrimaryKey val id: String,
    val type: OutboxActionType,
    val payloadJson: String,
    val status: OutboxActionStatus,
    val retryCount: Int,
    val lastError: String?,
    val idempotencyKey: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val availableAt: Long,
    val priority: Int,
    val targetEntityId: String?
)
