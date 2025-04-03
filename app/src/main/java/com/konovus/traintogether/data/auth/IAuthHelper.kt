package com.konovus.traintogether.data.auth

import com.konovus.traintogether.data.local.models.User


interface IAuthHelper {

    suspend fun login(): User?
    fun logout(): Unit
}