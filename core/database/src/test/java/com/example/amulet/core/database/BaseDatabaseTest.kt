package com.example.amulet.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before

abstract class BaseDatabaseTest {

    protected lateinit var database: AmuletDatabase

    @Before
    fun setUpDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AmuletDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDownDatabase() {
        if (this::database.isInitialized) {
            database.close()
        }
    }
}
