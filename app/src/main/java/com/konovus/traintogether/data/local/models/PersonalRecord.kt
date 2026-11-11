package com.konovus.traintogether.data.local.models

import com.konovus.traintogether.data.local.entities.PersonalRecordDB
import com.konovus.traintogether.data.utils.GymExercise
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.Units

data class PersonalRecord(
    val id: Int = 0,
    val exercise: GymExercise = GymExercise.BENCH_PRESS,
    val reps: Int = 0,
    val weight: Double = 0.0,
    val unit: Units = Units.METRIC,
    val date: Long = System.currentTimeMillis(),
) {
    fun toDbEntity() = PersonalRecordDB(
        id = id,
        exercise = exercise,
        reps = reps,
        weight = weight,
        date = date,
        unit = unit,
    )
}