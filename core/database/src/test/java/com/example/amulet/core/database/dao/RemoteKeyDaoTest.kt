package com.example.amulet.core.database.dao

import com.example.amulet.core.database.BaseDatabaseTest
import com.example.amulet.core.database.entity.RemoteKeyEntity
import com.example.amulet.core.database.entity.RemoteKeyPartition
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RemoteKeyDaoTest : BaseDatabaseTest() {

    private lateinit var remoteKeyDao: RemoteKeyDao

    @Before
    fun prepare() {
        remoteKeyDao = database.remoteKeyDao()
    }

    @Test
    fun `upsert and fetch remote key`() = runTest {
        val key = RemoteKeyEntity(
            id = "hugs:sent",
            tableName = "hugs",
            partition = RemoteKeyPartition.SENT,
            nextCursor = "cursor-1",
            prevCursor = null,
            updatedAt = 100L
        )

        remoteKeyDao.upsert(key)

        val stored = remoteKeyDao.get("hugs", RemoteKeyPartition.SENT)
        assertEquals(key, stored)
    }

    @Test
    fun `cleanup removes outdated keys`() = runTest {
        val oldKey = RemoteKeyEntity("patterns:public", "patterns", RemoteKeyPartition.PUBLIC, null, null, 0L)
        val freshKey = RemoteKeyEntity("patterns:mine", "patterns", RemoteKeyPartition.MINE, null, null, 200L)

        remoteKeyDao.upsert(listOf(oldKey, freshKey))

        val removed = remoteKeyDao.cleanupOlderThan(100L)
        assertEquals(1, removed)
        assertNull(remoteKeyDao.get("patterns", RemoteKeyPartition.PUBLIC))
        assertEquals(freshKey, remoteKeyDao.get("patterns", RemoteKeyPartition.MINE))
    }
}
