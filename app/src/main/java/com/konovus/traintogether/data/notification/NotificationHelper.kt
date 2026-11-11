package com.konovus.traintogether.data.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.konovus.traintogether.R
import com.konovus.traintogether.presentation.MainActivity
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    private val context: Context,
) {

    private val notificationManager =
        context.getSystemService(NotificationManager::class.java) as NotificationManager


    companion object {
        const val CHANNEL_ID_STOPWATCH = "101"
        const val ACTION = "action"
        const val ID_STOPWATCH = 1
    }

    fun createStopWatchNotification(
        elapsedTime: Long,
        isRunning: Boolean = true,
    ): Notification {
        createStopWatchNotificationChannel()

        val totalSeconds = elapsedTime / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds / 60) % 60
        val seconds = totalSeconds % 60

        val contentText = if (hours > 0)
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else
            String.format("%02d:%02d", minutes, seconds)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_STOPWATCH)
            .setContentTitle("Stopwatch")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setContentIntent(getActivityActionPendingIntent())

        if (isRunning) {
            builder
                .setShowWhen(true)
                .setUsesChronometer(true)
                // the chronometer counts up from this moment
                .setWhen(System.currentTimeMillis() - elapsedTime)
        } else {
            builder
                .setShowWhen(false)
                .setUsesChronometer(false)
                .setContentText(contentText)
        }

        return builder.build()
    }


    private fun createStopWatchNotificationChannel() {
        val notificationChannel =
            NotificationChannel(
                CHANNEL_ID_STOPWATCH,
                "Stopwatch Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun getActivityActionPendingIntent(): PendingIntent {
        val resultIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "workout_screen")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntentFlag = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(
            context,
            101,
            resultIntent,
            pendingIntentFlag
        )
    }

}