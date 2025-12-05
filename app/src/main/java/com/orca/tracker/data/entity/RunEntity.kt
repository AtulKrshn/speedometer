package com.orca.tracker.data.entity


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "runs")
data class RunEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val startTime: Long,
    val endTime: Long = 0L,
    val duration: Long = 0L,
    val distanceMeters: Double = 0.0,
    val avgSpeed: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val encodedPolyline: String = "",
    val mapThumbnailPath: String? = null,
    val notes: String? = null,
    val isActive: Boolean = false
)