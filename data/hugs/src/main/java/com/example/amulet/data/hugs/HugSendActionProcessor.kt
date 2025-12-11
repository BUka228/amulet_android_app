package com.example.amulet.data.hugs

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.OutboxActionType
import com.example.amulet.core.network.dto.hug.HugSendRequestDto
import com.example.amulet.core.sync.processing.ActionProcessor
import com.example.amulet.data.hugs.datasource.remote.HugsRemoteDataSource
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.fold
import javax.inject.Inject
import kotlinx.serialization.json.Json

class HugSendActionProcessor @Inject constructor(
    private val remoteDataSource: HugsRemoteDataSource,
    private val json: Json,
) : ActionProcessor {

    override suspend fun process(action: OutboxActionEntity): AppResult<Unit> {
        if (action.type != OutboxActionType.HUG_SEND) {
            return Err(AppError.Validation(mapOf("type" to "Unsupported action type")))
        }

        val request = runCatching { json.decodeFromString(HugSendRequestDto.serializer(), action.payloadJson) }
            .getOrElse { return Err(AppError.Validation(mapOf("payload" to "Invalid JSON"))) }

        val result = remoteDataSource.sendHug(request)

        return result.fold(
            success = { Ok(Unit) },
            failure = { error -> Err(error) }
        )
    }
}
