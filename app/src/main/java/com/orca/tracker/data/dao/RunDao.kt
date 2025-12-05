package com.orca.tracker.data.dao

import androidx.room.*
import com.orca.tracker.data.entity.RunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Query("SELECT * FROM runs WHERE isActive = 0 ORDER BY startTime DESC")
    fun getAllRuns(): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveRun(): RunEntity?

    @Query("SELECT * FROM runs WHERE id = :id")
    suspend fun getRunById(id: String): RunEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: RunEntity)

    @Update
    suspend fun updateRun(run: RunEntity)

    @Delete
    suspend fun deleteRun(run: RunEntity)

    @Query("DELETE FROM runs WHERE startTime < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}