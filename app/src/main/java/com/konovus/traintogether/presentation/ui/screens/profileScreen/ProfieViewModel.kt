package com.konovus.traintogether.presentation.ui.screens.profileScreen


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konovus.traintogether.data.local.entities.WorkoutLogDB
import com.konovus.traintogether.data.local.models.PersonalRecord
import com.konovus.traintogether.data.local.models.WorkoutLog
import com.konovus.traintogether.data.remote.IWorkoutRepository
import com.konovus.traintogether.data.utils.GymExercise
import com.konovus.traintogether.presentation.redux.AppState
import com.konovus.traintogether.presentation.redux.Store
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.Units
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

// =================================
// PROFILE VIEW MODEL
// =================================

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val remoteDb: IWorkoutRepository,
    private val store: Store<AppState>,
) : ViewModel() {

    private val _uiState = mutableStateOf(ProfileState())
    val uiState = _uiState

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            store.state.collect { appState ->
                val workoutLogs = appState.workoutLogs
                val personalRecords = appState.personalRecords

                _uiState.value = _uiState.value.copy(
                    userName = appState.userProfile.username,
                    userEmail = appState.userEmail,
                    profilePicUri = appState.userProfile.profilePicUri,
                    units = appState.units,

                    // Athletic Identity
                    trainingAge = calculateTrainingAge(workoutLogs),
                    trainingFrequency = calculateTrainingFrequency(workoutLogs),
                    preferredTrainingTime = findPreferredTrainingTime(workoutLogs),
                    consistencyRating = calculateConsistencyRating(appState.streak, workoutLogs),
                    trainingFocus = detectTrainingFocus(workoutLogs),

                    // Strength Profile
                    bestLifts = findBestLifts(personalRecords).take(5),
                    risingStars = findRisingStars(workoutLogs),

                    // Training DNA
                    goToExercises = findGoToExercises(workoutLogs).take(5),
                    averageSessionLength = calculateAverageSessionLength(workoutLogs),
                    mostActiveDay = findMostActiveDay(workoutLogs),
                    bestTrainingBlock = findBestTrainingBlock(workoutLogs),

                    // Milestones
                    milestones = generateMilestones(workoutLogs, personalRecords, appState.streak),

                    // Recent & History
                    recentWorkouts = workoutLogs.sortedByDescending { it.startedTimestamp }
                        .take(10),
                    groupedWorkoutLogs = groupWorkoutLogsByDate(workoutLogs)
                )
            }
        }
    }

    private fun calculateTrainingAge(logs: List<WorkoutLogDB>): String {
        if (logs.isEmpty()) return "Just started"
        val firstWorkout =
            logs.minByOrNull { it.startedTimestamp }?.startedTimestamp ?: return "Just started"
        val days = ((System.currentTimeMillis() - firstWorkout) / (24 * 60 * 60 * 1000)).toInt()
        return when {
            days < 30 -> "$days days"
            days < 365 -> "${days / 30} months"
            else -> "${days / 365} year${if (days / 365 > 1) "s" else ""}"
        }
    }

    private fun calculateTrainingFrequency(logs: List<WorkoutLogDB>): String {
        if (logs.isEmpty()) return "Getting started"
        val weeksActive =
            ((System.currentTimeMillis() - logs.minOf { it.startedTimestamp }) / (7 * 24 * 60 * 60 * 1000)).toInt()
                .coerceAtLeast(1)
        val avgPerWeek = logs.size / weeksActive
        return when {
            avgPerWeek >= 6 -> "Daily Grinder (${avgPerWeek}x/week)"
            avgPerWeek >= 4 -> "Consistent (${avgPerWeek}x/week)"
            avgPerWeek >= 2 -> "Regular (${avgPerWeek}x/week)"
            else -> "Weekend Warrior"
        }
    }

    private fun findPreferredTrainingTime(logs: List<WorkoutLogDB>): String {
        if (logs.isEmpty()) return ""
        val timeGroups = logs.groupBy {
            val hour = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(it.startedTimestamp),
                ZoneId.systemDefault()
            ).hour
            when {
                hour < 12 -> "Morning"
                hour < 17 -> "Afternoon"
                else -> "Evening"
            }
        }
        return timeGroups.maxByOrNull { it.value.size }?.key?.let { "$it Athlete" } ?: ""
    }

    private fun calculateConsistencyRating(streak: Int, logs: List<WorkoutLogDB>): String {
        if (logs.isEmpty()) return "Getting Started"

        // Calculate training period in weeks
        val oldestWorkout = logs.minOf { it.startedTimestamp }
        val currentTime = System.currentTimeMillis()
        val trainingPeriodWeeks = ((currentTime - oldestWorkout) / (7 * 24 * 60 * 60 * 1000.0))
            .coerceAtLeast(1.0)

        // Average workouts per week
        val workoutsPerWeek = logs.size / trainingPeriodWeeks

        // Recent activity (last 8 weeks for more realistic view)
        val eightWeeksAgo = currentTime - (8 * 7 * 24 * 60 * 60 * 1000L)
        val recentWorkouts = logs.count { it.startedTimestamp >= eightWeeksAgo }

        return when {
            // Elite: Very long streak regardless of frequency
            streak >= 16 -> "Elite"

            // Solid: Long streak OR consistent frequency
            streak >= 8 -> "Solid"
            workoutsPerWeek >= 3 && recentWorkouts >= 12 -> "Solid"

            // Building: Moderate streak OR regular training
            streak >= 4 -> "Building"
            workoutsPerWeek >= 1.5 && recentWorkouts >= 6 -> "Building"

            // Inconsistent: Some activity
            streak >= 2 || workoutsPerWeek >= 0.75 -> "Inconsistent"

            else -> "Getting Started"
        }
    }

    private fun detectTrainingFocus(logs: List<WorkoutLogDB>): String {
        if (logs.isEmpty()) return "Exploring"
        val exerciseCounts = logs.flatMap { it.exerciseLogs }
            .groupBy { it.exercise.muscleGroup }
            .mapValues { it.value.size }

        val total = exerciseCounts.values.sum()
        val dominant = exerciseCounts.maxByOrNull { it.value }

        return when {
            dominant != null && dominant.value > total * 0.4 -> "${dominant.key} Focus"
            exerciseCounts.size >= 3 -> "Balanced Training"
            else -> "Building Routine"
        }
    }

    private fun findBestLifts(prs: List<PersonalRecord>): List<BestLift> {
        return prs.groupBy { it.exercise }
            .map { (exercise, records) ->
                val best = records.maxByOrNull { it.reps * it.weight }!!
                BestLift(
                    exercise = exercise.displayName,
                    weight = best.weight,
                    reps = best.reps,
                    date = best.date
                )
            }
            .sortedByDescending { it.weight * it.reps }
    }

    private fun findRisingStars(logs: List<WorkoutLogDB>): List<RisingStar> {
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
        val recentLogs = logs.filter { it.startedTimestamp >= thirtyDaysAgo }
        val olderLogs = logs.filter { it.startedTimestamp < thirtyDaysAgo }

        if (recentLogs.isEmpty() || olderLogs.isEmpty()) return emptyList()

        val recentMaxes = recentLogs.flatMap { it.exerciseLogs }
            .groupBy { it.exercise }
            .mapValues { it.value.maxOfOrNull { log -> log.reps * log.weight } ?: 0 }

        val olderMaxes = olderLogs.flatMap { it.exerciseLogs }
            .groupBy { it.exercise }
            .mapValues { it.value.maxOfOrNull { log -> log.reps * log.weight } ?: 0 }

        return recentMaxes.mapNotNull { (exercise, recentMax) ->
            val oldMax = olderMaxes[exercise] ?: return@mapNotNull null
            if (oldMax > 0) {
                val improvement = ((recentMax - oldMax).toDouble() / oldMax * 100).toInt()
                if (improvement > 5) {
                    RisingStar(exercise.displayName, improvement)
                } else null
            } else null
        }.sortedByDescending { it.improvement }.take(3)
    }

    private fun findGoToExercises(logs: List<WorkoutLogDB>): List<GoToExercise> {
        return logs.flatMap { it.exerciseLogs }
            .groupBy { it.exercise }
            .map { (exercise, logs) ->
                GoToExercise(exercise.displayName, logs.size)
            }
            .sortedByDescending { it.count }
    }

    private fun calculateAverageSessionLength(logs: List<WorkoutLogDB>): String {
        if (logs.isEmpty()) return "0m"
        val avgMinutes = logs.map { (it.finishedTimestamp - it.startedTimestamp) / (60 * 1000) }
            .average().toInt()
        return "${avgMinutes}min"
    }

    private fun findMostActiveDay(logs: List<WorkoutLogDB>): String {
        if (logs.isEmpty()) return ""
        val dayOfWeek = logs.groupBy {
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(it.startedTimestamp),
                ZoneId.systemDefault()
            ).dayOfWeek
        }.maxByOrNull { it.value.size }?.key
        return dayOfWeek?.getDisplayName(TextStyle.FULL, Locale.getDefault()) ?: ""
    }

    private fun findBestTrainingBlock(logs: List<WorkoutLogDB>): String {
        if (logs.size < 4) return "" // Need at least 4 workouts for meaningful block

        val sortedLogs = logs.sortedBy { it.startedTimestamp }

        // Group workouts by week to find consecutive training weeks
        val workoutsByWeek = mutableMapOf<Int, MutableList<WorkoutLogDB>>()

        sortedLogs.forEach { log ->
            val weekNumber = (log.startedTimestamp / (7 * 24 * 60 * 60 * 1000L)).toInt()
            workoutsByWeek.getOrPut(weekNumber) { mutableListOf() }.add(log)
        }

        // Find longest streak of consecutive weeks with workouts
        val sortedWeeks = workoutsByWeek.keys.sorted()
        var maxStreakLength = 0
        var maxStreakStartWeek = 0
        var currentStreakLength = 1
        var currentStreakStartWeek = sortedWeeks.first()

        for (i in 1 until sortedWeeks.size) {
            val prevWeek = sortedWeeks[i - 1]
            val currentWeek = sortedWeeks[i]

            // Check if weeks are consecutive (allowing 1 week gap for flexibility)
            if (currentWeek - prevWeek <= 2) {
                currentStreakLength++
            } else {
                // Streak broken, check if it was the longest
                if (currentStreakLength > maxStreakLength) {
                    maxStreakLength = currentStreakLength
                    maxStreakStartWeek = currentStreakStartWeek
                }
                // Start new streak
                currentStreakLength = 1
                currentStreakStartWeek = currentWeek
            }
        }

        // Check final streak
        if (currentStreakLength > maxStreakLength) {
            maxStreakLength = currentStreakLength
            maxStreakStartWeek = currentStreakStartWeek
        }

        // Need at least 4 consecutive weeks to be considered a "block"
        if (maxStreakLength < 4) return ""

        // Get first and last workout in the best block
        val blockWeeks = (maxStreakStartWeek until maxStreakStartWeek + maxStreakLength)
            .filter { it in workoutsByWeek.keys }

        if (blockWeeks.isEmpty()) return ""

        val firstWorkout = workoutsByWeek[blockWeeks.first()]?.minByOrNull { it.startedTimestamp }
        val lastWorkout = workoutsByWeek[blockWeeks.last()]?.maxByOrNull { it.startedTimestamp }

        if (firstWorkout == null || lastWorkout == null) return ""

        // Calculate total workouts in this block
        val totalWorkoutsInBlock = blockWeeks.sumOf { workoutsByWeek[it]?.size ?: 0 }

        // Format the result
        val startDate = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(firstWorkout.startedTimestamp),
            ZoneId.systemDefault()
        )

        val endDate = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(lastWorkout.startedTimestamp),
            ZoneId.systemDefault()
        )

        val formatter = DateTimeFormatter.ofPattern("MMM yyyy")

        return "${startDate.format(formatter)} to ${endDate.format(formatter)}"
    }

    private fun generateMilestones(
        logs: List<WorkoutLogDB>,
        prs: List<PersonalRecord>,
        streak: Int
    ): List<Milestone> {
        val milestones = mutableListOf<Milestone>()

        // Workout count milestones
        val workoutCounts = listOf(10, 25, 50, 100, 200, 500)
        workoutCounts.forEach { count ->
            if (logs.size >= count) {
                val log = logs.sortedBy { it.startedTimestamp }.getOrNull(count - 1)
                log?.let {
                    milestones.add(
                        Milestone(
                            "🎯 $count Workouts Completed",
                            it.startedTimestamp
                        )
                    )
                }
            }
        }

        // Streak milestones
        if (streak >= 4) milestones.add(
            Milestone(
                "🔥 $streak Week Streak",
                System.currentTimeMillis()
            )
        )

        // PR milestones (first PR for each exercise)
        prs.groupBy { it.exercise }.forEach { (exercise, records) ->
            val first = records.minByOrNull { it.date }
            first?.let {
                milestones.add(
                    Milestone(
                        "🏆 First ${exercise.displayName} PR - ${it.weight}kg",
                        it.date
                    )
                )
            }
        }

        // Exercise specialization (50+ times)
        logs.flatMap { it.exerciseLogs }
            .groupBy { it.exercise }
            .filter { it.value.size >= 50 }
            .forEach { (exercise, _) ->
                milestones.add(
                    Milestone(
                        "💪 ${exercise.displayName} Master - 50 sessions",
                        System.currentTimeMillis()
                    )
                )
            }

        return milestones.sortedByDescending { it.date }.take(10)
    }

    private fun groupWorkoutLogsByDate(logs: List<WorkoutLogDB>): List<WorkoutLogGroup> {
        return logs
            .sortedByDescending { it.startedTimestamp }
            .groupBy {
                LocalDate.ofEpochDay(it.startedTimestamp / (24 * 60 * 60 * 1000))
            }
            .map { (date, logs) ->
                WorkoutLogGroup(date, logs.map { it.toUiModel() })
            }
    }

    fun onEvent(event: ProfileEvents) {
        when (event) {
            is ProfileEvents.OnExpandWorkout -> toggleWorkoutExpansion(event.workoutId)
            ProfileEvents.OnToggleHistoryTab -> _uiState.value = _uiState.value.copy(
                showFullHistory = !_uiState.value.showFullHistory
            )
        }
    }

    private fun toggleWorkoutExpansion(workoutId: Int) {
        val currentExpanded = _uiState.value.expandedWorkoutIds
        _uiState.value = _uiState.value.copy(
            expandedWorkoutIds = if (currentExpanded.contains(workoutId)) {
                currentExpanded - workoutId
            } else {
                currentExpanded + workoutId
            }
        )
    }
}

// Helper functions
fun formatWorkoutDate(date: LocalDate): String {
    val today = LocalDate.now()
    return when {
        date == today -> "Today"
        date == today.minusDays(1) -> "Yesterday"
        date.isAfter(today.minusDays(7)) -> date.format(DateTimeFormatter.ofPattern("EEEE"))
        else -> date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}

fun formatWorkoutTime(timestamp: Long): String {
    val time = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    return time.format(DateTimeFormatter.ofPattern("h:mm a"))
}

fun formatWorkoutDateTime(timestamp: Long): String {
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    val date = dateTime.toLocalDate()
    val today = LocalDate.now()

    val dateStr = when {
        date == today -> "Today"
        date == today.minusDays(1) -> "Yesterday"
        date.isAfter(today.minusDays(7)) -> date.format(DateTimeFormatter.ofPattern("EEEE"))
        else -> date.format(DateTimeFormatter.ofPattern("MMM d"))
    }

    val timeStr = dateTime.format(DateTimeFormatter.ofPattern("h:mm a"))
    return "$dateStr at $timeStr"
}

fun formatMilestoneDate(timestamp: Long): String {
    val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
}

// =================================
// DATA CLASSES & SEALED INTERFACES
// =================================

data class ProfileState(
    val userName: String = "User",
    val userEmail: String = "",
    val profilePicUri: String = "",
    val units: Units = Units.METRIC,

    // Athletic Identity
    val trainingAge: String = "",
    val trainingFrequency: String = "",
    val preferredTrainingTime: String = "",
    val consistencyRating: String = "",
    val trainingFocus: String = "",

    // Strength Profile
    val bestLifts: List<BestLift> = emptyList(),
    val risingStars: List<RisingStar> = emptyList(),

    // Training DNA
    val goToExercises: List<GoToExercise> = emptyList(),
    val averageSessionLength: String = "",
    val mostActiveDay: String = "",
    val bestTrainingBlock: String = "",

    // Milestones
    val milestones: List<Milestone> = emptyList(),

    // History
    val recentWorkouts: List<WorkoutLogDB> = emptyList(),
    val groupedWorkoutLogs: List<WorkoutLogGroup> = emptyList(),
    val showFullHistory: Boolean = false,
    val expandedWorkoutIds: Set<Int> = emptySet()
)

data class BestLift(
    val exercise: String,
    val weight: Double,
    val reps: Int,
    val date: Long
)

data class RisingStar(
    val exercise: String,
    val improvement: Int
)

data class GoToExercise(
    val name: String,
    val count: Int
)

data class Milestone(
    val title: String,
    val date: Long
)

data class WorkoutLogGroup(
    val date: LocalDate,
    val workouts: List<WorkoutLog>
)

sealed interface ProfileEvents {
    data class OnExpandWorkout(val workoutId: Int) : ProfileEvents
    object OnToggleHistoryTab : ProfileEvents
}

enum class Units(val symbol: String) {
    METRIC("kg"),
    IMPERIAL("lbs")
}

// Extension property for GymExercise
val GymExercise.muscleGroup: String
    get() = when (this) {
        GymExercise.BENCH_PRESS, GymExercise.INCLINE_DUMBBELL_PRESS -> "Chest"
        GymExercise.SQUAT, GymExercise.LEG_PRESS -> "Legs"
        GymExercise.DEADLIFT -> "Back"
        GymExercise.PULL_UP, GymExercise.BARBELL_ROW -> "Back"
        GymExercise.OVERHEAD_SHOULDER_PRESS -> "Shoulders"
        else -> "Other"
    }



