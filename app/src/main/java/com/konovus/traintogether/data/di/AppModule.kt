package com.konovus.traintogether.data.di

import android.app.Application
import com.konovus.traintogether.data.local.preferences.SettingsManager
import com.konovus.traintogether.data.local.preferences.SettingsManagerImpl
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

}