package com.konovus.traintogether.data.auth

interface IAuthHelper {

    suspend fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit)

    suspend fun createUserAccount(email: String, password: String, onResult: (Boolean, String?) -> Unit)

    fun signInWithEmailPassword(email: String, password: String, onResult: (Boolean, String?) -> Unit)

}