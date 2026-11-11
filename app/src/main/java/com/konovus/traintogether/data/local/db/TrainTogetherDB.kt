package com.konovus.traintogether.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.konovus.traintogether.data.local.db.dao.PersonalRecordDao
import com.konovus.traintogether.data.local.db.dao.UserProfileDao
import com.konovus.traintogether.data.local.db.dao.WorkoutDao
import com.konovus.traintogether.data.local.db.dao.WorkoutLogDao
import com.konovus.traintogether.data.local.entities.PersonalRecordDB
import com.konovus.traintogether.data.local.entities.UserProfileDB
import com.konovus.traintogether.data.local.entities.WorkoutDB
import com.konovus.traintogether.data.local.entities.WorkoutLogDB

@Database(
    entities = [
        WorkoutDB::class,
        WorkoutLogDB::class,
        PersonalRecordDB::class,
        UserProfileDB::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TrainTogetherDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutLogDao(): WorkoutLogDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun userProfileDao(): UserProfileDao
}