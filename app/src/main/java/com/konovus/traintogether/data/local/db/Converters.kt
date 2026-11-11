package com.konovus.traintogether.data.local.db

import androidx.room.TypeConverter
import com.konovus.traintogether.data.local.entities.ExerciseDB
import com.konovus.traintogether.data.local.entities.ExerciseLogDB
import com.konovus.traintogether.data.utils.GymExercise
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.Units

class Converters {
    @TypeConverter
    fun fromExerciseList(exercises: List<ExerciseDB>): String {
        return exercises.joinToString(separator = "|||") { exercise ->
            "${exercise.id},${exercise.gymExercise.name},${exercise.warmUpSets},${exercise.workingSets},${exercise.hasCircuitNext},${exercise.hasCircuitPrev}"
        }
    }

    @TypeConverter
    fun toExerciseList(data: String): List<ExerciseDB> {
        if (data.isEmpty()) return emptyList()
        return data.split("|||").map { exerciseStr ->
            val parts = exerciseStr.split(",")
            ExerciseDB(
                id = parts[0].toInt(),
                gymExercise = GymExercise.valueOf(parts[1]),
                warmUpSets = parts[2].toInt(),
                workingSets = parts[3].toInt(),
                hasCircuitNext = parts[4].toBoolean(),
                hasCircuitPrev = parts[5].toBoolean()
            )
        }
    }

    @TypeConverter
    fun fromExerciseLogList(logs: List<ExerciseLogDB>): String {
        return logs.joinToString(separator = "|||") { log ->
            "${log.exercise.name},${log.set},${log.reps},${log.weight}"
        }
    }

    @TypeConverter
    fun toExerciseLogList(data: String): List<ExerciseLogDB> {
        if (data.isEmpty()) return emptyList()
        return data.split("|||").map { logStr ->
            val parts = logStr.split(",")
            ExerciseLogDB(
                exercise = GymExercise.valueOf(parts[0]),
                set = parts[1],
                reps = parts[2].toInt(),
                weight = parts[3].toInt()
            )
        }
    }

    @TypeConverter
    fun fromGymExercise(exercise: GymExercise): String = exercise.name

    @TypeConverter
    fun toGymExercise(data: String): GymExercise = GymExercise.valueOf(data)

    @TypeConverter
    fun fromUnits(units: Units): String = units.name

    @TypeConverter
    fun toUnits(data: String): Units = Units.valueOf(data)
}