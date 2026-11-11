package com.konovus.traintogether.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.konovus.traintogether.data.local.entities.WorkoutLogDB
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutLogDao {
    @Query("SELECT * FROM workout_logs ORDER BY startedTimestamp DESC")
    fun getAllWorkoutLogs(): Flow<List<WorkoutLogDB>>

    @Query("SELECT * FROM workout_logs WHERE id = :logId")
    suspend fun getWorkoutLogById(logId: Int): WorkoutLogDB?

    @Query("SELECT * FROM workout_logs WHERE workoutId = :workoutId ORDER BY startedTimestamp DESC")
    fun getLogsByWorkoutId(workoutId: Int): Flow<List<WorkoutLogDB>>

    @Query("SELECT * FROM workout_logs ORDER BY startedTimestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<WorkoutLogDB>>

    @Query("SELECT * FROM workout_logs WHERE startedTimestamp >= :startDate AND startedTimestamp <= :endDate ORDER BY startedTimestamp DESC")
    fun getLogsByDateRange(startDate: Long, endDate: Long): Flow<List<WorkoutLogDB>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLogDB): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLogs(logs: List<WorkoutLogDB>)

    @Update
    suspend fun updateWorkoutLog(log: WorkoutLogDB)

    @Delete
    suspend fun deleteWorkoutLog(log: WorkoutLogDB)

    @Query("DELETE FROM workout_logs WHERE id = :logId")
    suspend fun deleteWorkoutLogById(logId: Int)

    @Query("DELETE FROM workout_logs")
    suspend fun deleteAllWorkoutLogs()

    @Query("SELECT COUNT(*) FROM workout_logs")
    suspend fun getWorkoutLogCount(): Int
}
