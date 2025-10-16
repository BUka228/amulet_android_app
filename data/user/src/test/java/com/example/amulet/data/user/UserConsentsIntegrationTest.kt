package com.example.amulet.data.user

import com.example.amulet.core.database.entity.UserEntity
import com.example.amulet.core.network.dto.user.UserDto
import com.example.amulet.data.user.datasource.local.UserLocalDataSource
import com.example.amulet.data.user.datasource.remote.UserRemoteDataSource
import com.example.amulet.data.user.mapper.UserDtoMapper
import com.example.amulet.data.user.mapper.UserEntityMapper
import com.example.amulet.shared.domain.privacy.model.UserConsents
import com.example.amulet.shared.domain.user.model.UserId
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Интеграционный тест для проверки корректности маппинга UserConsents
 * через весь стек: DTO -> Domain -> Entity
 */
class UserConsentsIntegrationTest {

    @MockK
    private lateinit var remoteDataSource: UserRemoteDataSource

    @MockK(relaxed = true)
    private lateinit var localDataSource: UserLocalDataSource

    private lateinit var repository: UserRepositoryImpl
    private lateinit var json: Json

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        json = Json { ignoreUnknownKeys = true }
        val dtoMapper = UserDtoMapper(json)
        val entityMapper = UserEntityMapper(json)
        repository = UserRepositoryImpl(remoteDataSource, localDataSource, dtoMapper, entityMapper)
    }

    @Test
    fun `fetchProfile correctly maps UserConsents from API response`() = runTest {
        // Реальные данные из логов ошибки
        val consentsJson = buildJsonObject {
            put("analytics", false)
            put("marketing", false)
            put("notifications", true)
            put("updatedAt", "2025-10-16T15:35:50.306508+00:00")
        }

        val userDto = UserDto(
            id = "9e5ff58a-1659-47d5-b846-b15d6099f8ea",
            displayName = null,
            avatarUrl = null,
            timezone = "UTC",
            language = "en",
            consents = consentsJson,
            createdAt = null,
            updatedAt = null
        )

        coEvery { remoteDataSource.fetchCurrentUser() } returns Ok(userDto)

        // Захватываем сохраненную entity
        val entitySlot = slot<UserEntity>()
        coEvery { localDataSource.upsert(capture(entitySlot)) } returns Unit

        val result = repository.fetchProfile(UserId("9e5ff58a-1659-47d5-b846-b15d6099f8ea"))

        // Проверяем результат
        assertNotNull(result.component1())
        val user = result.component1()!!
        
        assertNotNull(user.consents)
        assertEquals(false, user.consents?.analytics)
        assertEquals(false, user.consents?.marketing)
        assertEquals(true, user.consents?.notifications)
        assertNotNull(user.consents?.updatedAt)

        // Проверяем, что entity корректно сохранена
        val savedEntity = entitySlot.captured
        assertNotNull(savedEntity.consentsJson)
        
        // Проверяем, что JSON валидный и содержит правильные данные
        val parsedConsents = json.decodeFromString<Map<String, String?>>(savedEntity.consentsJson)
        assertEquals("false", parsedConsents["analytics"])
        assertEquals("false", parsedConsents["marketing"])
        assertEquals("true", parsedConsents["notifications"])
        assertNotNull(parsedConsents["updatedAt"])
    }

    @Test
    fun `UserConsents type consistency check`() {
        // Проверяем, что тип Instant корректный
        val consents = UserConsents(
            analytics = true,
            marketing = false,
            notifications = true,
            updatedAt = Instant.parse("2025-10-16T15:35:50.306508Z")
        )

        assertNotNull(consents.updatedAt)
        
        // Проверяем, что это kotlinx.datetime.Instant
        val instant: Instant = consents.updatedAt!!
        assertEquals("2025-10-16T15:35:50.306508Z", instant.toString())
    }

    @Test
    fun `UserConsents constructor accepts kotlinx datetime Instant`() {
        val instant: Instant = Instant.parse("2025-10-16T15:35:50.306508Z")
        
        // Этот тест должен скомпилироваться без ошибок
        val consents = UserConsents(
            analytics = false,
            marketing = false,
            notifications = true,
            updatedAt = instant
        )

        assertEquals(instant, consents.updatedAt)
    }

    @Test
    fun `Mapper creates UserConsents with correct Instant type`() {
        val mapper = UserDtoMapper(json)
        
        val consentsJson = buildJsonObject {
            put("analytics", false)
            put("marketing", false)
            put("notifications", true)
            put("updatedAt", "2025-10-16T15:35:50.306508+00:00")
        }

        val userDto = UserDto(
            id = "test",
            consents = consentsJson
        )

        val user = mapper.toDomain(userDto)

        assertNotNull(user.consents)
        assertNotNull(user.consents?.updatedAt)
        
        // Проверяем тип
        val instant: Instant = user.consents?.updatedAt!!
        assertNotNull(instant)
    }
}
