package com.konovus.traintogether.data.utils

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class StopWatchImpl {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickerJob: Job? = null

    val state = MutableStateFlow(StopWatchState())

    private var segmentStart = 0L   // elapsedRealtime when the current run began
    private var accumulated = 0L    // time banked from earlier segments

    private fun currentElapsed(): Long =
        accumulated + if (state.value.isRunning) SystemClock.elapsedRealtime() - segmentStart else 0L

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                state.update { if (it.isRunning) it.copy(time = currentElapsed()) else it }
                delay(100.milliseconds)
            }
        }
    }

    @Synchronized
    fun start() {
        accumulated = 0L
        segmentStart = SystemClock.elapsedRealtime()
        state.value = state.value.copy(
            isRunning = true,
            time = 0L,
            startTimestamp = System.currentTimeMillis(), // wall clock, WorkoutViewModel relies on this
            endTimestamp = 0,
        )
        startTicker()
    }

    @Synchronized
    fun resume() {
        if (state.value.isRunning) return
        segmentStart = SystemClock.elapsedRealtime()
        state.value = state.value.copy(isRunning = true)
        startTicker()
    }

    @Synchronized
    fun pause() {
        if (!state.value.isRunning) return
        accumulated = currentElapsed()
        tickerJob?.cancel()
        state.value = state.value.copy(isRunning = false, time = accumulated)
    }

    @Synchronized
    fun reset() {
        tickerJob?.cancel()
        accumulated = 0L
        state.value = state.value.copy(
            isRunning = false,
            endTimestamp = System.currentTimeMillis(),
            time = 0L,
        )
    }

    fun dismiss() { tickerJob?.cancel() }
}

data class StopWatchState(
    val isRunning: Boolean = false,
    val time: Long = 0,
    val startTimestamp: Long = 0,
    val endTimestamp: Long = 0,
)