package com.konovus.traintogether.presentation.redux

import com.konovus.traintogether.data.local.entities.ExerciseLogDB
import com.konovus.traintogether.data.local.entities.WorkoutLogDB
import com.konovus.traintogether.data.local.models.Exercise
import com.konovus.traintogether.data.local.models.PersonalRecord
import com.konovus.traintogether.data.local.models.UserProfile
import com.konovus.traintogether.data.local.models.Workout
import com.konovus.traintogether.data.utils.ImmutableList
import com.konovus.traintogether.data.utils.WorkoutFrequency
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.AppTheme
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.Units


data class AppState(
    val theme: AppTheme = AppTheme.DARK,
    val isLoggedIn: Boolean = false,
    val userEmail: String = "",
    val workouts: List<Workout> = emptyList(),
    val isStopWatchRunning: Boolean = false,
    val isForegroundServiceOn: Boolean = false,
    val newWorkoutLog: Boolean = false,
    val workoutLogs: List<WorkoutLogDB> = emptyList(),
    val personalRecords: List<PersonalRecord> = emptyList(),
    val units: Units = Units.METRIC,
    val streak: Int = 0,
    val workoutFrequency: WorkoutFrequency = WorkoutFrequency.ONCE_A_WEEK,
    val workoutState: ActiveWorkoutState = ActiveWorkoutState(),
    val userProfile: UserProfile = UserProfile(),
    )

data class ActiveWorkoutState(
    val workoutId: Int = 0,
    val currentSetNumber: Int = 1,
    val currentExerciseIndex: Int = 0,
    val workoutIsRunning: Boolean = false,
    val workoutTitle: String = "",
    val currentExercise: Exercise = Exercise(),
    val currentSet: String = "",
    val stopWatchTime: String = "00:00",
    val exerciseLogs: ImmutableList<ExerciseLogDB> = ImmutableList(),
    val reps: Int = 0,
    val weight: Int = 0,
    val startTimestamp: Long = 0L,
    val totalTime: String = "0h 0m",
    val startFinishBtnText: String = "Start",
    )
