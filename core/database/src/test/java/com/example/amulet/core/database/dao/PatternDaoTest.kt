package com.example.amulet.core.database.dao

import androidx.paging.PagingSource
import com.example.amulet.core.database.BaseDatabaseTest
import com.example.amulet.core.database.entity.PatternEntity
import com.example.amulet.core.database.entity.TagEntity
import com.example.amulet.core.database.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PatternDaoTest : BaseDatabaseTest() {

    private lateinit var patternDao: PatternDao
    private lateinit var userDao: UserDao

    @Before
    fun prepare() {
        patternDao = database.patternDao()
        userDao = database.userDao()
    }

    @Test
    fun `upsert pattern with tags and shares`() = runTest {
        val owner = UserEntity("owner", "Owner", null, null, null, "{}", null, null)
        val recipient = UserEntity("recipient", "Recipient", null, null, null, "{}", null, null)
        userDao.upsert(listOf(owner, recipient))

        val pattern = PatternEntity(
            id = "pattern-1",
            ownerId = owner.id,
            kind = "light",
            hardwareVersion = 100,
            title = "Relax",
            description = "",
            specJson = "{}",
            public = false,
            reviewStatus = null,
            usageCount = 1,
            createdAt = 1L,
            updatedAt = 2L
        )

        val tags = listOf(
            TagEntity("tag-1", "calm"),
            TagEntity("tag-2", "sleep")
        )

        patternDao.upsertPatternWithRelations(
            pattern = pattern,
            tags = tags,
            tagIds = tags.map(TagEntity::id),
            sharedUserIds = listOf(recipient.id)
        )

        val stored = patternDao.observeByIdWithRelations(pattern.id).first()
        assertEquals(pattern, stored?.pattern)
        assertEquals(tags.toSet(), stored?.tags?.toSet())
        assertEquals(listOf(recipient), stored?.sharedWith)
    }

    @Test
    fun `paging public patterns sorted by createdAt`() = runTest {
        val patterns = (1..3).map {
            PatternEntity(
                id = "public-$it",
                ownerId = null,
                kind = "light",
                hardwareVersion = 100,
                title = "Pattern $it",
                description = null,
                specJson = "{}",
                public = true,
                reviewStatus = null,
                usageCount = null,
                createdAt = it.toLong(),
                updatedAt = it.toLong()
            )
        }
        patternDao.upsertPatterns(patterns)

        val pagingSource = patternDao.pagingPublic()
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        val expectedOrder = patterns.sortedByDescending { it.createdAt }
        val loadedPatterns = page.data.map { it.pattern }
        assertEquals(expectedOrder, loadedPatterns)
    }
}
