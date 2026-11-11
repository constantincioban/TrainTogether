package com.konovus.traintogether.data.local.db

import com.konovus.traintogether.data.local.entities.PersonalRecordDB
import com.konovus.traintogether.data.local.entities.UserProfileDB
import com.konovus.traintogether.data.local.entities.WorkoutDB
import com.konovus.traintogether.data.local.entities.WorkoutLogDB
import com.konovus.traintogether.data.utils.GymExercise
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDBRepository @Inject constructor(
    private val database: TrainTogetherDatabase
) : ILocalDBRepository {

    private val workoutDao = database.workoutDao()
    private val workoutLogDao = database.workoutLogDao()
    private val personalRecordDao = database.personalRecordDao()
    private val userProfileDao = database.userProfileDao()

    // =================================
    // WORKOUTS
    // =================================

    override fun getAllWorkouts(): Flow<List<WorkoutDB>> =
        workoutDao.getAllWorkouts()

    override suspend fun getWorkoutById(workoutId: Int): WorkoutDB? =
        workoutDao.getWorkoutById(workoutId)

    override fun getRecentWorkouts(limit: Int): Flow<List<WorkoutDB>> =
        workoutDao.getRecentWorkouts(limit)

    override suspend fun insertWorkout(workout: WorkoutDB): Long =
        workoutDao.insertWorkout(workout)

    override suspend fun insertWorkouts(workouts: List<WorkoutDB>) =
        workoutDao.insertWorkouts(workouts)

    override suspend fun updateWorkout(workout: WorkoutDB) =
        workoutDao.updateWorkout(workout)

    override suspend fun deleteWorkout(workout: WorkoutDB) =
        workoutDao.deleteWorkout(workout)

    override suspend fun deleteWorkoutById(workoutId: Int) =
        workoutDao.deleteWorkoutById(workoutId)

    override suspend fun deleteAllWorkouts() =
        workoutDao.deleteAllWorkouts()

    override suspend fun getWorkoutCount(): Int =
        workoutDao.getWorkoutCount()

    // =================================
    // WORKOUT LOGS
    // =================================

    override fun getAllWorkoutLogs(): Flow<List<WorkoutLogDB>> =
        workoutLogDao.getAllWorkoutLogs()

    override suspend fun getWorkoutLogById(logId: Int): WorkoutLogDB? =
        workoutLogDao.getWorkoutLogById(logId)

    override fun getLogsByWorkoutId(workoutId: Int): Flow<List<WorkoutLogDB>> =
        workoutLogDao.getLogsByWorkoutId(workoutId)

    override fun getRecentLogs(limit: Int): Flow<List<WorkoutLogDB>> =
        workoutLogDao.getRecentLogs(limit)

    override fun getLogsByDateRange(startDate: Long, endDate: Long): Flow<List<WorkoutLogDB>> =
        workoutLogDao.getLogsByDateRange(startDate, endDate)

    override suspend fun insertWorkoutLog(log: WorkoutLogDB): Long =
        workoutLogDao.insertWorkoutLog(log)

    override suspend fun insertWorkoutLogs(logs: List<WorkoutLogDB>) =
        workoutLogDao.insertWorkoutLogs(logs)

    override suspend fun updateWorkoutLog(log: WorkoutLogDB) =
        workoutLogDao.updateWorkoutLog(log)

    override suspend fun deleteWorkoutLog(log: WorkoutLogDB) =
        workoutLogDao.deleteWorkoutLog(log)

    override suspend fun deleteWorkoutLogById(logId: Int) =
        workoutLogDao.deleteWorkoutLogById(logId)

    override suspend fun deleteAllWorkoutLogs() =
        workoutLogDao.deleteAllWorkoutLogs()

    override suspend fun getWorkoutLogCount(): Int =
        workoutLogDao.getWorkoutLogCount()

    // =================================
    // PERSONAL RECORDS
    // =================================

    override fun getAllPersonalRecords(): Flow<List<PersonalRecordDB>> =
        personalRecordDao.getAllPersonalRecords()

    override suspend fun getPersonalRecordById(recordId: Int): PersonalRecordDB? =
        personalRecordDao.getPersonalRecordById(recordId)

    override fun getRecordsByExercise(exercise: GymExercise): Flow<List<PersonalRecordDB>> =
        personalRecordDao.getRecordsByExercise(exercise)

    override suspend fun getBestRecordForExercise(exercise: GymExercise): PersonalRecordDB? =
        personalRecordDao.getBestRecordForExercise(exercise)

    override suspend fun insertPersonalRecord(record: PersonalRecordDB): Long =
        personalRecordDao.insertPersonalRecord(record)

    override suspend fun insertPersonalRecords(records: List<PersonalRecordDB>) =
        personalRecordDao.insertPersonalRecords(records)

    override suspend fun updatePersonalRecord(record: PersonalRecordDB) =
        personalRecordDao.updatePersonalRecord(record)

    override suspend fun deletePersonalRecord(record: PersonalRecordDB) =
        personalRecordDao.deletePersonalRecord(record)

    override suspend fun deletePersonalRecordById(recordId: Int) =
        personalRecordDao.deletePersonalRecordById(recordId)

    override suspend fun deleteAllPersonalRecords() =
        personalRecordDao.deleteAllPersonalRecords()

    override suspend fun getPersonalRecordCount(): Int =
        personalRecordDao.getPersonalRecordCount()

    // =================================
    // USER PROFILE
    // =================================

    override fun getUserProfile(): Flow<UserProfileDB?> =
        userProfileDao.getUserProfile()

    override suspend fun getUserProfileOnce(): UserProfileDB? =
        userProfileDao.getUserProfileOnce()

    override suspend fun insertUserProfile(profile: UserProfileDB) =
        userProfileDao.insertUserProfile(profile)

    override suspend fun updateUserProfile(profile: UserProfileDB) =
        userProfileDao.updateUserProfile(profile)

    override suspend fun deleteUserProfile(profile: UserProfileDB) =
        userProfileDao.deleteUserProfile(profile)

    override suspend fun deleteAllUserProfiles() =
        userProfileDao.deleteAllUserProfiles()

    // =================================
    // UTILITY
    // =================================

    override suspend fun clearAllData() {
        deleteAllWorkouts()
        deleteAllWorkoutLogs()
        deleteAllPersonalRecords()
        deleteAllUserProfiles()
    }
}