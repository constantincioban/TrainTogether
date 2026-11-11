package com.konovus.traintogether.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.konovus.traintogether.data.local.models.Workout

@Entity(tableName = "workouts")
data class WorkoutDB(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val exercises: List<ExerciseDB> = emptyList(),
) {
    fun toUiModel(): Workout {
        return Workout(
            id = id,
            title = title,
            exercises = exercises.map { it.toUiModel() },
        )
    }
}