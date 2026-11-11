package com.konovus.traintogether.data.local.models

import com.konovus.traintogether.data.local.entities.UserProfileDB

data class UserProfile(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val profilePicUri: String = "",
) {
    fun toDbEntity() = UserProfileDB(
        id = id,
        username = username,
        profilePicUri = profilePicUri,
        email = email
    )
}
