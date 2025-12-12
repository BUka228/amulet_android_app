package com.example.amulet.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.amulet.core.database.entity.PairEntity
import com.example.amulet.core.database.entity.PairMemberEntity
import com.example.amulet.core.database.entity.PairEmotionEntity
import com.example.amulet.core.database.entity.PairQuickReplyEntity
import com.example.amulet.core.database.relation.PairWithMembers
import com.example.amulet.core.database.relation.PairWithMemberSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface PairDao {

    @Transaction
    @Query("SELECT * FROM pairs WHERE id = :pairId")
    fun observeById(pairId: String): Flow<PairWithMembers?>

    @Transaction
    @Query("SELECT * FROM pairs ORDER BY createdAt DESC")
    fun observeAllWithSettings(): Flow<List<PairWithMemberSettings>>

    @Transaction
    @Query("SELECT * FROM pairs WHERE id = :pairId")
    fun observePairWithSettings(pairId: String): Flow<PairWithMemberSettings?>

    @Transaction
    @Query("SELECT * FROM pairs ORDER BY createdAt DESC")
    fun pagingAll(): PagingSource<Int, PairWithMembers>

    @Transaction
    @Query("SELECT * FROM pairs WHERE status = :status ORDER BY createdAt DESC")
    fun pagingByStatus(status: String): PagingSource<Int, PairWithMembers>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPair(pair: PairEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<PairMemberEntity>)

    @Query(
        "UPDATE pair_members SET muted = :muted, quietHoursStartMinutes = :quietStart, " +
            "quietHoursEndMinutes = :quietEnd, maxHugsPerHour = :maxHugsPerHour " +
            "WHERE pairId = :pairId AND userId = :userId"
    )
    suspend fun updateMemberSettings(
        pairId: String,
        userId: String,
        muted: Boolean,
        quietStart: Int?,
        quietEnd: Int?,
        maxHugsPerHour: Int?
    )

    @Query("DELETE FROM pair_members WHERE pairId = :pairId")
    suspend fun deleteMembers(pairId: String)

    @Query("DELETE FROM pairs WHERE id = :pairId")
    suspend fun deletePair(pairId: String)

    @Query("DELETE FROM pair_members")
    suspend fun deleteAllMembers()

    @Query("DELETE FROM pairs")
    suspend fun deleteAllPairs()

    // Palette (pair_emotions)

    @Query("SELECT * FROM pair_emotions WHERE pairId = :pairId ORDER BY `order` ASC")
    fun observeEmotions(pairId: String): Flow<List<PairEmotionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEmotions(entities: List<PairEmotionEntity>)

    @Query("DELETE FROM pair_emotions WHERE pairId = :pairId")
    suspend fun deleteEmotions(pairId: String)

    // Quick replies (pair_quick_replies)

    @Query("SELECT * FROM pair_quick_replies WHERE pairId = :pairId AND userId = :userId")
    fun observeQuickReplies(pairId: String, userId: String): Flow<List<PairQuickReplyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuickReplies(entities: List<PairQuickReplyEntity>)

    @Query("DELETE FROM pair_quick_replies WHERE pairId = :pairId AND userId = :userId")
    suspend fun deleteQuickReplies(pairId: String, userId: String)

    @Transaction
    suspend fun upsertPairWithMembers(pair: PairEntity, members: List<PairMemberEntity>) {
        upsertPair(pair)
        deleteMembers(pair.id)
        if (members.isNotEmpty()) {
            insertMembers(members)
        }
    }
}
