package com.konovus.traintogether.data.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HiltApplication : Application() {

}

const val TAG = "TRAIN_TOGETHER"