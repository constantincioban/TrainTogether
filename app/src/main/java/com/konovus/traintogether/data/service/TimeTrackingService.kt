package com.konovus.traintogether.data.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.konovus.traintogether.data.notification.NotificationHelper
import com.konovus.traintogether.data.utils.StopWatchImpl
import com.konovus.traintogether.presentation.redux.AppState
import com.konovus.traintogether.presentation.redux.Store
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class TimeTrackingService: Service() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var stopWatch: StopWatchImpl

    @Inject
    lateinit var store: Store<AppState>

    // todo de ce sunt doua coroutineScope, si daca e nevoie de job in general ???
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        coroutineScope.launch {
            store.update { it.copy(isForegroundServiceOn = true) }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_STOPWATCH -> {
                println("ACTION_START_STOPWATCH: Starting stopwatch service")
                // onStartCommand
                startForeground(
                    NotificationHelper.ID_STOPWATCH,
                    notificationHelper.createStopWatchNotification(
                        stopWatch.state.value.time,
                        stopWatch.state.value.isRunning,
                    )
                )
                updateStopWatchNotification()
            }

            ACTION_STOP -> {
                coroutineScope.launch {
                    store.update { it.copy(isForegroundServiceOn = false) }
                }
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }



    private fun updateStopWatchNotification() {
        coroutineScope.launch {
            // updateStopWatchNotification
            stopWatch.state
                .map { Triple(it.time / 1000, it.isRunning, it.time) }
                .distinctUntilChanged { old, new -> old.first == new.first && old.second == new.second }
                .collect { (_, running, time) ->
                    val notification = notificationHelper.createStopWatchNotification(time, running)
                    NotificationManagerCompat.from(applicationContext)
                        .notify(NotificationHelper.ID_STOPWATCH, notification)
                }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    companion object {
        const val ACTION_START_STOPWATCH = "ACTION_START_STOPWATCH"
        const val ACTION_STOP = "ACTION_STOP"
    }
}