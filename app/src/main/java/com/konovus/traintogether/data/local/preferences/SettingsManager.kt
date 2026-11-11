package com.konovus.traintogether.data.local.preferences

interface SettingsManager {

    companion object{
        const val SERVICE_IS_SET = "service_is_set"
        const val TIMEFRAME = "timeframe"
        const val USERNAME = "username"
        const val PROFILE_PIC_URI = "profile_pic_uri"
        const val THEME = "theme"
        const val NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val UNITS = "units"
        const val LANGUAGE = "language"
        const val WORKOUT_FREQUENCY = "workout_frequency"
        const val EXERCISE_PR = "exercise_pr"
    }

    suspend fun saveIntSetting(key: String, value: Int)

    suspend fun saveBooleanSetting(key: String, value: Boolean)

    suspend fun saveStringSetting(key: String, value: String)

    suspend fun saveStringListSetting(key: String, value: List<String>)

    suspend fun readStringSetting(key: String): String

    suspend fun readIntSetting(key: String): Int

    suspend fun readBooleanSetting(key: String): Boolean

    suspend fun readStringListSetting(key: String): List<String>

    suspend fun saveObjectList(key: String, objects: List<Any>)

    suspend fun <T> readObjectList(key: String, clazz: Class<T>): List<T>
}