package com.example.amulet.core.database.dao

import com.example.amulet.core.database.BaseDatabaseTest
import com.example.amulet.core.database.entity.DeviceEntity
import com.example.amulet.core.database.entity.DeviceStatus
import com.example.amulet.core.database.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class UserDaoTest : BaseDatabaseTest() {

    private lateinit var userDao: UserDao
    private lateinit var deviceDao: DeviceDao

    @Before
    fun prepare() {
        userDao = database.userDao()
        deviceDao = database.deviceDao()
    }

    @Test
    fun `upsert and observe user`() = runTest {
        val user = UserEntity(
            id = "user-1",
            displayName = "Alice",
            avatarUrl = null,
            timezone = "UTC",
            language = "en",
            consentsJson = "{}",
            createdAt = 1L,
            updatedAt = 2L
        )

        userDao.upsert(user)

        val stored = userDao.observeById(user.id).first()
        assertEquals(user, stored)
    }

    @Test
    fun `relation returns user with devices`() = runTest {
        val user = UserEntity(
            id = "user-2",
            displayName = "Bob",
            avatarUrl = null,
            timezone = null,
            language = null,
            consentsJson = "{}",
            createdAt = null,
            updatedAt = null
        )
        val device = DeviceEntity(
            id = "device-1",
            ownerId = user.id,
            serial = "serial-1",
            hardwareVersion = 100,
            firmwareVersion = "1.0.0",
            name = "Device",
            batteryLevel = 0.9,
            status = DeviceStatus.ONLINE,
            settingsJson = "{}",
            pairedAt = 100L,
            updatedAt = 200L
        )

        userDao.upsert(user)
        deviceDao.upsert(device)

        val result = userDao.observeWithDevices(user.id).first()
        assertNotNull(result)
        assertEquals(user, result?.user)
        assertEquals(listOf(device), result?.devices)
    }
}
