package com.konovus.traintogether.data.local.models

import androidx.compose.runtime.Immutable
import com.konovus.traintogether.data.local.entities.ExerciseDB
import com.konovus.traintogether.data.utils.GymExercise

@Immutable
data class Exercise(
    val id: Int = 0,
    val gymExercise: GymExercise = GymExercise.BENCH_PRESS,
    val warmUpSets: String = "",
    val workingSets: String = "",
    val hasCircuitNext: Boolean = false,
    val hasCircuitPrev: Boolean = false,
) {
    fun toDbEntity() = ExerciseDB(
        gymExercise = gymExercise,
        warmUpSets = warmUpSets.toInt(),
        workingSets = workingSets.toInt(),
        hasCircuitNext = hasCircuitNext,
        hasCircuitPrev = hasCircuitPrev,
    )
}
