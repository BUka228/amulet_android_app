package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.model.Emotion
import com.example.amulet.shared.domain.hugs.model.Hug
import com.example.amulet.shared.domain.hugs.model.HugId
import com.example.amulet.shared.domain.hugs.model.HugStatus
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.user.model.UserId
import kotlinx.coroutines.flow.Flow

interface HugsRepository {

    suspend fun sendHug(
        pairId: PairId?,
        fromUserId: UserId,
        toUserId: UserId?,
        emotion: Emotion,
        payload: Map<String, Any?>? = null
    ): AppResult<Unit>

    fun observeHugsForPair(pairId: PairId): Flow<List<Hug>>

    fun observeHugsForUser(userId: UserId): Flow<List<Hug>>

    suspend fun updateHugStatus(hugId: HugId, status: HugStatus): AppResult<Unit>

    suspend fun getHugById(hugId: HugId): AppResult<Hug>

    /**
     * Синхронизировать историю «объятий» с сервером для заданного направления.
     * direction: "sent" / "received" / "all" – в зависимости от API-контракта.
     */
    suspend fun syncHugs(
        direction: String,
        cursor: String? = null,
        limit: Int? = null
    ): AppResult<Unit>
}
