package com.konovus.traintogether.data.di

import android.app.Application
import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.konovus.traintogether.data.auth.FirebaseAuth
import com.konovus.traintogether.data.auth.IAuthHelper
import com.konovus.traintogether.data.local.backup.BackupManager
import com.konovus.traintogether.data.local.backup.IBackupManager
import com.konovus.traintogether.data.local.preferences.SettingsManager
import com.konovus.traintogether.data.local.preferences.SettingsManagerImpl
import com.konovus.traintogether.data.notification.NotificationHelper
import com.konovus.traintogether.data.remote.IWorkoutRepository
import com.konovus.traintogether.data.remote.WorkoutRepository
import com.konovus.traintogether.data.utils.StopWatchImpl
import com.konovus.traintogether.presentation.redux.AppState
import com.konovus.traintogether.presentation.redux.Store
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Module
    @InstallIn(SingletonComponent::class)
    object AppModule {

        @Provides
        @Singleton
        fun provideBackupManager(
            @ApplicationContext context: Context
        ): IBackupManager {
            return BackupManager(context)
        }
    }

    @Provides
    @Singleton
    fun provideSettingsManager(app: Application): SettingsManager =
        SettingsManagerImpl(app)

    @Provides
    @Singleton
    fun provideWorkoutRepository(): IWorkoutRepository = WorkoutRepository(Firebase.firestore)

    @Provides
    @Singleton
    fun provideAuth(): IAuthHelper = FirebaseAuth()

    @Provides
    @Singleton
    fun provideStore(app: Application): Store<AppState> = Store(AppState())

    @Provides
    @Singleton
    fun provideStopWatch(): StopWatchImpl = StopWatchImpl()

    @Provides
    @Singleton
    fun provideNotificationHelper(app: Application): NotificationHelper = NotificationHelper(app)


}