package com.orca.tracker.data.dao


import androidx.room.*
import com.orca.tracker.data.entity.LocationPointEntity

@Dao
interface LocationPointDao {
    @Query("SELECT * FROM location_points WHERE runId = :runId ORDER BY timestamp ASC")
    suspend fun getPointsForRun(runId: String): List<LocationPointEntity>

    @Insert
    suspend fun insertPoint(point: LocationPointEntity)

    @Insert
    suspend fun insertPoints(points: List<LocationPointEntity>)

    @Query("DELETE FROM location_points WHERE runId = :runId")
    suspend fun deletePointsForRun(runId: String)
}