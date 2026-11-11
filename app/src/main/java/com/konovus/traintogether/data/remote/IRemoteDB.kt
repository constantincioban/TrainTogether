package com.konovus.traintogether.data.remote

import com.konovus.traintogether.data.local.entities.PersonalRecordDB
import com.konovus.traintogether.data.local.entities.UserProfileDB
import com.konovus.traintogether.data.local.entities.WorkoutDB
import com.konovus.traintogether.data.local.entities.WorkoutLogDB
import com.konovus.traintogether.data.utils.GymExercise


interface IGenericRepository {
    suspend fun <T : Any> save(userId: String, collection: CollectionPath, item: T, clazz: Class<T>): Result<T>
    suspend fun <T : Any> update(userId: String, collection: CollectionPath, id: String, updates: Map<String, Any>): Result<Boolean>
    suspend fun <T : Any> delete(userId: String, collection: CollectionPath, id: String): Result<Boolean>
    suspend fun <T : Any> getById(userId: String, collection: CollectionPath, id: String, clazz: Class<T>): Result<T?>
    suspend fun <T : Any> query(userId: String, collection: CollectionPath, clazz: Class<T>, options: QueryOptions = QueryOptions()): Result<List<T>>
}

interface IWorkoutRepository {
    // Workout operations
    suspend fun saveWorkout(userId: String, workout: WorkoutDB): Result<WorkoutDB>
    suspend fun getWorkouts(userId: String, options: QueryOptions = QueryOptions()): Result<List<WorkoutDB>>
    suspend fun getWorkoutById(userId: String, workoutId: Int): Result<WorkoutDB?>
    suspend fun deleteWorkout(userId: String, workoutId: Int): Result<Boolean>

    // WorkoutLog operations
    suspend fun saveWorkoutLog(userId: String, workoutLog: WorkoutLogDB): Result<WorkoutLogDB>
    suspend fun getWorkoutLogs(userId: String, options: QueryOptions = QueryOptions()): Result<List<WorkoutLogDB>>
    suspend fun getWorkoutLogById(userId: String, logId: Int): Result<WorkoutLogDB?>
    suspend fun getLogsByWorkoutId(userId: String, workoutId: Int): Result<List<WorkoutLogDB>>
    suspend fun getRecentLogs(userId: String, limit: Int = 10): Result<List<WorkoutLogDB>>

    // PersonalRecord operations
    suspend fun savePersonalRecord(userId: String, pr: PersonalRecordDB): Result<PersonalRecordDB>
    suspend fun getPersonalRecords(userId: String, options: QueryOptions = QueryOptions()): Result<List<PersonalRecordDB>>
    suspend fun getPRsByExercise(userId: String, exercise: GymExercise): Result<List<PersonalRecordDB>>
    suspend fun deletePersonalRecord(userId: String, prId: Int): Result<Boolean>
    suspend fun getAllTimePRs(userId: String): Result<List<PersonalRecordDB>>

    // UserProfile operations
    suspend fun saveUserProfile(userId: String, profile: UserProfileDB): Result<String>
    suspend fun getUserProfile(userId: String): Result<UserProfileDB?>
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Boolean>
    suspend fun deleteUserProfile(userId: String): Result<Boolean>
}


sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "Unknown error"
    ) : Result<Nothing>()

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (Throwable) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }

    fun getOrNull(): T? = (this as? Success)?.data
}

// Query options
data class QueryOptions(
    val limit: Int? = null,
    val orderBy: String? = null,
    val orderDirection: OrderDirection = OrderDirection.DESCENDING,
    val filters: Map<String, Any> = emptyMap(),
    val rangeFilters: Map<String, Pair<Any?, Any?>> = emptyMap()
)

enum class OrderDirection { ASCENDING, DESCENDING }

enum class CollectionPath(val path: String) {
    WORKOUTS("workouts"),
    WORKOUT_LOGS("workout_logs"),
    PERSONAL_RECORDS("personal_records"),
    USERS("users"),
    USER_PROFILES("user_profiles");

    fun userPath(userId: String) = "users/$userId/$path"
}
