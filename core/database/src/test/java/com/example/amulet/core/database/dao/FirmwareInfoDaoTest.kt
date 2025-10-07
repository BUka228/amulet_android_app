package com.example.amulet.core.database.dao

import com.example.amulet.core.database.BaseDatabaseTest
import com.example.amulet.core.database.entity.FirmwareInfoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FirmwareInfoDaoTest : BaseDatabaseTest() {

    private lateinit var firmwareInfoDao: FirmwareInfoDao

    @Before
    fun prepare() {
        firmwareInfoDao = database.firmwareInfoDao()
    }

    @Test
    fun `observe by hardware version`() = runTest {
        val info = FirmwareInfoEntity(
            id = "fw",
            hardwareVersion = 1,
            versionName = "1.0.0",
            versionCode = 100,
            downloadUrl = "https://example.com",
            changelog = null,
            mandatory = false,
            cachedAt = 10L
        )
        firmwareInfoDao.upsert(info)

        val observed = firmwareInfoDao.observeByHardwareVersion(1).first()
        assertEquals(listOf(info), observed)
    }

    @Test
    fun `cleanup removes old entries`() = runTest {
        val old = FirmwareInfoEntity("old", 1, "1.0.0", 100, "url", null, false, 0L)
        val fresh = FirmwareInfoEntity("fresh", 1, "1.1.0", 110, "url", null, false, 100L)
        firmwareInfoDao.upsert(listOf(old, fresh))

        val removed = firmwareInfoDao.cleanup(10L)
        assertEquals(1, removed)
        val observed = firmwareInfoDao.observeByHardwareVersion(1).first()
        assertEquals(listOf(fresh), observed)
    }
}
