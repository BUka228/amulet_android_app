package com.example.amulet.data.user.mapper

import com.example.amulet.core.database.entity.UserEntity
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class UserEntityMapperTest {

    private lateinit var mapper: UserEntityMapper
    private lateinit var json: Json

    @Before
    fun setUp() {
        json = Json { ignoreUnknownKeys = true }
        mapper = UserEntityMapper(json)
    }

    @Test
    fun `toDomain maps UserEntity with valid consents JSON`() {
        val consentsJson = """
            {
                "analytics": "false",
                "marketing": "false",
                "notifications": "true",
                "updatedAt": "2025-10-16T15:35:50.306508Z"
            }
        """.trimIndent()

        val entity = UserEntity(
            id = "test-user",
            displayName = "Test User",
            avatarUrl = null,
            timezone = "UTC",
            language = "en",
            consentsJson = consentsJson,
            createdAt = 1697462400000L,
            updatedAt = 1697462400000L
        )

        val result = mapper.toDomain(entity)

        assertNotNull(result)
        assertEquals("test-user", result.id.value)
        assertEquals("Test User", result.displayName)
        
        assertNotNull(result.consents)
        assertEquals(false, result.consents?.analytics)
        assertEquals(false, result.consents?.marketing)
        assertEquals(true, result.consents?.notifications)
        assertNotNull(result.consents?.updatedAt)
    }

    @Test
    fun `toDomain handles empty consents JSON`() {
        val entity = UserEntity(
            id = "test-user",
            displayName = "Test",
            avatarUrl = null,
            timezone = null,
            language = null,
            consentsJson = "{}",
            createdAt = null,
            updatedAt = null
        )

        val result = mapper.toDomain(entity)

        assertNotNull(result)
        // Empty JSON should return null consents
        assertNull(result.consents)
    }

    @Test
    fun `toDomain handles malformed consents JSON gracefully`() {
        val entity = UserEntity(
            id = "test-user",
            displayName = "Test",
            avatarUrl = null,
            timezone = null,
            language = null,
            consentsJson = "invalid json",
            createdAt = null,
            updatedAt = null
        )

        val result = mapper.toDomain(entity)

        assertNotNull(result)
        assertNull(result.consents) // Should return null on parse error
    }

    @Test
    fun `toDomain converts timestamps correctly`() {
        val entity = UserEntity(
            id = "test-user",
            displayName = "Test",
            avatarUrl = null,
            timezone = null,
            language = null,
            consentsJson = "{}",
            createdAt = 1697462400000L,
            updatedAt = 1697548800000L
        )

        val result = mapper.toDomain(entity)

        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
        assertEquals(Instant.fromEpochMilliseconds(1697462400000L), result.createdAt)
        assertEquals(Instant.fromEpochMilliseconds(1697548800000L), result.updatedAt)
    }
}
