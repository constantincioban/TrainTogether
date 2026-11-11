package com.konovus.traintogether.data.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

fun Long.toFormattedDateForStats(): String {
    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    return formatter.format(Date(this))
}


fun Calendar.setToStartOfDay(): Calendar = apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

fun Calendar.setToEndOfDay(): Calendar = apply {
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}

fun millisecondsToTimerString(milliseconds: Long): String {
    // Determine if the input is negative
    val isNegative = milliseconds < 0

    // Use absolute value to calculate hours, minutes, and seconds
    val totalSeconds = Math.abs(milliseconds) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    // Build the time string conditionally
    val timeStringBuilder = StringBuilder()
    if (hours > 0) {
        timeStringBuilder.append(String.format("%d:", hours))
    }
    if (minutes > 0 || hours > 0) { // Show minutes if hours are shown or if minutes are greater than 0
        timeStringBuilder.append(String.format("%02d:", minutes))
    }
    timeStringBuilder.append(String.format("%02d", seconds))

    // Add negative sign if the original input was negative
    return if (isNegative) "-$timeStringBuilder" else timeStringBuilder.toString()
}


fun String.toAppTheme(): AppTheme = when (this.lowercase()) {
    "light" -> AppTheme.LIGHT
    "dark" -> AppTheme.DARK
    "auto" -> AppTheme.AUTO
    else -> AppTheme.AUTO // Default fallback
}

fun Long.toTimeFormat(): String {
    val totalMinutes = abs(this) / (1000 * 60)
    val totalHours = totalMinutes / 60
    val minutes = (totalMinutes % 60).toInt()
    val hours = totalHours.toInt()

    return when {
        totalHours < 1 -> {
            // Less than 1 hour: "45 min"
            "$minutes min"
        }
        totalHours < 24 -> {
            // 1-23 hours: "1h:12m"
            "${hours}h ${minutes}m"
        }
        else -> {
            // 24+ hours: "1d:1h"
            val days = totalHours / 24
            val remainingHours = (totalHours % 24).toInt()
            "${days}d ${remainingHours}h"
        }
    }
}

fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}

// Extension function to convert parameters to a URI path string
fun List<Any>.toNavigationPath(): String {
    return if (this.isEmpty()) {
        ""
    } else {
        this.joinToString(separator = "/", prefix = "/") { it.toString() }
    }
}


@Immutable
data class ImmutableList<T>(
    val wrapped: List<T> = listOf()
) : List<T> by wrapped

@Immutable
data class ImmutableMap<K, V>(
    val wrapped: Map<K, V> = mapOf()
) : Map<K, V> by wrapped

@SuppressLint("Range")
fun getDisplayNameFromUri(uri: android.net.Uri, context: Context): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            return displayName.split(".")[0].replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ENGLISH
                ) else it.toString()
            }
        }
    }
    return ""
}

fun findConsecutiveRanges(numbers: List<Int>): List<Pair<Int, Int>> {
    val result = mutableListOf<Pair<Int, Int>>()

    var start = 0
    while (start < numbers.size) {
        var end = start

        // Extend the run while the next number is consecutive
        while (end + 1 < numbers.size && numbers[end + 1] == numbers[end] + 1) {
            end++
        }

        // Add range if it has at least 2 consecutive numbers
        if (end > start) {
            result.add(start to end)
        }

        // Move to next potential range
        start = if (end > start) end + 1 else start + 1
    }

    return result
}


fun formatMillisToString(millis: Long): String {
    val date = Date(millis)
    val formatter = SimpleDateFormat("d MMM yy '@'HH:mm", Locale.UK)
    return formatter.format(date)
}

fun formatStringToMillis(dateString: String): Long {
    val formatter = SimpleDateFormat("d MMM yy '@'HH:mm", Locale.UK)
    return formatter.parse(dateString)?.time
        ?: throw IllegalArgumentException("Invalid date format")
}


fun formatMillisToDuration(millis: Long): String {
    val totalMinutes = millis / (60 * 1000)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return buildString {
        if (hours > 0) append("$hours hour${if (hours > 1) "s" else ""} ")
        if (minutes > 0 || hours == 0L) append("$minutes minute${if (minutes != 1L) "s" else ""}")
    }.trim()
}


fun formatMillisToReadableTime(millis: Long): String {
    val totalMinutes = millis / 1000 / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}


fun getTodaysDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    val currentDate = Date()
    return dateFormat.format(currentDate)
}

fun formatSecondsToMMss(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

fun isSameDate(dateMillis: Long, dateString: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val dateFromMillis = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val dateFromString = LocalDate.parse(dateString, formatter)

    return dateFromMillis.isEqual(dateFromString)
}

fun isSameMonth(dateInMillis: Long, month: String, zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
    val date = Instant.ofEpochMilli(dateInMillis).atZone(zoneId).toLocalDate()
    val monthOfDate = date.monthValue.toString()

    return monthOfDate == month
}


fun countConsecutiveDates(datesInMillis: List<Long>, zoneId: ZoneId = ZoneId.systemDefault()): Int {
    val today = LocalDate.now(zoneId)
    var count = 0
    var previousDate = today

    for (dateInMillis in datesInMillis) {
        val date = Instant.ofEpochMilli(dateInMillis).atZone(zoneId).toLocalDate()
        if (date.isEqual(previousDate) || date.isEqual(previousDate.minusDays(1))) {
            count++
            previousDate = date
        } else {
            continue
        }
    }
    return count
}

fun isSameDate(duration1: Long, duration2: Long, zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
    val date1 = Instant.ofEpochMilli(duration1).atZone(zoneId).toLocalDate()
    val date2 = Instant.ofEpochMilli(duration2).atZone(zoneId).toLocalDate()
    return date1.isEqual(date2)
}

fun oneDayInMillis(): Long {
    return 24 * 60 * 60 * 1000L
}

fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("EEE dd MMM, yyyy", Locale.ENGLISH)
    return dateFormat.format(Date())
}

fun formatTime(timeInMillis: Long): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timeInMillis))
}



fun formatDuration(durationInSeconds: Long): String {
    val hours = durationInSeconds / 3600
    val minutes = (durationInSeconds % 3600) / 60
    val seconds = durationInSeconds % 60

    return if (hours > 0 && seconds > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else if (hours > 0) {
        String.format("%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

fun formatDurationFromMillis(duration: Long): String {
    val hours = duration / 3600_000
    val minutes = duration / 60000
    val seconds = if (duration >= 60_000) (duration % 60_000) / 1000 else (duration / 1000)
    return if (hours > 0) {
        String.format("%02d:%02d", hours, minutes)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

@Composable
inline fun <reified T> Flow<T>.observeWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline action: suspend (T) -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        lifecycleOwner.lifecycleScope.launch {
            flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState).collect(action)
        }
    }
}

fun Date.formatToString(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}
