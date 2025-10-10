package com.example.amulet.data.user.datasource.local

import com.example.amulet.core.database.dao.UserDao
import com.example.amulet.core.database.entity.UserEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface UserLocalDataSource {
    suspend fun upsert(user: UserEntity)
    suspend fun findById(userId: String): UserEntity?
}

@Singleton
class UserLocalDataSourceImpl @Inject constructor(
    private val userDao: UserDao
) : UserLocalDataSource {

    override suspend fun upsert(user: UserEntity) {
        withContext(Dispatchers.IO) {
            userDao.upsert(user)
        }
    }

    override suspend fun findById(userId: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getById(userId)
    }
}
