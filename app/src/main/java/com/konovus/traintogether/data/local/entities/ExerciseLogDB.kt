package com.konovus.traintogether.data.local.entities

import com.konovus.traintogether.data.utils.GymExercise

data class ExerciseLogDB(
    val exercise: GymExercise = GymExercise.BENCH_PRESS,
    val set: String = "",
    val reps: Int = 0,
    val weight: Int = 0,
)
