package com.konovus.traintogether.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.konovus.traintogether.data.local.entities.WorkoutDB
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY id DESC")
    fun getAllWorkouts(): Flow<List<WorkoutDB>>

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Int): WorkoutDB?

    @Query("SELECT * FROM workouts ORDER BY id DESC LIMIT :limit")
    fun getRecentWorkouts(limit: Int): Flow<List<WorkoutDB>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutDB): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(workouts: List<WorkoutDB>)

    @Update
    suspend fun updateWorkout(workout: WorkoutDB)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutDB)

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkoutById(workoutId: Int)

    @Query("DELETE FROM workouts")
    suspend fun deleteAllWorkouts()

    @Query("SELECT COUNT(*) FROM workouts")
    suspend fun getWorkoutCount(): Int
}