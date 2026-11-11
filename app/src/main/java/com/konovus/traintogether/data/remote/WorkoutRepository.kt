package com.konovus.traintogether.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.konovus.traintogether.data.local.entities.PersonalRecordDB
import com.konovus.traintogether.data.local.entities.UserProfileDB
import com.konovus.traintogether.data.local.entities.WorkoutDB
import com.konovus.traintogether.data.local.entities.WorkoutLogDB
import com.konovus.traintogether.data.utils.GymExercise

class WorkoutRepository(
    firestore: FirebaseFirestore
) : BaseFirestoreRepository(firestore), IWorkoutRepository {

    // UserProfile operations
    override suspend fun saveUserProfile(userId: String, profile: UserProfileDB): Result<String> {
        return createUserProfile(userId, profile)
    }

    override suspend fun getUserProfile(userId: String): Result<UserProfileDB?> {
        return getProfile(userId)
    }

    override suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Boolean> {
        return updateProfile(userId, updates)
    }

    override suspend fun deleteUserProfile(userId: String): Result<Boolean> {
        return deleteProfile(userId)
    }

    // Workouts - auto-detects create vs update based on ID
    override suspend fun saveWorkout(userId: String, workout: WorkoutDB): Result<WorkoutDB> {
        return if (workout.id <= 0) {
            create(userId, CollectionPath.WORKOUTS, workout)
        } else {
            update(userId, CollectionPath.WORKOUTS, workout)
        }
    }

    override suspend fun getWorkouts(userId: String, options: QueryOptions) =
        query(userId, CollectionPath.WORKOUTS, WorkoutDB::class.java, options)

    override suspend fun getWorkoutById(userId: String, workoutId: Int) =
        getById(userId, CollectionPath.WORKOUTS, workoutId, WorkoutDB::class.java)

    override suspend fun deleteWorkout(userId: String, workoutId: Int) =
        delete(userId, CollectionPath.WORKOUTS, workoutId)

    // Workout Logs - auto-detects create vs update
    override suspend fun saveWorkoutLog(userId: String, workoutLog: WorkoutLogDB): Result<WorkoutLogDB> {
        return if (workoutLog.id <= 0) {
            create(userId, CollectionPath.WORKOUT_LOGS, workoutLog)
        } else {
            update(userId, CollectionPath.WORKOUT_LOGS, workoutLog)
        }
    }

    override suspend fun getWorkoutLogs(userId: String, options: QueryOptions) =
        query(userId, CollectionPath.WORKOUT_LOGS, WorkoutLogDB::class.java, options)

    override suspend fun getWorkoutLogById(userId: String, logId: Int) =
        getById(userId, CollectionPath.WORKOUT_LOGS, logId, WorkoutLogDB::class.java)

    override suspend fun getLogsByWorkoutId(userId: String, workoutId: Int) =
        query(
            userId,
            CollectionPath.WORKOUT_LOGS,
            WorkoutLogDB::class.java,
            QueryOptions(filters = mapOf("workoutId" to workoutId))
        )

    override suspend fun getRecentLogs(userId: String, limit: Int) =
        query(
            userId,
            CollectionPath.WORKOUT_LOGS,
            WorkoutLogDB::class.java,
            QueryOptions(
                limit = limit,
                orderBy = "startedTimestamp",
                orderDirection = OrderDirection.DESCENDING
            )
        )

    // Personal Records - auto-detects create vs update
    override suspend fun savePersonalRecord(userId: String, pr: PersonalRecordDB): Result<PersonalRecordDB> {
        return if (pr.id <= 0) {
            create(userId, CollectionPath.PERSONAL_RECORDS, pr)
        } else {
            update(userId, CollectionPath.PERSONAL_RECORDS, pr)
        }
    }

    override suspend fun getPersonalRecords(userId: String, options: QueryOptions) =
        query(userId, CollectionPath.PERSONAL_RECORDS, PersonalRecordDB::class.java, options)

    override suspend fun getPRsByExercise(userId: String, exercise: GymExercise) =
        query(
            userId,
            CollectionPath.PERSONAL_RECORDS,
            PersonalRecordDB::class.java,
            QueryOptions(
                filters = mapOf("exercise" to exercise),
                orderBy = "date",
                orderDirection = OrderDirection.DESCENDING
            )
        )

    override suspend fun deletePersonalRecord(userId: String, prId: Int) =
        delete(userId, CollectionPath.PERSONAL_RECORDS, prId)

    override suspend fun getAllTimePRs(userId: String) =
        query(
            userId,
            CollectionPath.PERSONAL_RECORDS,
            PersonalRecordDB::class.java,
            QueryOptions(
                orderBy = "date",
                orderDirection = OrderDirection.ASCENDING
            )
        )
}