package com.example.amulet.core.database.dao

import androidx.paging.PagingSource
import com.example.amulet.core.database.BaseDatabaseTest
import com.example.amulet.core.database.entity.HugEntity
import com.example.amulet.core.database.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HugDaoTest : BaseDatabaseTest() {

    private lateinit var hugDao: HugDao
    private lateinit var userDao: UserDao

    @Before
    fun prepare() {
        hugDao = database.hugDao()
        userDao = database.userDao()
    }

    @Test
    fun `paging sent hugs sorted by createdAt desc`() = runTest {
        val user = UserEntity(
            id = "sender",
            displayName = "Sender",
            avatarUrl = null,
            timezone = null,
            language = null,
            consentsJson = "{}",
            createdAt = null,
            updatedAt = null
        )
        userDao.upsert(user)

        val hugs = (1..3).map {
            HugEntity(
                id = "hug-$it",
                fromUserId = user.id,
                toUserId = null,
                pairId = null,
                emotionColor = null,
                emotionPatternId = null,
                payloadJson = null,
                inReplyToHugId = null,
                deliveredAt = null,
                createdAt = it.toLong()
            )
        }
        hugDao.upsert(hugs)

        val pagingSource = hugDao.pagingSent(user.id)
        val loadResult = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(loadResult is PagingSource.LoadResult.Page)
        val page = loadResult as PagingSource.LoadResult.Page
        val expectedOrder = hugs.sortedByDescending { it.createdAt }
        assertEquals(expectedOrder, page.data)
    }

    @Test
    fun `relation returns participants`() = runTest {
        val sender = UserEntity("sender", "Sender", null, null, null, "{}", null, null)
        val receiver = UserEntity("receiver", "Receiver", null, null, null, "{}", null, null)
        userDao.upsert(listOf(sender, receiver))

        val hug = HugEntity(
            id = "hug-rel",
            fromUserId = sender.id,
            toUserId = receiver.id,
            pairId = null,
            emotionColor = "red",
            emotionPatternId = null,
            payloadJson = null,
            inReplyToHugId = null,
            deliveredAt = null,
            createdAt = 10L
        )
        hugDao.upsert(hug)

        val relation = hugDao.observeWithParticipants(hug.id).first()
        assertEquals(sender, relation?.fromUser)
        assertEquals(receiver, relation?.toUser)
    }
}
