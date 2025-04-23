package com.konovus.traintogether.data.di

import android.app.Application
import com.konovus.traintogether.data.remote.FireStoreDB
import com.konovus.traintogether.data.remote.IRemoteDB
import com.konovus.traintogether.data.local.preferences.SettingsManager
import com.konovus.traintogether.data.local.preferences.SettingsManagerImpl
import com.konovus.traintogether.data.auth.FirebaseAuth
import com.konovus.traintogether.data.auth.IAuthHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideSettingsManager(app: Application): SettingsManager =
        SettingsManagerImpl(app.applicationContext)

    @Provides
    @Singleton
    fun provideRemoteDb(): IRemoteDB = FireStoreDB()

    @Provides
    @Singleton
    fun provideAuth(): IAuthHelper = FirebaseAuth()
}