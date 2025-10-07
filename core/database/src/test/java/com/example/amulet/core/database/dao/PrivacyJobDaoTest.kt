package com.example.amulet.core.database.dao

import com.example.amulet.core.database.BaseDatabaseTest
import com.example.amulet.core.database.entity.PrivacyJobEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PrivacyJobDaoTest : BaseDatabaseTest() {

    private lateinit var privacyJobDao: PrivacyJobDao

    @Before
    fun prepare() {
        privacyJobDao = database.privacyJobDao()
    }

    @Test
    fun `observe jobs for user`() = runTest {
        val job = PrivacyJobEntity(
            id = "job",
            userId = "user",
            type = "export",
            status = "pending",
            payloadJson = "{}",
            createdAt = 10L,
            updatedAt = 10L,
            expiresAt = null
        )
        privacyJobDao.upsert(job)

        val jobs = privacyJobDao.observeByUser("user").first()
        assertEquals(listOf(job), jobs)
    }

    @Test
    fun `cleanup removes expired jobs`() = runTest {
        val expired = PrivacyJobEntity("expired", "user", "delete", "pending", "{}", 0L, 0L, 10L)
        val active = PrivacyJobEntity("active", "user", "delete", "pending", "{}", 0L, 0L, 100L)
        privacyJobDao.upsert(listOf(expired, active))

        val removed = privacyJobDao.cleanupExpired(50L)
        assertEquals(1, removed)
        val remaining = privacyJobDao.observeByUser("user").first()
        assertEquals(listOf(active), remaining)
    }
}
