package com.konovus.traintogether.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.konovus.traintogether.data.local.models.PersonalRecord
import com.konovus.traintogether.data.utils.GymExercise
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.Units

@Entity(tableName = "personal_records")
data class PersonalRecordDB(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val exercise: GymExercise = GymExercise.BENCH_PRESS,
    val reps: Int = 0,
    val weight: Double = 0.0,
    val date: Long = 0L,
    val unit: Units = Units.METRIC,
) {
    fun toUiModel() = PersonalRecord(
        id = id,
        exercise = exercise,
        reps = reps,
        weight = weight,
        date = date,
        unit = unit,
    )
}