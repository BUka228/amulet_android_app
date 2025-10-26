package com.example.amulet.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.amulet.core.database.dao.DeviceDao
import com.example.amulet.core.database.dao.FirmwareInfoDao
import com.example.amulet.core.database.dao.HugDao
import com.example.amulet.core.database.dao.OutboxActionDao
import com.example.amulet.core.database.dao.PairDao
import com.example.amulet.core.database.dao.PatternDao
import com.example.amulet.core.database.dao.PracticeDao
import com.example.amulet.core.database.dao.PrivacyJobDao
import com.example.amulet.core.database.dao.RemoteKeyDao
import com.example.amulet.core.database.dao.RuleDao
import com.example.amulet.core.database.dao.TelemetryDao
import com.example.amulet.core.database.dao.UserDao
import com.example.amulet.core.database.entity.DeviceEntity
import com.example.amulet.core.database.entity.FirmwareInfoEntity
import com.example.amulet.core.database.entity.HugEntity
import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.PairEntity
import com.example.amulet.core.database.entity.PairMemberEntity
import com.example.amulet.core.database.entity.PatternEntity
import com.example.amulet.core.database.entity.PatternShareEntity
import com.example.amulet.core.database.entity.PatternTagCrossRef
import com.example.amulet.core.database.entity.PracticeEntity
import com.example.amulet.core.database.entity.PracticeSessionEntity
import com.example.amulet.core.database.entity.PrivacyJobEntity
import com.example.amulet.core.database.entity.RemoteKeyEntity
import com.example.amulet.core.database.entity.RuleEntity
import com.example.amulet.core.database.entity.TagEntity
import com.example.amulet.core.database.entity.TelemetryEventEntity
import com.example.amulet.core.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        DeviceEntity::class,
        HugEntity::class,
        PatternEntity::class,
        TagEntity::class,
        PatternTagCrossRef::class,
        PatternShareEntity::class,
        PairEntity::class,
        PairMemberEntity::class,
        PracticeEntity::class,
        PracticeSessionEntity::class,
        RuleEntity::class,
        TelemetryEventEntity::class,
        PrivacyJobEntity::class,
        FirmwareInfoEntity::class,
        OutboxActionEntity::class,
        RemoteKeyEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class AmuletDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun deviceDao(): DeviceDao
    abstract fun hugDao(): HugDao
    abstract fun patternDao(): PatternDao
    abstract fun pairDao(): PairDao
    abstract fun practiceDao(): PracticeDao
    abstract fun ruleDao(): RuleDao
    abstract fun telemetryDao(): TelemetryDao
    abstract fun privacyJobDao(): PrivacyJobDao
    abstract fun firmwareInfoDao(): FirmwareInfoDao
    abstract fun outboxActionDao(): OutboxActionDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}
