package com.konovus.traintogether.data.utils

enum class GymExercise(val displayName: String) {
    BENCH_PRESS("Bench Press"),
    SQUAT("Squat"),
    DEADLIFT("Deadlift"),
    PULL_UP("Pull-Up"),
    BICEP_PULL_UP("Bicep Pull-Up"),
    PUSH_UP("Push-Up"),
    OVERHEAD_SHOULDER_PRESS("Overhead Shoulder Press"),
    BARBELL_ROW("Barbell Row"),
    BICEP_CURL("Bicep Curl"),
    TRICEP_DIP("Tricep Dip"),
    LAT_PULLDOWN("Lat Pulldown"),
    LEG_PRESS("Leg Press"),
    LUNGES("Lunges"),
    CABLE_TRICEP_PUSHDOWN("Cable Tricep Pushdown"),
    HAMMER_CURL("Hammer Curl"),
    FACE_PULL("Face Pull"),
    INCLINE_DUMBBELL_PRESS("Incline Dumbbell Press"),
    SEATED_ROW("Seated Row"),
    CHEST_FLY("Chest Fly"),
    LEG_CURL("Leg Curl"),
    LEG_EXTENSION("Leg Extension");

    override fun toString(): String = displayName
}
