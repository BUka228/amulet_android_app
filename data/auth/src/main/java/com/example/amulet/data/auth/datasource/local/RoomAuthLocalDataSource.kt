package com.example.amulet.data.auth.datasource.local

import com.example.amulet.core.database.AmuletDatabase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class RoomAuthLocalDataSource @Inject constructor(
    private val database: AmuletDatabase
) : AuthLocalDataSource {

    override suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }
}
