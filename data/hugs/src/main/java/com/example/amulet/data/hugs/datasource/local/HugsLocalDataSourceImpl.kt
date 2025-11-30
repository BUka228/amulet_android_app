package com.example.amulet.data.hugs.datasource.local

import com.example.amulet.core.database.dao.HugDao
import com.example.amulet.core.database.entity.HugEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HugsLocalDataSourceImpl @Inject constructor(
    private val hugDao: HugDao
) : HugsLocalDataSource {

    override fun observeById(id: String): Flow<HugEntity?> =
        hugDao.observeById(id)

    override fun observeByPairId(pairId: String): Flow<List<HugEntity>> =
        hugDao.observeByPairId(pairId)

    override fun observeByUserId(userId: String): Flow<List<HugEntity>> =
        hugDao.observeByUserId(userId)

    override suspend fun upsert(entity: HugEntity) {
        hugDao.upsert(entity)
    }

    override suspend fun upsert(entities: List<HugEntity>) {
        hugDao.upsert(entities)
    }

    override suspend fun updateStatus(id: String, status: String) {
        hugDao.updateStatus(id, status)
    }
}
