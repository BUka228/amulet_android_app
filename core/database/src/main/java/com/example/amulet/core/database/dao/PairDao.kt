package com.example.amulet.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.amulet.core.database.entity.PairEntity
import com.example.amulet.core.database.entity.PairMemberEntity
import com.example.amulet.core.database.relation.PairWithMembers
import kotlinx.coroutines.flow.Flow

@Dao
interface PairDao {

    @Transaction
    @Query("SELECT * FROM pairs WHERE id = :pairId")
    fun observeById(pairId: String): Flow<PairWithMembers?>

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

    @Query("DELETE FROM pair_members WHERE pairId = :pairId")
    suspend fun deleteMembers(pairId: String)

    @Query("DELETE FROM pairs WHERE id = :pairId")
    suspend fun deletePair(pairId: String)

    @Transaction
    suspend fun upsertPairWithMembers(pair: PairEntity, members: List<PairMemberEntity>) {
        upsertPair(pair)
        deleteMembers(pair.id)
        if (members.isNotEmpty()) {
            insertMembers(members)
        }
    }
}
