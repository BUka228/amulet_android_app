package com.example.amulet.data.auth.datasource.local

import com.example.amulet.core.database.AmuletDatabase
import com.example.amulet.shared.core.logging.Logger
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
            Logger.d("Clearing local auth-related tables", TAG)
            database.clearAllTables()
            Logger.i("Local tables cleared", TAG)
        }
    }
}
