package com.example.amulet.core.database.dao

import com.example.amulet.core.database.BaseDatabaseTest
import com.example.amulet.core.database.entity.DeviceEntity
import com.example.amulet.core.database.entity.PracticeEntity
import com.example.amulet.core.database.entity.PracticeSessionEntity
import com.example.amulet.core.database.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PracticeDaoTest : BaseDatabaseTest() {

    private lateinit var practiceDao: PracticeDao
    private lateinit var userDao: UserDao
    private lateinit var deviceDao: DeviceDao

    @Before
    fun prepare() {
        practiceDao = database.practiceDao()
        userDao = database.userDao()
        deviceDao = database.deviceDao()
    }

    @Test
    fun `session relation returns full details`() = runTest {
        val user = UserEntity("user", "User", null, null, null, "{}", null, null)
        val practice = PracticeEntity(
            id = "practice",
            type = "breath",
            title = "Breathing",
            description = null,
            durationSec = 300,
            level = null,
            goal = null,
            tagsJson = "[]",
            contraindicationsJson = "[]",
            patternId = null,
            audioUrl = null,
            usageCount = 0,
            localesJson = "{}",
            createdAt = null,
            updatedAt = null,
            stepsJson = null,
            safetyNotesJson = null,
            scriptJson = null
        )
        val device = DeviceEntity(
            id = "device",
            ownerId = user.id,
            bleAddress = "AA:BB:CC:DD:EE:FF",
            hardwareVersion = 100,
            firmwareVersion = "1.0.0",
            name = "Device",
            batteryLevel = null,
            status = null,
            settingsJson = "{}",
            addedAt = System.currentTimeMillis(),
            lastConnectedAt = null
        )
        val session = PracticeSessionEntity(
            id = "session",
            userId = user.id,
            practiceId = practice.id,
            deviceId = device.id,
            status = "active",
            startedAt = 10L,
            completedAt = null,
            durationSec = null,
            completed = false,
            intensity = null,
            brightness = null
        )

        userDao.upsert(user)
        deviceDao.upsert(device)
        practiceDao.upsertPractice(practice)
        practiceDao.upsertSession(session)

        val loaded = practiceDao.observeSession(session.id).first()
        assertEquals(session, loaded?.session)
        assertEquals(user, loaded?.user)
        assertEquals(practice, loaded?.practice)
        assertEquals(device, loaded?.device)
    }

    @Test
    fun `cleanup removes old completed sessions`() = runTest {
        val user = UserEntity("user-2", "User", null, null, null, "{}", null, null)
        val practice = PracticeEntity(
            id = "practice-2",
            type = "meditation",
            title = "Meditation",
            description = null,
            durationSec = null,
            level = null,
            goal = null,
            tagsJson = "[]",
            contraindicationsJson = "[]",
            patternId = null,
            audioUrl = null,
            usageCount = 0,
            localesJson = "{}",
            createdAt = null,
            updatedAt = null,
            stepsJson = null,
            safetyNotesJson = null,
            scriptJson = null
        )
        val session = PracticeSessionEntity(
            id = "session-old",
            userId = user.id,
            practiceId = practice.id,
            deviceId = null,
            status = "completed",
            startedAt = 0L,
            completedAt = 5L,
            durationSec = 5,
            completed = true,
            intensity = null,
            brightness = null
        )

        userDao.upsert(user)
        practiceDao.upsertPractice(practice)
        practiceDao.upsertSession(session)

        val removed = practiceDao.cleanupCompletedSessions(cutoff = 10L)
        assertEquals(1, removed)
        val remaining = practiceDao.observeSessionsForUser(user.id).first()
        assertEquals(emptyList<PracticeSessionEntity>(), remaining.map { it.session })
    }
}
