package com.konovus.traintogether.data.local.models

import com.konovus.traintogether.data.local.entities.ExerciseLogDB
import com.konovus.traintogether.data.local.entities.WorkoutLogDB
import com.konovus.traintogether.data.utils.formatStringToMillis

data class WorkoutLog(
    val id: Int = 0,
    val workoutId: Int = 0,
    val exerciseLogs: List<ExerciseLogDB> = emptyList(),
    val startedTimestamp: String = "",
    val finishedTimestamp: String = "",
) {
    fun toDbEntity() = WorkoutLogDB(
        id = id,
        workoutId = workoutId,
        exerciseLogs = exerciseLogs,
        startedTimestamp = formatStringToMillis(startedTimestamp),
        finishedTimestamp = formatStringToMillis(finishedTimestamp),
    )
}
