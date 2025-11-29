package com.example.amulet.core.database.converter

import androidx.room.TypeConverter
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal object InstantConverters {

    @OptIn(ExperimentalTime::class)
    @TypeConverter
    @JvmStatic
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilliseconds()

    @OptIn(ExperimentalTime::class)
    @TypeConverter
    @JvmStatic
    fun toInstant(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }
}
