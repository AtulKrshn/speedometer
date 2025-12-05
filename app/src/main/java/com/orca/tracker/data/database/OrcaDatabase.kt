package com.orca.tracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.orca.tracker.data.dao.*
import com.orca.tracker.data.entity.*

@Database(
    entities = [RunEntity::class, LocationPointEntity::class, AppSettingsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class OrcaDatabase : RoomDatabase() {
    abstract fun runDao(): RunDao
    abstract fun locationPointDao(): LocationPointDao
    abstract fun settingsDao(): SettingsDao
}