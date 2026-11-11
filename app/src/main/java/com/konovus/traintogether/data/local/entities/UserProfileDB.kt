package com.konovus.traintogether.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.konovus.traintogether.data.local.models.UserProfile

@Entity(tableName = "user_profile")
data class UserProfileDB(
    @PrimaryKey
    val id: String = "",
    val username: String = "",
    val profilePicUri: String = "",
    val email: String = "",
) {
    fun toUiModel() = UserProfile(
        id = id,
        username = username,
        email = email,
        profilePicUri = profilePicUri,
    )
}