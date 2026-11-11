package com.konovus.traintogether.data.utils

import com.konovus.traintogether.data.local.entities.WorkoutLogDB
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class WorkoutFrequency(val displayName: String, val timesPerWeek: Int) {
    ONCE_A_WEEK("Once a week", 1),
    TWICE_A_WEEK("Twice a week", 2),
    THREE_TIMES("3x per week", 3),
    FOUR_TIMES("4x per week", 4),
    FIVE_TIMES("5x per week", 5),
    SIX_TIMES("6x per week", 6),
    DAILY("Daily", 7),
    TWICE_A_DAY("Twice a day", 14),
    THREE_TIMES_A_DAY("3x per day", 21),
}

enum class PeriodType(val displayName: String) {
    DAY("Day"),
    WEEK("Week"),
}

private fun WorkoutFrequency.getPeriodType(): PeriodType {
    return when (this) {
        WorkoutFrequency.DAILY,
        WorkoutFrequency.TWICE_A_DAY,
        WorkoutFrequency.THREE_TIMES_A_DAY -> PeriodType.DAY
        else -> PeriodType.WEEK
    }
}

data class StreakResult(
    val currentStreak: Int,
    val longestStreak: Int,
    val currentPeriodCount: Int,
    val isCurrentPeriodComplete: Boolean
)
class StreakCalculator(
    private val timeZone: ZoneId = ZoneId.systemDefault()
) {

    fun calculateStreak(
        workoutLogs: List<WorkoutLogDB>,
        goal: WorkoutFrequency
    ): StreakResult {
        if (workoutLogs.isEmpty()) {
            return StreakResult(0, 0, 0, false)
        }

        val workoutDates = extractWorkoutDates(workoutLogs)
        val periodType = goal.getPeriodType()

        return when (periodType) {
            PeriodType.DAY -> calculateDailyStreak(workoutDates, goal)
            PeriodType.WEEK -> calculateRollingWeeklyStreak(workoutDates, goal)
        }
    }

    private fun extractWorkoutDates(workoutLogs: List<WorkoutLogDB>): List<LocalDate> {
        return workoutLogs
            .map { log ->
                Instant.ofEpochMilli(log.startedTimestamp)
                    .atZone(timeZone)
                    .toLocalDate()
            }
            .sorted()
    }

    private fun calculateDailyStreak(
        workoutDates: List<LocalDate>,
        goal: WorkoutFrequency
    ): StreakResult {
        val targetPerDay = goal.timesPerWeek / 7
        val today = LocalDate.now(timeZone)

        // Group by day and count workouts per day
        val workoutsByDay = workoutDates.groupBy { it }.mapValues { it.value.size }

        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0

        // Calculate current streak (working backwards from today)
        var checkDate = today
        val firstDay = workoutDates.firstOrNull() ?: today

        while (!checkDate.isBefore(firstDay)) {
            val workoutCount = workoutsByDay[checkDate] ?: 0
            val goalMet = workoutCount >= targetPerDay

            if (goalMet) {
                currentStreak++
            } else {
                break
            }

            checkDate = checkDate.minusDays(1)
        }

        // Calculate longest streak (forward pass)
        checkDate = firstDay
        while (!checkDate.isAfter(today)) {
            val workoutCount = workoutsByDay[checkDate] ?: 0
            val goalMet = workoutCount >= targetPerDay

            if (goalMet) {
                tempStreak++
                longestStreak = maxOf(longestStreak, tempStreak)
            } else {
                tempStreak = 0
            }

            checkDate = checkDate.plusDays(1)
        }

        val todayWorkouts = workoutsByDay[today] ?: 0

        return StreakResult(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            currentPeriodCount = todayWorkouts,
            isCurrentPeriodComplete = todayWorkouts >= targetPerDay
        )
    }

    private fun calculateRollingWeeklyStreak(
        workoutDates: List<LocalDate>,
        goal: WorkoutFrequency
    ): StreakResult {
        val targetPerWeek = goal.timesPerWeek
        val today = LocalDate.now(timeZone)

        if (workoutDates.isEmpty()) {
            return StreakResult(0, 0, 0, false)
        }

        val firstWorkout = workoutDates.first()

        // Calculate by checking consecutive 7-day periods, not every single day
        val weeklyPeriods = generateWeeklyPeriods(firstWorkout, today)

        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0

        // Calculate longest streak first (forward pass)
        for (weekStart in weeklyPeriods) {
            val weekEnd = weekStart.plusDays(6)
            val workoutsInWeek = workoutDates.count { it >= weekStart && it <= weekEnd }

            if (workoutsInWeek >= targetPerWeek) {
                tempStreak++
                longestStreak = maxOf(longestStreak, tempStreak)
            } else {
                tempStreak = 0
            }
        }

        // Calculate current streak (working backwards from most recent complete period)
        val reversedPeriods = weeklyPeriods.reversed()
        for (weekStart in reversedPeriods) {
            val weekEnd = weekStart.plusDays(6)
            val workoutsInWeek = workoutDates.count { it >= weekStart && it <= weekEnd }
            val isCurrentWeek = weekEnd >= today && weekStart <= today

            val goalMet = if (isCurrentWeek) {
                // For current week, check if goal can still be achieved
                canStillMeetWeeklyGoal(workoutsInWeek, targetPerWeek, weekEnd)
            } else {
                workoutsInWeek >= targetPerWeek
            }

            if (goalMet) {
                currentStreak++
            } else {
                break
            }
        }

        // Current 7-day window stats (last 7 days including today)
        val currentWindowStart = today.minusDays(6)
        val currentWindowWorkouts = workoutDates.count { it >= currentWindowStart && it <= today }

        return StreakResult(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            currentPeriodCount = currentWindowWorkouts,
            isCurrentPeriodComplete = currentWindowWorkouts >= targetPerWeek
        )
    }

    private fun generateWeeklyPeriods(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val periods = mutableListOf<LocalDate>()
        var current = startDate

        while (current.plusDays(6) <= endDate) {
            periods.add(current)
            current = current.plusDays(7)
        }

        // Add the final partial week if it contains today
        if (current <= endDate) {
            periods.add(current)
        }

        return periods
    }

    private fun canStillMeetWeeklyGoal(
        currentCount: Int,
        targetCount: Int,
        windowEndDate: LocalDate
    ): Boolean {
        if (currentCount >= targetCount) return true

        val today = LocalDate.now(timeZone)

        // If the week is already complete (ended before today), check if goal was met
        if (windowEndDate < today) {
            return currentCount >= targetCount
        }

        // If the week is still ongoing, check if goal can still be achieved
        val remainingWorkouts = targetCount - currentCount
        val remainingDays = windowEndDate.toEpochDay() - today.toEpochDay() + 1

        return remainingWorkouts <= remainingDays
    }
}

// Extension function for easier usage
fun List<WorkoutLogDB>.calculateStreak(
    goal: WorkoutFrequency,
    timeZone: ZoneId = ZoneId.systemDefault()
): StreakResult {
    return StreakCalculator(timeZone).calculateStreak(this, goal)
}