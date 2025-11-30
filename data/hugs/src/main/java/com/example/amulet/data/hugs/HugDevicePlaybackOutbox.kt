package com.example.amulet.data.hugs

import com.example.amulet.core.database.dao.OutboxActionDao
import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.OutboxActionStatus
import com.example.amulet.core.database.entity.OutboxActionType
import com.example.amulet.core.sync.scheduler.OutboxScheduler
import com.example.amulet.shared.domain.hugs.model.HugId
import com.example.amulet.shared.domain.patterns.model.PatternId
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

/**
 * Outbox-обёртка для отложенного воспроизведения hug-паттерна на амулете.
 */
class HugDevicePlaybackOutbox @Inject constructor(
    private val outboxActionDao: OutboxActionDao,
    private val outboxScheduler: OutboxScheduler,
    private val json: Json,
) {

    suspend fun enqueue(
        hugId: HugId,
        patternId: PatternId,
        intensity: Double = 1.0,
    ) {
        val now = System.currentTimeMillis()

        val payloadObject = buildJsonObject {
            put("hugId", hugId.value)
            put("patternId", patternId.value)
            put("intensity", intensity)
        }

        val action = OutboxActionEntity(
            id = UUID.randomUUID().toString(),
            type = OutboxActionType.HUG_DEVICE_PLAY,
            payloadJson = json.encodeToString(payloadObject),
            status = OutboxActionStatus.PENDING,
            retryCount = 0,
            lastError = null,
            idempotencyKey = "hug_device_play_${hugId.value}",
            createdAt = now,
            updatedAt = now,
            availableAt = now,
            priority = 1,
            targetEntityId = hugId.value,
        )

        outboxActionDao.upsert(action)
        // Планируем синхронизацию Outbox, чтобы задача была обработана как можно скорее.
        outboxScheduler.scheduleSync()
    }
}
