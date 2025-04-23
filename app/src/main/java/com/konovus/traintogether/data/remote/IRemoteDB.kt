package com.konovus.traintogether.data.remote

interface IRemoteDB {

    // Generic method to fetch all data from a user's collection
    suspend fun <T : Any> getData(userKey: String, collectionPath: CollectionPath, returnObjectClass: Class<T>): Response<List<T>>

    sealed class Response<out T> {
        data class Success<out T>(val data: T) : Response<T>()
        data class Error(val message: String) : Response<Nothing>()
    }
}

enum class CollectionPath(val path: String) {
    USERS("users"),
}