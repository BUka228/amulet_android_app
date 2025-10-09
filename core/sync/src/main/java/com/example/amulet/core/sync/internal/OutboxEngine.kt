package com.example.amulet.core.sync.internal

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.sync.processing.ActionProcessor
import com.example.amulet.core.sync.processing.ActionProcessorFactory
import com.example.amulet.shared.core.logging.Logger
import com.github.michaelbull.result.fold
import javax.inject.Inject

class OutboxEngine @Inject constructor(
    private val store: OutboxActionStore,
    private val processorFactory: ActionProcessorFactory,
    private val errorResolver: ActionErrorResolver,
    private val timeProvider: TimeProvider,
    private val config: OutboxSyncConfig
) {

    suspend fun run(): Int {
        val iterationLimit = if (config.maxActionsPerSync <= 0) Int.MAX_VALUE else config.maxActionsPerSync
        var processed = 0
        while (processed < iterationLimit) {
            val now = timeProvider.now()
            val action = store.takeNextAction(now) ?: break
            val resolution = process(action)
            val updatedAt = timeProvider.now()
            when (resolution) {
                ActionResolution.Completed -> store.markCompleted(action, updatedAt)
                is ActionResolution.Retry -> store.markForRetry(action, resolution.delayMillis, resolution.errorMessage, updatedAt)
                is ActionResolution.Failed -> store.markFailed(action, resolution.errorMessage, updatedAt)
            }
            processed += 1
        }
        return processed
    }

    private suspend fun process(action: OutboxActionEntity): ActionResolution {
        val processor = runCatching { processorFactory.get(action.type) }
            .getOrElse { throwable ->
                Logger.e(
                    "Processor not found for action type ${action.type}",
                    throwable,
                    TAG
                )
                return ActionResolution.Failed("Missing processor for ${action.type}")
            }

        return try {
            executeProcessor(processor, action)
        } catch (throwable: Throwable) {
            Logger.e(
                "Processor ${action.type} threw unexpected exception: ${throwable.message}",
                throwable,
                TAG
            )
            ActionResolution.Failed(throwable.message ?: throwable::class.simpleName ?: "Processor error")
        }
    }

    private suspend fun executeProcessor(
        processor: ActionProcessor,
        action: OutboxActionEntity
    ): ActionResolution = processor.process(action).fold(
        success = {
            Logger.d("Action ${action.id} (${action.type}) completed", TAG)
            ActionResolution.Completed
        },
        failure = { appError ->
            val resolution = errorResolver.resolve(action, appError)
            when (resolution) {
                is ActionResolution.Retry -> Logger.w(
                    "Action ${action.id} will retry in ${resolution.delayMillis} ms: ${resolution.errorMessage}",
                    tag = TAG
                )
                is ActionResolution.Failed -> Logger.e(
                    "Action ${action.id} failed permanently: ${resolution.errorMessage}",
                    throwable = null,
                    tag = TAG
                )
                ActionResolution.Completed -> Unit
            }
            resolution
        }
    )

    private companion object {
        private const val TAG = "OutboxEngine"
    }
}
