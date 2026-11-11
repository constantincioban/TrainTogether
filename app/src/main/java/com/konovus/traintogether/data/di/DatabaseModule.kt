package com.konovus.traintogether.data.di

import android.content.Context
import androidx.room.Room
import com.konovus.traintogether.data.local.db.ILocalDBRepository
import com.konovus.traintogether.data.local.db.LocalDBRepository
import com.konovus.traintogether.data.local.db.TrainTogetherDatabase
import com.konovus.traintogether.data.local.db.dao.PersonalRecordDao
import com.konovus.traintogether.data.local.db.dao.UserProfileDao
import com.konovus.traintogether.data.local.db.dao.WorkoutDao
import com.konovus.traintogether.data.local.db.dao.WorkoutLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTrainTogetherDatabase(
        @ApplicationContext context: Context
    ): TrainTogetherDatabase {
        return Room.databaseBuilder(
            context,
            TrainTogetherDatabase::class.java,
            "train_together_database"
        )
            .fallbackToDestructiveMigration() // Remove in production, use proper migrations
            .build()
    }

    @Provides
    @Singleton
    fun provideWorkoutDao(database: TrainTogetherDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    @Singleton
    fun provideWorkoutLogDao(database: TrainTogetherDatabase): WorkoutLogDao {
        return database.workoutLogDao()
    }

    @Provides
    @Singleton
    fun providePersonalRecordDao(database: TrainTogetherDatabase): PersonalRecordDao {
        return database.personalRecordDao()
    }

    @Provides
    @Singleton
    fun provideUserProfileDao(database: TrainTogetherDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    @Singleton
    fun provideLocalRepository(database: TrainTogetherDatabase): ILocalDBRepository {
        return LocalDBRepository(database)
    }
}