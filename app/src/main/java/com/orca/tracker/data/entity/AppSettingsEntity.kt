package com.orca.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val useMetricUnits: Boolean = true,
    val accuracyMode: String = "HIGH", // HIGH, BALANCED, BATTERY_SAVER
    val autoPauseEnabled: Boolean = false,
    val keepScreenOn: Boolean = true,
    val mapStyle: String = "NORMAL", // NORMAL, DARK, SATELLITE
    val polylineColor: String = "#2196F3",
    val dataRetentionDays: Int = 90
)

