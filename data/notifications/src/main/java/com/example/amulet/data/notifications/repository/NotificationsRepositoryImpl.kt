package com.example.amulet.data.notifications.repository

import com.example.amulet.core.network.dto.notifications.UserPushTokenDto
import com.example.amulet.core.network.dto.notifications.UserPushTokenRequestDto
import com.example.amulet.core.network.serialization.ApiTimestamp
import com.example.amulet.data.notifications.datasource.remote.NotificationsRemoteDataSource
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.mapSuccess
import com.example.amulet.shared.domain.notifications.model.PushToken
import com.example.amulet.shared.domain.notifications.model.PushTokenRegistration
import com.example.amulet.shared.domain.notifications.repository.NotificationsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.Instant as KtInstant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Singleton
class NotificationsRepositoryImpl @Inject constructor(
    private val remoteDataSource: NotificationsRemoteDataSource
) : NotificationsRepository {

    override suspend fun registerPushToken(registration: PushTokenRegistration): AppResult<List<PushToken>> =
        remoteDataSource.registerPushToken(registration.toRequest()).mapSuccess { dtos ->
            dtos.map { it.toDomain() }
        }

    override suspend fun getPushTokens(): AppResult<List<PushToken>> =
        remoteDataSource.getPushTokens().mapSuccess { dtos ->
            dtos.map { it.toDomain() }
        }

    override suspend fun deletePushToken(tokenId: String): AppResult<List<PushToken>> =
        remoteDataSource.deletePushToken(tokenId).mapSuccess { dtos ->
            dtos.map { it.toDomain() }
        }

    private fun PushTokenRegistration.toRequest(): UserPushTokenRequestDto =
        UserPushTokenRequestDto(
            token = token,
            platform = platform,
            lastSeenAt = lastSeenAt?.let { ApiTimestamp(it.toEpochMilliseconds()) }
        )

    private fun UserPushTokenDto.toDomain(): PushToken = PushToken(
        id = id,
        token = token,
        platform = platform,
        createdAt = createdAt?.let { KtInstant.fromEpochMilliseconds(it.value) },
        lastSeenAt = lastSeenAt?.let { KtInstant.fromEpochMilliseconds(it.value) }
    )
}
