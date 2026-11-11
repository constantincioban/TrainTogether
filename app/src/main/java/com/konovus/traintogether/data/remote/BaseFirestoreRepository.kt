package com.konovus.traintogether.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.konovus.traintogether.data.local.entities.PersonalRecordDB
import com.konovus.traintogether.data.local.entities.UserProfileDB
import com.konovus.traintogether.data.local.entities.WorkoutDB
import com.konovus.traintogether.data.local.entities.WorkoutLogDB
import kotlinx.coroutines.tasks.await
import kotlin.math.absoluteValue

abstract class BaseFirestoreRepository(
    protected val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "FirestoreRepo"
    }



    // CREATE - generates ID if 0 or negative
    protected suspend fun <T : Any> create(
        userId: String,
        collection: CollectionPath,
        item: T
    ): Result<T> = try {
        require(userId.isNotBlank()) { "User ID required" }

        val id = getItemId(item).takeIf { it > 0 } ?: generateIntId()
        val now = System.currentTimeMillis()

        firestore.collection(collection.userPath(userId))
            .document(id.toString())
            .set(item)
            .await()

        Log.d(TAG, "Created ${collection.path}/$id")
        Result.Success(item)
    } catch (e: Exception) {
        Log.e(TAG, "Error creating item", e)
        Result.Error(e)
    }

    // UPDATE - requires existing ID > 0
    protected suspend fun <T : Any> update(
        userId: String,
        collection: CollectionPath,
        item: T
    ): Result<T> = try {
        require(userId.isNotBlank()) { "User ID required" }
        val id = getItemId(item)
        require(id > 0) { "Item ID required for update" }


        firestore.collection(collection.userPath(userId))
            .document(id.toString())
            .set(item)
            .await()

        Log.d(TAG, "Updated ${collection.path}/$id")
        Result.Success(item)
    } catch (e: IllegalArgumentException) {
        Result.Error(e, e.message ?: "Validation error")
    } catch (e: Exception) {
        Log.e(TAG, "Error updating item", e)
        Result.Error(e)
    }

    // DELETE
    protected suspend fun delete(
        userId: String,
        collection: CollectionPath,
        id: Int
    ): Result<Boolean> = try {
        require(userId.isNotBlank()) { "User ID required" }
        require(id > 0) { "Valid item ID required" }

        firestore.collection(collection.userPath(userId))
            .document(id.toString())
            .delete()
            .await()

        Log.d(TAG, "Deleted ${collection.path}/$id")
        Result.Success(true)
    } catch (e: Exception) {
        Log.e(TAG, "Error deleting item", e)
        Result.Error(e)
    }

    // GET BY ID
    protected suspend fun <T : Any> getById(
        userId: String,
        collection: CollectionPath,
        id: Int,
        clazz: Class<T>
    ): Result<T?> = try {
        require(userId.isNotBlank()) { "User ID required" }
        require(id > 0) { "Valid item ID required" }

        val document = firestore.collection(collection.userPath(userId))
            .document(id.toString())
            .get()
            .await()

        val item = if (document.exists()) document.toObject(clazz) else null
        Result.Success(item)
    } catch (e: Exception) {
        Log.e(TAG, "Error getting item by id", e)
        Result.Error(e)
    }

    // QUERY
    protected suspend fun <T : Any> query(
        userId: String,
        collection: CollectionPath,
        clazz: Class<T>,
        options: QueryOptions = QueryOptions()
    ): Result<List<T>> = try {
        require(userId.isNotBlank()) { "User ID required" }

        var query: Query = firestore.collection(collection.userPath(userId))

        // Apply filters
        options.filters.forEach { (field, value) ->
            query = query.whereEqualTo(field, value)
        }

        options.rangeFilters.forEach { (field, range) ->
            range.first?.let { min -> query = query.whereGreaterThanOrEqualTo(field, min) }
            range.second?.let { max -> query = query.whereLessThanOrEqualTo(field, max) }
        }

        // Apply ordering
        options.orderBy?.let { orderField ->
            val direction = if (options.orderDirection == OrderDirection.ASCENDING) {
                Query.Direction.ASCENDING
            } else {
                Query.Direction.DESCENDING
            }
            query = query.orderBy(orderField, direction)
        }

        // Apply limit
        options.limit?.let { query = query.limit(it.toLong()) }

        val querySnapshot = query.get().await()
        val items = querySnapshot.documents.mapNotNull { it.toObject(clazz) }

        Result.Success(items)
    } catch (e: Exception) {
        Log.e(TAG, "Error querying items", e)
        Result.Error(e)
    }

    protected suspend fun createUserProfile(
        userId: String,
        profile: UserProfileDB
    ): Result<String> = try {
        require(userId.isNotBlank()) { "User ID required" }

        firestore.collection("users")
            .document(userId)
            .set(profile)
            .await()

        Log.d(TAG, "Created user profile for $userId")
        Result.Success(userId)
    } catch (e: Exception) {
        Log.e(TAG, "Error creating user profile", e)
        Result.Error(e)
    }

    // GET UserProfile
    protected suspend fun getProfile(
        userId: String
    ): Result<UserProfileDB?> = try {
        require(userId.isNotBlank()) { "User ID required" }

        val document = firestore.collection("users")
            .document(userId)
            .get()
            .await()

        val profile = if (document.exists()) {
            document.toObject(UserProfileDB::class.java)
        } else null

        Result.Success(profile)
    } catch (e: Exception) {
        Log.e(TAG, "Error getting user profile", e)
        Result.Error(e)
    }

    // UPDATE UserProfile
    protected suspend fun updateProfile(
        userId: String,
        updates: Map<String, Any>
    ): Result<Boolean> = try {
        require(userId.isNotBlank()) { "User ID required" }
        require(updates.isNotEmpty()) { "Updates cannot be empty" }

        firestore.collection("users")
            .document(userId)
            .update(updates)
            .await()

        Log.d(TAG, "Updated user profile for $userId")
        Result.Success(true)
    } catch (e: Exception) {
        Log.e(TAG, "Error updating user profile", e)
        Result.Error(e)
    }

    // DELETE UserProfile
    protected suspend fun deleteProfile(
        userId: String
    ): Result<Boolean> = try {
        require(userId.isNotBlank()) { "User ID required" }

        firestore.collection("users")
            .document(userId)
            .delete()
            .await()

        Log.d(TAG, "Deleted user profile for $userId")
        Result.Success(true)
    } catch (e: Exception) {
        Log.e(TAG, "Error deleting user profile", e)
        Result.Error(e)
    }

    // ========================================
    // HELPERS - Int ID handling
    // ========================================

    private fun generateIntId(): Int =
        System.currentTimeMillis().toInt().absoluteValue

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getItemId(item: T): Int = when (item) {
        is WorkoutDB -> item.id
        is WorkoutLogDB -> item.id
        is PersonalRecordDB -> item.id
        else -> 0
    }

}