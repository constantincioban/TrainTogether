package com.konovus.traintogether.presentation.ui.screens.statsScreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.konovus.traintogether.data.local.entities.WorkoutLogDB
import com.konovus.traintogether.data.local.models.PersonalRecord
import com.konovus.traintogether.data.utils.GymExercise
import com.konovus.traintogether.data.utils.setToEndOfDay
import com.konovus.traintogether.data.utils.setToStartOfDay
import com.konovus.traintogether.presentation.redux.AppState
import com.konovus.traintogether.presentation.redux.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val store: Store<AppState>
) : ViewModel() {

    private val _uiState = mutableStateOf(StatsState())
    val uiState = _uiState

    init {
        loadStatsData()
    }

    // Event handling methods
    fun onEvent(event: StatsEvent) {
        when (event) {
            is StatsEvent.OnExerciseSelected -> onExerciseSelected(event.exercise)
            is StatsEvent.OnTimeframeSelected -> onTimeframeSelected(event.timeframe)
            StatsEvent.ToggleExerciseDropdown -> onToggleExerciseDropdown()
        }
    }

    private fun onExerciseSelected(exercise: GymExercise) {
        _uiState.value = _uiState.value.copy(
            selectedExercise = exercise,
            isExerciseDropdownExpanded = false
        )
        updateVolumeChartData()
    }

    private fun onTimeframeSelected(timeframe: TimeFrame) {
        _uiState.value = _uiState.value.copy(selectedTimeframe = timeframe)
        updateQuickStats()
        updateExerciseDistributionChartData()
        updateVolumeChartData()
    }

    private fun onToggleExerciseDropdown() {
        _uiState.value = _uiState.value.copy(
            isExerciseDropdownExpanded = !_uiState.value.isExerciseDropdownExpanded
        )
    }

    // Data loading and UI state update methods
    private fun loadStatsData() {
        val allWorkoutsLogs = store.state.value.workoutLogs
        val filteredWorkouts = getFilteredWorkouts(_uiState.value.selectedTimeframe)
        val allExercisesDoneByUser = allWorkoutsLogs.map{ it.exerciseLogs }.flatten().map{ it.exercise }.distinct()
            .ifEmpty { GymExercise.entries }

        _uiState.value = _uiState.value.copy(
            workoutLogs = allWorkoutsLogs, // Keep all workouts for other calculations
            personalRecords = store.state.value.personalRecords.sortedBy { it.date }.reversed(),
            quickStats = createQuickStats(filteredWorkouts), // Use filtered workouts
            workoutsThisPeriod = filteredWorkouts.size,
            totalVolume = getTotalVolume(filteredWorkouts),
            averageDuration = getAverageDuration(filteredWorkouts),
            exerciseCount = getUniqueExerciseCount(filteredWorkouts),
            allExercisesDoneByUser = allExercisesDoneByUser
        )

        updateExerciseDistributionChartData()
        updateVolumeChartData()
    }

    private fun updateQuickStats() {
        val filteredWorkouts = getFilteredWorkouts(_uiState.value.selectedTimeframe)
        val updatedQuickStats = createQuickStats(filteredWorkouts)

        _uiState.value = _uiState.value.copy(
            quickStats = updatedQuickStats,
            workoutsThisPeriod = filteredWorkouts.size,
            totalVolume = getTotalVolume(filteredWorkouts),
            averageDuration = getAverageDuration(filteredWorkouts),
            exerciseCount = getUniqueExerciseCount(filteredWorkouts)
        )
    }

    private fun createQuickStats(workoutLogs: List<WorkoutLogDB>): List<QuickStat> {
        return listOf(
            QuickStat(
                type = QuickStatType.TOTAL_WORKOUTS,
                value = workoutLogs.size.toString(),
                label = "Workouts",
                icon = Icons.Rounded.FitnessCenter,
                color = Color(0xFF007AFF)
            ),
            QuickStat(
                type = QuickStatType.TOTAL_VOLUME,
                value = "${getTotalVolume(workoutLogs) / 1000}k",
                label = "Total Volume",
                icon = Icons.Rounded.TrendingUp,
                color = Color(0xFFFFA726)
            ),
            QuickStat(
                type = QuickStatType.AVERAGE_DURATION,
                value = "${getAverageDuration(workoutLogs)}m",
                label = "Avg Duration",
                icon = Icons.Rounded.Timer,
                color = Color(0xFF66BB6A)
            ),
            QuickStat(
                type = QuickStatType.PERSONAL_RECORDS,
                value = getPRsForTimeframe(_uiState.value.personalRecords, _uiState.value.selectedTimeframe).toString(),
                label = "New PRs",
                icon = Icons.Rounded.EmojiEvents,
                color = Color(0xFFFFD700)
            )
        )
    }

    private fun updateExerciseDistributionChartData() {
        val filteredWorkouts = getFilteredWorkouts(_uiState.value.selectedTimeframe)
        val distribution = filteredWorkouts
            .flatMap { it.exerciseLogs }
            .groupBy { it.exercise }
            .map { PieEntry(it.value.size.toFloat(), it.key.displayName) }

        _uiState.value = _uiState.value.copy(exerciseDistribution = distribution)
    }

    private fun updateVolumeChartData() {
        val exercise = _uiState.value.selectedExercise
        val timeframe = _uiState.value.selectedTimeframe
        val workoutLogs = store.state.value.workoutLogs

        val data = generateVolumeDataPoints(exercise, timeframe, workoutLogs)
        val total = data.sumOf { it.volume }

        _uiState.value = _uiState.value.copy(
            volumeData = data,
            totalVolume = total
        )
    }

    private fun generateVolumeDataPoints(
        exercise: GymExercise,
        timeframe: TimeFrame,
        workoutLogs: List<WorkoutLogDB>
    ): List<VolumeDataPoint> {
        // Filter workouts by timeframe first
        val filteredWorkouts = getFilteredWorkouts(timeframe)

        // Get only workouts that contain the selected exercise
        val relevantWorkouts = filteredWorkouts
            .filter { workout ->
                workout.exerciseLogs.any { it.exercise == exercise }
            }
            .sortedBy { it.startedTimestamp }

        // Convert each workout to a data point
        return relevantWorkouts.mapIndexed { index, workout ->
            val volume = getVolumeFromWorkout(workout, exercise)
            val dateLabel = formatWorkoutDate(workout.startedTimestamp)
            VolumeDataPoint(dateLabel, volume)
        }
    }

    private fun formatWorkoutDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    private fun generateWeeklyData(workouts: List<WorkoutLogDB>, exercise: GymExercise): List<VolumeDataPoint> {
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return (0 .. 6).map { daysAgo ->
            val volume = getVolumeForTimeRange(workouts, exercise) { getDayRange(daysAgo) }
            VolumeDataPoint(dayNames[daysAgo], volume)
        }
    }

    private fun generateMonthlyData(workouts: List<WorkoutLogDB>, exercise: GymExercise): List<VolumeDataPoint> {
        val thirtyDaysAgo = System.currentTimeMillis() - (32 * 24 * 60 * 60 * 1000L)

        return workouts
            .filter { workout ->
                workout.startedTimestamp >= thirtyDaysAgo
            }
            .filter { workout ->
                workout.exerciseLogs.any { it.exercise == exercise }
            }
            .sortedBy { it.startedTimestamp }
            .map { workout ->
                val volume = getVolumeFromWorkout(workout, exercise)
                val dateLabel = SimpleDateFormat("dd/MM", Locale.getDefault()).format(workout.startedTimestamp)
                VolumeDataPoint(dateLabel, volume)
            }
    }

    private fun generateThreeMonthData(workouts: List<WorkoutLogDB>, exercise: GymExercise): List<VolumeDataPoint> {
        return (2 downTo 0).map { monthsAgo ->
            val volume = getVolumeForTimeRange(workouts, exercise) { getMonthRange(monthsAgo) }
            val monthName = getMonthName(monthsAgo)
            VolumeDataPoint(monthName, volume)
        }
    }

    private fun generateSixMonthData(workouts: List<WorkoutLogDB>, exercise: GymExercise): List<VolumeDataPoint> {
        val calendar = Calendar.getInstance()

        return (5 downTo 0).map { monthsAgo ->
            calendar.time = Date()
            calendar.add(Calendar.MONTH, -monthsAgo)

            val monthLabel = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
            val volume = getVolumeForTimeRange(workouts, exercise) { getMonthRange(monthsAgo) }

            VolumeDataPoint(monthLabel, volume)
        }
    }

    private fun generateYearlyData(workouts: List<WorkoutLogDB>, exercise: GymExercise): List<VolumeDataPoint> {
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        return monthNames.mapIndexed { index, month ->
            val volume = getVolumeForTimeRange(workouts, exercise) { getSpecificMonthRange(index) }
            VolumeDataPoint(month, volume)
        }
    }

    private fun generateAllTimeData(workouts: List<WorkoutLogDB>, exercise: GymExercise): List<VolumeDataPoint> {
        return workouts
            .groupBy { getMonthYearString(it.startedTimestamp) }
            .map { (monthYear, logs) ->
                val volume = logs.sumOf { getVolumeFromWorkout(it, exercise) }
                VolumeDataPoint(monthYear, volume)
            }
            .sortedBy { it.label }
    }

    private fun getPRsForTimeframe(
        personalRecords: List<PersonalRecord>,
        timeframe: TimeFrame
    ): Int {
        if (personalRecords.isEmpty()) return 0

        val currentTime = System.currentTimeMillis()
        val startTime = when (timeframe) {
            TimeFrame.WEEK -> currentTime - (7 * 24 * 60 * 60 * 1000L)
            TimeFrame.MONTH -> currentTime - (30 * 24 * 60 * 60 * 1000L)
            TimeFrame.THREE_MONTHS -> currentTime - (90 * 24 * 60 * 60 * 1000L)
            TimeFrame.SIX_MONTHS -> currentTime - (180 * 24 * 60 * 60 * 1000L)
            TimeFrame.ONE_YEAR -> currentTime - (365 * 24 * 60 * 60 * 1000L)
            TimeFrame.ALL_TIME -> 0L
        }

        return personalRecords.count { pr ->
            pr.date >= startTime
        }
    }


    // Volume calculation methods
    private fun getVolumeForTimeRange(
        workouts: List<WorkoutLogDB>,
        exercise: GymExercise,
        rangeProvider: () -> Pair<Long, Long>
    ): Int {
        val (start, end) = rangeProvider()
        return workouts
            .filter { it.startedTimestamp in start..end }
            .sumOf { getVolumeFromWorkout(it, exercise) }
    }

    private fun getVolumeFromWorkout(workout: WorkoutLogDB, exercise: GymExercise): Int {
        return workout.exerciseLogs
            .filter { it.exercise == exercise && it.set.contains("Working") }
            .sumOf { it.reps * it.weight }
    }

    // Time range calculation methods
    private fun createCalendar(): Calendar = Calendar.getInstance()

    private fun getDayRange(daysAgo: Int): Pair<Long, Long> {
        val calendar = createCalendar()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)

        val dayStart = calendar.setToStartOfDay().timeInMillis
        val dayEnd = calendar.setToEndOfDay().timeInMillis

        return dayStart to dayEnd
    }

    private fun getWeekRange(weeksAgo: Int): Pair<Long, Long> {
        val calendar = createCalendar()
        calendar.add(Calendar.WEEK_OF_YEAR, -weeksAgo)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val weekStart = calendar.setToStartOfDay().timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, 6)
        val weekEnd = calendar.setToEndOfDay().timeInMillis

        return weekStart to weekEnd
    }

    private fun getMonthRange(monthsAgo: Int): Pair<Long, Long> {
        val calendar = createCalendar()
        calendar.add(Calendar.MONTH, -monthsAgo)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val monthStart = calendar.setToStartOfDay().timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val monthEnd = calendar.timeInMillis

        return monthStart to monthEnd
    }

    private fun getSpecificMonthRange(month: Int): Pair<Long, Long> {
        val calendar = createCalendar()
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.set(currentYear, month, 1)
        val monthStart = calendar.setToStartOfDay().timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val monthEnd = calendar.timeInMillis

        return monthStart to monthEnd
    }

    // Helper and utility methods
    private fun getMonthName(monthsAgo: Int): String {
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val calendar = createCalendar()
        calendar.add(Calendar.MONTH, -monthsAgo)
        return monthNames[calendar.get(Calendar.MONTH)]
    }

    private fun getMonthYearString(timestamp: Long): String {
        val calendar = createCalendar()
        calendar.timeInMillis = timestamp
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return "$year-${String.format("%02d", month)}"
    }

    private fun getFilteredWorkouts(timeFrame: TimeFrame): List<WorkoutLogDB> {
        if (timeFrame == TimeFrame.ALL_TIME) return store.state.value.workoutLogs

        val now = System.currentTimeMillis()
        val cutoffTime = when (timeFrame) {
            TimeFrame.WEEK -> now - (7 * 24 * 60 * 60 * 1000L)
            TimeFrame.MONTH -> now - (31 * 24 * 60 * 60 * 1000L)
            TimeFrame.THREE_MONTHS -> now - (90 * 24 * 60 * 60 * 1000L)
            TimeFrame.SIX_MONTHS -> now - (180 * 24 * 60 * 60 * 1000L)
            TimeFrame.ONE_YEAR -> now - (365 * 24 * 60 * 60 * 1000L)
            TimeFrame.ALL_TIME -> 0L
        }

        return store.state.value.workoutLogs.filter { it.startedTimestamp >= cutoffTime }
    }

    private fun getUniqueExerciseCount(workouts: List<WorkoutLogDB>): Int {
        return workouts
            .flatMap { it.exerciseLogs }
            .map { it.exercise }
            .distinct()
            .size
    }

    private fun getTotalVolume(workouts: List<WorkoutLogDB>): Int {
        return workouts
            .flatMap { it.exerciseLogs }
            .sumOf { it.reps * it.weight }
    }

    private fun getAverageDuration(workouts: List<WorkoutLogDB>): Int {
        if (workouts.isEmpty()) return 0
        return workouts
            .map { it.finishedTimestamp - it.startedTimestamp }
            .average()
            .let { (it / (1000 * 60)).toInt() }
    }
}

data class StatsState(
    val averageDuration: Int = 0,
    val quickStats: List<QuickStat> = emptyList(),
    val workoutsThisPeriod: Int = 0,
    val totalVolume: Int = 0,
    val exerciseCount: Int = 0,
    val workoutLogs: List<WorkoutLogDB> = emptyList(),
    val frequencyData: List<Entry> = emptyList(),
    val exerciseDistribution: List<PieEntry> = emptyList(),
    val personalRecords: List<PersonalRecord> = emptyList(),
    val selectedTimeframe: TimeFrame = TimeFrame.MONTH,
    val selectedExercise: GymExercise = GymExercise.BENCH_PRESS,
    val volumeData: List<VolumeDataPoint> = emptyList(),
    val isExerciseDropdownExpanded: Boolean = false,
    val allExercisesDoneByUser: List<GymExercise> = emptyList()
)

sealed class StatsEvent {
    data class OnExerciseSelected(val exercise: GymExercise) : StatsEvent()
    data class OnTimeframeSelected(val timeframe: TimeFrame) : StatsEvent()
    object ToggleExerciseDropdown : StatsEvent()

}

data class VolumeDataPoint(
    val label: String,
    val volume: Int
)

enum class TimeFrame(val displayName: String) {
    WEEK("7D"),
    MONTH("1M"),
    THREE_MONTHS("3M"),
    SIX_MONTHS("6M"),
    ONE_YEAR("1Y"),
    ALL_TIME("All Time")
}

enum class QuickStatType {
    TOTAL_WORKOUTS,
    TOTAL_VOLUME,
    AVERAGE_DURATION,
    PERSONAL_RECORDS
}

data class QuickStat(
    val type: QuickStatType,
    val value: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)