package com.example.amulet.data.user.mapper

import com.example.amulet.core.network.dto.user.UserDto
import com.example.amulet.shared.domain.privacy.model.UserConsents
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalTime::class)
class UserDtoMapperTest {

    private lateinit var mapper: UserDtoMapper
    private lateinit var json: Json

    @Before
    fun setUp() {
        json = Json { ignoreUnknownKeys = true }
        mapper = UserDtoMapper(json)
    }

    @Test
    fun `toDomain maps UserDto with consents correctly`() {
        // Реальные данные из логов
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

        val result = mapper.toDomain(userDto)

        assertNotNull(result)
        assertEquals("9e5ff58a-1659-47d5-b846-b15d6099f8ea", result.id.value)
        assertEquals("UTC", result.timezone)
        assertEquals("en", result.language)
        
        assertNotNull(result.consents)
        assertEquals(false, result.consents?.analytics)
        assertEquals(false, result.consents?.marketing)
        assertEquals(true, result.consents?.notifications)
        assertNotNull(result.consents?.updatedAt)
    }

    @Test
    fun `toDomain handles null consents`() {
        val userDto = UserDto(
            id = "test-user",
            displayName = "Test User",
            consents = null
        )

        val result = mapper.toDomain(userDto)

        assertNotNull(result)
        assertEquals("test-user", result.id.value)
        assertEquals("Test User", result.displayName)
        assertEquals(null, result.consents)
    }

    @Test
    fun `toDomain handles empty consents object`() {
        val consentsJson = buildJsonObject { }

        val userDto = UserDto(
            id = "test-user",
            consents = consentsJson
        )

        val result = mapper.toDomain(userDto)

        assertNotNull(result)
        assertNotNull(result.consents)
        assertEquals(false, result.consents?.analytics)
        assertEquals(false, result.consents?.marketing)
        assertEquals(false, result.consents?.notifications)
        assertEquals(null, result.consents?.updatedAt)
    }

    @Test
    fun `toDomain parses ISO8601 timestamp correctly`() {
        val consentsJson = buildJsonObject {
            put("analytics", true)
            put("marketing", true)
            put("notifications", true)
            put("updatedAt", "2025-10-16T15:35:50.306508+00:00")
        }

        val userDto = UserDto(
            id = "test-user",
            consents = consentsJson
        )

        val result = mapper.toDomain(userDto)

        assertNotNull(result.consents)
        assertNotNull(result.consents?.updatedAt)
        
        val expectedInstant = Instant.parse("2025-10-16T15:35:50.306508Z")
        assertEquals(expectedInstant, result.consents?.updatedAt)
    }

    @Test
    fun `toEntity converts consents to JSON string correctly`() {
        val consentsJson = buildJsonObject {
            put("analytics", false)
            put("marketing", false)
            put("notifications", true)
            put("updatedAt", "2025-10-16T15:35:50.306508+00:00")
        }

        val userDto = UserDto(
            id = "test-user",
            displayName = "Test",
            consents = consentsJson
        )

        val result = mapper.toEntity(userDto)

        assertNotNull(result)
        assertEquals("test-user", result.id)
        assertEquals("Test", result.displayName)
        assertNotNull(result.consentsJson)
        
        // Проверяем, что JSON валидный
        val parsedConsents = json.decodeFromString<Map<String, String?>>(result.consentsJson)
        assertEquals("false", parsedConsents["analytics"])
        assertEquals("false", parsedConsents["marketing"])
        assertEquals("true", parsedConsents["notifications"])
    }

    @Test
    fun `UserConsents roundtrip through JSON preserves data`() {
        val original = UserConsents(
            analytics = true,
            marketing = false,
            notifications = true,
            updatedAt = Instant.parse("2025-10-16T15:35:50.306508Z")
        )

        // Конвертируем в JSON через toEntity
        val consentsJson = buildJsonObject {
            put("analytics", original.analytics)
            put("marketing", original.marketing)
            put("notifications", original.notifications)
            put("updatedAt", original.updatedAt.toString())
        }

        val userDto = UserDto(
            id = "test",
            consents = consentsJson
        )

        val result = mapper.toDomain(userDto)

        assertNotNull(result.consents)
        assertEquals(original.analytics, result.consents?.analytics)
        assertEquals(original.marketing, result.consents?.marketing)
        assertEquals(original.notifications, result.consents?.notifications)
        assertEquals(original.updatedAt, result.consents?.updatedAt)
    }
}
