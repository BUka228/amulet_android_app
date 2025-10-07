package com.example.amulet.core.database

import androidx.room.TypeConverter
import com.example.amulet.core.database.entity.DeviceStatus
import com.example.amulet.core.database.entity.OutboxActionStatus
import com.example.amulet.core.database.entity.OutboxActionType
import com.example.amulet.core.database.entity.RemoteKeyPartition

internal object DatabaseTypeConverters {

    @TypeConverter
    @JvmStatic
    fun toOutboxActionType(value: String?): OutboxActionType? = value?.let(OutboxActionType::valueOf)

    @TypeConverter
    @JvmStatic
    fun fromOutboxActionType(value: OutboxActionType?): String? = value?.name

    @TypeConverter
    @JvmStatic
    fun toOutboxActionStatus(value: String?): OutboxActionStatus? = value?.let(OutboxActionStatus::valueOf)

    @TypeConverter
    @JvmStatic
    fun fromOutboxActionStatus(value: OutboxActionStatus?): String? = value?.name

    @TypeConverter
    @JvmStatic
    fun toDeviceStatus(value: String?): DeviceStatus? = value?.let(DeviceStatus::fromValue)

    @TypeConverter
    @JvmStatic
    fun fromDeviceStatus(value: DeviceStatus?): String? = value?.value

    @TypeConverter
    @JvmStatic
    fun toRemoteKeyPartition(value: String?): RemoteKeyPartition? = value?.let(RemoteKeyPartition::fromValue)

    @TypeConverter
    @JvmStatic
    fun fromRemoteKeyPartition(value: RemoteKeyPartition?): String? = value?.value
}
