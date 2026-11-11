package com.konovus.traintogether.data.local.models

import androidx.compose.runtime.Immutable
import com.konovus.traintogether.data.local.entities.WorkoutDB

@Immutable
data class Workout(
    val id: Int = -1,
    val title: String = "",
    val exercises: List<Exercise> = emptyList(),
) {
    fun toDbEntity() = WorkoutDB(
        id = id,
        title = title,
        exercises = exercises.map { it.toDbEntity() },
    )
}
