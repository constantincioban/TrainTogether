package com.konovus.traintogether.data.local.entities

import com.konovus.traintogether.data.local.models.Exercise
import com.konovus.traintogether.data.utils.GymExercise

data class ExerciseDB(
    val id: Int = 0,
    val gymExercise: GymExercise = GymExercise.BENCH_PRESS,
    val warmUpSets: Int = 0,
    val workingSets: Int = 0,
    val hasCircuitNext: Boolean = false,
    val hasCircuitPrev: Boolean = false,
) {
    fun toUiModel() = Exercise(
        gymExercise = gymExercise,
        warmUpSets = warmUpSets.toString(),
        workingSets = workingSets.toString(),
        hasCircuitNext = hasCircuitNext,
        hasCircuitPrev = hasCircuitPrev,

        )
}