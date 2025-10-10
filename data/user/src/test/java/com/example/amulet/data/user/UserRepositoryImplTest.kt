package com.example.amulet.data.user

import com.example.amulet.core.database.entity.UserEntity
import com.example.amulet.core.network.dto.user.UserDto
import com.example.amulet.data.user.datasource.local.UserLocalDataSource
import com.example.amulet.data.user.datasource.remote.UserRemoteDataSource
import com.example.amulet.data.user.mapper.UserDtoMapper
import com.example.amulet.data.user.mapper.UserEntityMapper
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.user.model.UserId
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class UserRepositoryImplTest {

    @MockK
    private lateinit var remoteDataSource: UserRemoteDataSource

    @MockK(relaxed = true)
    private lateinit var localDataSource: UserLocalDataSource

    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        val json = Json { ignoreUnknownKeys = true }
        val dtoMapper = UserDtoMapper(json)
        val entityMapper = UserEntityMapper(json)
        repository = UserRepositoryImpl(remoteDataSource, localDataSource, dtoMapper, entityMapper)
    }

    @Test
    fun `fetchProfile returns remote user and caches it`() = runTest {
        val userDto = UserDto(id = "user-1", displayName = "User")
        coEvery { remoteDataSource.fetchCurrentUser() } returns Ok(userDto)

        val result = repository.fetchProfile(UserId("user-1"))

        assertEquals(UserId("user-1"), result.component1()?.id)
        assertNull(result.component2())
        coVerify(exactly = 1) { remoteDataSource.fetchCurrentUser() }
        confirmVerified(remoteDataSource)
    }

    @Test
    fun `fetchProfile returns cached user when remote fails`() = runTest {
        coEvery { remoteDataSource.fetchCurrentUser() } returns Err(AppError.Network)
        coEvery { localDataSource.findById("user-1") } returns UserEntity(
            id = "user-1",
            displayName = "Cached",
            avatarUrl = null,
            timezone = null,
            language = null,
            consentsJson = "{}",
            createdAt = null,
            updatedAt = null
        )

        val result = repository.fetchProfile(UserId("user-1"))

        assertEquals("Cached", result.component1()?.displayName)
        assertNull(result.component2())
    }

    @Test
    fun `fetchProfile propagates error when cache empty`() = runTest {
        coEvery { remoteDataSource.fetchCurrentUser() } returns Err(AppError.Timeout)
        coEvery { localDataSource.findById("user-1") } returns null

        val result = repository.fetchProfile(UserId("user-1"))

        assertNull(result.component1())
        assertEquals(AppError.Timeout, result.component2())
    }
}
