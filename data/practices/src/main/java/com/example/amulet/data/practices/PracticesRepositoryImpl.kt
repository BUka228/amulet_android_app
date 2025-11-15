package com.example.amulet.data.practices

import com.example.amulet.data.practices.datasource.LocalPracticesDataSource
import com.example.amulet.data.practices.datasource.RemotePracticesDataSource
import com.example.amulet.data.practices.mapper.toDomain
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.auth.UserSessionContext
import com.example.amulet.shared.core.auth.UserSessionProvider
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeCategory
import com.example.amulet.shared.domain.practices.model.PracticeFilter
import com.example.amulet.shared.domain.practices.model.PracticeId
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeSessionId
import com.example.amulet.shared.domain.practices.model.PracticeSessionStatus
import com.example.amulet.shared.domain.practices.model.UserPreferences
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PracticesRepositoryImpl @Inject constructor(
    private val local: LocalPracticesDataSource,
    private val remote: RemotePracticesDataSource,
    private val sessionProvider: UserSessionProvider,
    private val json: Json
) : PracticesRepository {

    private val currentUserId: String
        get() = when (val c = sessionProvider.currentContext) {
            is UserSessionContext.LoggedIn -> c.userId.value
            else -> throw IllegalStateException("User not authenticated")
        }

    override fun getPracticesStream(filter: PracticeFilter): Flow<List<Practice>> {
        val base = local.observePractices().map { it.map { e -> e.toDomain() } }
        val filtered = base.map { list ->
            val from = filter.durationFromSec
            val to = filter.durationToSec
            list.filter { p ->
                (filter.type == null || p.type == filter.type) &&
                (filter.categoryId == null || true) &&
                (from == null || (p.durationSec ?: Int.MAX_VALUE) >= from) &&
                (to == null || (p.durationSec ?: 0) <= to)
            }
        }
        return if (filter.onlyFavorites) {
            combine(filtered, local.observeFavoriteIds(currentUserId)) { practices, favIds ->
                practices.filter { it.id in favIds }
            }
        } else filtered
    }

    override fun getPracticeById(id: PracticeId): Flow<Practice?> =
        local.observePracticeById(id).map { it?.toDomain() }

    override fun getCategoriesStream(): Flow<List<PracticeCategory>> =
        local.observeCategories().map { it.map { c -> PracticeCategory(c.id, c.title, c.order) } }

    override fun getFavoritesStream(): Flow<List<Practice>> =
        combine(local.observePractices(), local.observeFavoriteIds(currentUserId)) { entities, favIds ->
            entities.filter { it.id in favIds }.map { it.toDomain() }
        }

    override fun getRecommendationsStream(limit: Int?): Flow<List<Practice>> {
        val prefs = getUserPreferencesStream()
        val recent = local.observeSessionsForUser(currentUserId)
        val all = local.observePractices()
        return combine(all, prefs, recent) { practices, preferences, sessions ->
            val completedIds = sessions.filter { it.completed }.map { it.practiceId }.toSet()
            val prefDur = preferences.preferredDurationsSec
            val scored = practices.map { e ->
                val p = e.toDomain()
                val d = p.durationSec
                val score = (if (d != null && prefDur.any { kotlin.math.abs(it - d) <= 120 }) 2 else 0) +
                    (if (p.id !in completedIds) 1 else 0)
                p to score
            }
            scored.sortedByDescending { it.second }.map { it.first }.let { if (limit != null) it.take(limit) else it }
        }
    }

    override suspend fun search(query: String, filter: PracticeFilter): AppResult<List<Practice>> {
        val list = getPracticesStream(filter).first()
        return Ok(list.filter { it.title.contains(query, true) || (it.description?.contains(query, true) == true) })
    }

    override suspend fun refreshCatalog(): AppResult<Unit> = remote.refreshCatalog()

    override suspend fun setFavorite(practiceId: PracticeId, favorite: Boolean): AppResult<Unit> {
        local.setFavorite(currentUserId, practiceId, favorite)
        return Ok(Unit)
    }

    override fun getActiveSessionStream(): Flow<PracticeSession?> =
        local.observeSessionsForUser(currentUserId).map { list ->
            list.firstOrNull { it.status == PracticeSessionStatus.ACTIVE.name }?.toDomain()
        }

    override fun getSessionsHistoryStream(limit: Int?): Flow<List<PracticeSession>> =
        local.observeSessionsForUser(currentUserId).map { list ->
            val mapped = list.map { it.toDomain() }
            if (limit != null) mapped.take(limit) else mapped
        }

    override suspend fun startPractice(
        practiceId: PracticeId,
        intensity: Double?,
        brightness: Double?
    ): AppResult<PracticeSession> {
        val now = System.currentTimeMillis()
        val session = com.example.amulet.core.database.entity.PracticeSessionEntity(
            id = java.util.UUID.randomUUID().toString(),
            userId = currentUserId,
            practiceId = practiceId,
            deviceId = null,
            status = PracticeSessionStatus.ACTIVE.name,
            startedAt = now,
            completedAt = null,
            durationSec = null,
            completed = false,
            intensity = intensity,
            brightness = brightness
        )
        local.upsertSession(session)
        return Ok(session.toDomain())
    }

    override suspend fun pauseSession(sessionId: PracticeSessionId): AppResult<Unit> {
        val current = local.getSessionById(sessionId) ?: return Err(AppError.NotFound)
        if (current.status != PracticeSessionStatus.ACTIVE.name) return Ok(Unit)
        local.upsertSession(current.copy(status = PracticeSessionStatus.CANCELLED.name))
        return Ok(Unit)
    }

    override suspend fun resumeSession(sessionId: PracticeSessionId): AppResult<Unit> {
        val current = local.getSessionById(sessionId) ?: return Err(AppError.NotFound)
        if (current.status == PracticeSessionStatus.ACTIVE.name) return Ok(Unit)
        local.upsertSession(current.copy(status = PracticeSessionStatus.ACTIVE.name))
        return Ok(Unit)
    }

    override suspend fun stopSession(sessionId: PracticeSessionId, completed: Boolean): AppResult<PracticeSession> {
        val current = local.getSessionById(sessionId) ?: return Err(AppError.NotFound)
        val now = System.currentTimeMillis()
        val updated = current.copy(
            status = if (completed) PracticeSessionStatus.COMPLETED.name else PracticeSessionStatus.CANCELLED.name,
            completedAt = now,
            durationSec = (((now - current.startedAt) / 1000).toInt()).coerceAtLeast(0),
            completed = completed
        )
        local.upsertSession(updated)
        return Ok(updated.toDomain())
    }

    override fun getUserPreferencesStream(): Flow<UserPreferences> =
        local.observePreferences(currentUserId).map { e ->
            val goals = e?.goalsJson?.let { json.parseToJsonElement(it).jsonArray.map { it.jsonPrimitive.content } } ?: emptyList()
            val interests = e?.interestsJson?.let { json.parseToJsonElement(it).jsonArray.map { it.jsonPrimitive.content } } ?: emptyList()
            val durations = e?.preferredDurationsJson
                ?.let { json.parseToJsonElement(it).jsonArray.mapNotNull { je -> je.jsonPrimitive.content.toIntOrNull() } }
                ?: emptyList()
            if (e == null) UserPreferences() else UserPreferences(
                defaultIntensity = e.defaultIntensity,
                defaultBrightness = e.defaultBrightness,
                goals = goals,
                interests = interests,
                preferredDurationsSec = durations
            )
        }

    override suspend fun updateUserPreferences(preferences: UserPreferences): AppResult<Unit> {
        val goalsJson = json.encodeToString(json.encodeToJsonElement(preferences.goals))
        val interestsJson = json.encodeToString(json.encodeToJsonElement(preferences.interests))
        val durationsJson = json.encodeToString(json.encodeToJsonElement(preferences.preferredDurationsSec))
        local.upsertPreferences(
            com.example.amulet.core.database.entity.UserPreferencesEntity(
                userId = currentUserId,
                defaultIntensity = preferences.defaultIntensity,
                defaultBrightness = preferences.defaultBrightness,
                goalsJson = goalsJson,
                interestsJson = interestsJson,
                preferredDurationsJson = durationsJson
            )
        )
        return Ok(Unit)
    }
}
