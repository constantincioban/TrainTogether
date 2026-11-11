package com.konovus.traintogether.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.konovus.traintogether.data.local.models.WorkoutLog
import com.konovus.traintogether.data.utils.formatMillisToString

@Entity(tableName = "workout_logs")
data class WorkoutLogDB(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val workoutId: Int = 0,
    val exerciseLogs: List<ExerciseLogDB> = listOf(),
    val startedTimestamp: Long = 0L,
    val finishedTimestamp: Long = 0L,
) {
    fun toUiModel() = WorkoutLog(
        id = id,
        workoutId = workoutId,
        exerciseLogs = exerciseLogs,
        startedTimestamp = formatMillisToString(startedTimestamp),
        finishedTimestamp = formatMillisToString(finishedTimestamp)
    )
}