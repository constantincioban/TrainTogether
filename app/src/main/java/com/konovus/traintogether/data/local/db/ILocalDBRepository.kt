package com.konovus.traintogether.data.local.db

import com.konovus.traintogether.data.local.entities.PersonalRecordDB
import com.konovus.traintogether.data.local.entities.UserProfileDB
import com.konovus.traintogether.data.local.entities.WorkoutDB
import com.konovus.traintogether.data.local.entities.WorkoutLogDB
import com.konovus.traintogether.data.utils.GymExercise
import kotlinx.coroutines.flow.Flow

interface ILocalDBRepository {
    // Workouts
    fun getAllWorkouts(): Flow<List<WorkoutDB>>
    suspend fun getWorkoutById(workoutId: Int): WorkoutDB?
    fun getRecentWorkouts(limit: Int): Flow<List<WorkoutDB>>
    suspend fun insertWorkout(workout: WorkoutDB): Long
    suspend fun insertWorkouts(workouts: List<WorkoutDB>)
    suspend fun updateWorkout(workout: WorkoutDB)
    suspend fun deleteWorkout(workout: WorkoutDB)
    suspend fun deleteWorkoutById(workoutId: Int)
    suspend fun deleteAllWorkouts()
    suspend fun getWorkoutCount(): Int

    // Workout Logs
    fun getAllWorkoutLogs(): Flow<List<WorkoutLogDB>>
    suspend fun getWorkoutLogById(logId: Int): WorkoutLogDB?
    fun getLogsByWorkoutId(workoutId: Int): Flow<List<WorkoutLogDB>>
    fun getRecentLogs(limit: Int): Flow<List<WorkoutLogDB>>
    fun getLogsByDateRange(startDate: Long, endDate: Long): Flow<List<WorkoutLogDB>>
    suspend fun insertWorkoutLog(log: WorkoutLogDB): Long
    suspend fun insertWorkoutLogs(logs: List<WorkoutLogDB>)
    suspend fun updateWorkoutLog(log: WorkoutLogDB)
    suspend fun deleteWorkoutLog(log: WorkoutLogDB)
    suspend fun deleteWorkoutLogById(logId: Int)
    suspend fun deleteAllWorkoutLogs()
    suspend fun getWorkoutLogCount(): Int

    // Personal Records
    fun getAllPersonalRecords(): Flow<List<PersonalRecordDB>>
    suspend fun getPersonalRecordById(recordId: Int): PersonalRecordDB?
    fun getRecordsByExercise(exercise: GymExercise): Flow<List<PersonalRecordDB>>
    suspend fun getBestRecordForExercise(exercise: GymExercise): PersonalRecordDB?
    suspend fun insertPersonalRecord(record: PersonalRecordDB): Long
    suspend fun insertPersonalRecords(records: List<PersonalRecordDB>)
    suspend fun updatePersonalRecord(record: PersonalRecordDB)
    suspend fun deletePersonalRecord(record: PersonalRecordDB)
    suspend fun deletePersonalRecordById(recordId: Int)
    suspend fun deleteAllPersonalRecords()
    suspend fun getPersonalRecordCount(): Int

    // User Profile
    fun getUserProfile(): Flow<UserProfileDB?>
    suspend fun getUserProfileOnce(): UserProfileDB?
    suspend fun insertUserProfile(profile: UserProfileDB)
    suspend fun updateUserProfile(profile: UserProfileDB)
    suspend fun deleteUserProfile(profile: UserProfileDB)
    suspend fun deleteAllUserProfiles()

    // Utility
    suspend fun clearAllData()
}
