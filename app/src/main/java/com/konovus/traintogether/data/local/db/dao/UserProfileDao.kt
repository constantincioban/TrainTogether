package com.konovus.traintogether.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.konovus.traintogether.data.local.entities.UserProfileDB
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfileDB?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileDB?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileDB)

    @Update
    suspend fun updateUserProfile(profile: UserProfileDB)

    @Delete
    suspend fun deleteUserProfile(profile: UserProfileDB)

    @Query("DELETE FROM user_profile")
    suspend fun deleteAllUserProfiles()
}