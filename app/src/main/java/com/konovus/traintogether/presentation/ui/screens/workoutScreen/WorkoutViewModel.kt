package com.konovus.traintogether.presentation.ui.screens.workoutScreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konovus.traintogether.data.local.db.ILocalDBRepository
import com.konovus.traintogether.data.local.entities.ExerciseLogDB
import com.konovus.traintogether.data.local.entities.WorkoutLogDB
import com.konovus.traintogether.data.local.models.Exercise
import com.konovus.traintogether.data.local.models.PersonalRecord
import com.konovus.traintogether.data.local.models.Workout
import com.konovus.traintogether.data.local.models.WorkoutLog
import com.konovus.traintogether.data.remote.IWorkoutRepository
import com.konovus.traintogether.data.utils.ImmutableList
import com.konovus.traintogether.data.utils.StopWatchImpl
import com.konovus.traintogether.data.utils.formatDurationFromMillis
import com.konovus.traintogether.data.utils.formatMillisToReadableTime
import com.konovus.traintogether.navigation.Screens
import com.konovus.traintogether.presentation.redux.ActiveWorkoutState
import com.konovus.traintogether.presentation.redux.AppState
import com.konovus.traintogether.presentation.redux.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val remoteDb: IWorkoutRepository,
    private val localDb: ILocalDBRepository,
    private val store: Store<AppState>,
    private val stopWatch: StopWatchImpl,

    ) : ViewModel() {

    private val _uiState =
        mutableStateOf(WorkoutState())

    val uiState = _uiState

    private var messageIsActive = false
    private val _eventChannel = Channel<String>(0)
    val eventChannel = _eventChannel.receiveAsFlow()


    private var currentSet = 1

    private var currentExerciseIndex = 0
    private var startTimestamp: Long = 0L
    private var endTimestamp: Long = 0L
    private val exercises: MutableList<Exercise> = mutableListOf()
    private var isFinishing = false

    init {
        viewModelScope.launch {
            savedStateHandle.get<Int>(Screens.Workout.workoutIdArg)?.let { workoutId ->
                if (workoutId == 0) return@let

                currentSet = store.state.value.workoutState.currentSetNumber
                currentExerciseIndex = store.state.value.workoutState.currentExerciseIndex

                store.state.value.workouts.find { it.id == workoutId }?.let {
                    exercises.addAll(it.exercises.toMutableList())
                    val workoutState = store.state.value.workoutState
                    val currentExercise = if (workoutState.workoutIsRunning)
                        workoutState.currentExercise else it.exercises.first()
                    _uiState.value = _uiState.value.copy(
                        workout = it,
                        currentExercise = currentExercise,
                    )

                    if (store.state.value.workoutState.workoutId == workoutId) {
                        _uiState.value = _uiState.value.copy(
                            workoutIsRunning = store.state.value.workoutState.workoutIsRunning,
                            currentExercise = store.state.value.workoutState.currentExercise,
                            totalTime = store.state.value.workoutState.totalTime,
                            currentSet = store.state.value.workoutState.currentSet,
                            reps = store.state.value.workoutState.reps,
                            weight = store.state.value.workoutState.weight,
                            startFinishBtnText = store.state.value.workoutState.startFinishBtnText,
                            exerciseLogs = store.state.value.workoutState.exerciseLogs,
                        )
                    }

                }

                _uiState.value = _uiState.value.copy(
                    previousWorkoutLog = store.state.value.workoutLogs.filter { it.toUiModel().workoutId == workoutId }
                        .ifEmpty { listOf(WorkoutLogDB()) }
                        .maxBy { it.startedTimestamp }.toUiModel(),
                )
            }


        }
        collectStopWatchState()
        calculateTotalWorkoutTime()
    }


    private fun calculateTotalWorkoutTime() = viewModelScope.launch {
        snapshotFlow { _uiState.value.workoutIsRunning }
            .distinctUntilChanged()
            .collect { isRunning ->
                if (isRunning)
                    while (true) {
                        delay(1000)
                        val timeString = formatMillisToReadableTime(System.currentTimeMillis() - startTimestamp)
                        _uiState.value = _uiState.value.copy(totalTime = timeString)
                    }
            }
    }

    private fun collectStopWatchState() = viewModelScope.launch {
        stopWatch.state.collect { stopWatchState ->
            if (_uiState.value.workout.id != store.state.value.workoutState.workoutId) {
                return@collect
            }

            _uiState.value = _uiState.value.copy(
                stopWatchTime = formatDurationFromMillis(stopWatchState.time),
                stopWatchIcon = if (stopWatchState.isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                isStopWatchRunning = stopWatchState.isRunning,
                isEditButtonEnabled = !stopWatchState.isRunning
            )

            store.update {
                it.copy(
                    workoutState = ActiveWorkoutState(
                        workoutId = _uiState.value.workout.id,
                        currentSetNumber = currentSet,
                        currentExerciseIndex = currentExerciseIndex,
                        stopWatchTime = _uiState.value.stopWatchTime,
                        workoutIsRunning = stopWatchState.isRunning,
                        workoutTitle = _uiState.value.workout.title,
                        currentExercise = _uiState.value.currentExercise,
                        currentSet = _uiState.value.currentSet,
                        totalTime = _uiState.value.totalTime,
                        reps = _uiState.value.reps,
                        weight = _uiState.value.weight,
                        startFinishBtnText = _uiState.value.startFinishBtnText,
                        exerciseLogs = _uiState.value.exerciseLogs,
                    )
                )
            }

            // Only set startTimestamp if it's the first time (workout start)
            if (startTimestamp == 0L) {
                startTimestamp = stopWatchState.startTimestamp
            }


        }
    }

    fun onEvent(event: WorkoutEvent) {
        when (event) {
            is WorkoutEvent.StartWorkout -> startWorkout()
            is WorkoutEvent.FinishWorkout -> finishWorkout()
            WorkoutEvent.OnSetDone -> setDone()
            WorkoutEvent.OnSetNext -> setNext()
            is WorkoutEvent.OnUpdateReps -> updateReps(event.reps)
            is WorkoutEvent.OnUpdateWeight -> updateWeight(event.weight)
            is WorkoutEvent.OnMoodSelected -> updateSelectedMood(event.mood)
            WorkoutEvent.OnNavigateBack -> navigateBack()
            WorkoutEvent.OnTogglePRCongratsDialog -> handleOnTogglePRCongratsDialog()
            is WorkoutEvent.OnToggleEditWorkout -> handleToggleOnEditWorkout()
            WorkoutEvent.OnToggleIsPublic -> handleToggleIsPublic()
            is WorkoutEvent.OnCancelWorkout -> handleCancelWorkout(event.saveWorkout)
            WorkoutEvent.OnToggleCancelWorkoutDialog -> handleToggleCancelWorkoutDialog()
        }
    }

    private fun handleToggleCancelWorkoutDialog() {
        _uiState.value =
            _uiState.value.copy(showCancelWorkoutDialog = !_uiState.value.showCancelWorkoutDialog)
    }

    private fun handleCancelWorkout(saveWorkout: Boolean) = viewModelScope.launch {
        stopWatch.pause()

        if (saveWorkout && _uiState.value.exerciseLogs.isNotEmpty()) {
            // Save the workout with current progress
            endTimestamp = System.currentTimeMillis()

            val newWorkoutLog = WorkoutLogDB(
                id = store.state.value.workoutLogs.maxOfOrNull { it.id }?.plus(1) ?: 1,
                workoutId = _uiState.value.workout.id,
                exerciseLogs = _uiState.value.exerciseLogs,
                startedTimestamp = startTimestamp,
                finishedTimestamp = endTimestamp,
            )
            localDb.insertWorkoutLog(newWorkoutLog)
            remoteDb.saveWorkoutLog(
                store.state.value.userProfile.id,
                workoutLog = newWorkoutLog
            )
            _uiState.value = _uiState.value.copy(isSaved = true)
        }

        // Reset workout state
        _uiState.value = _uiState.value.copy(
            workoutIsRunning = false,
            workoutIsFinished = true,
            totalTime = "0h 0m",
            navigateBack = if (_uiState.value.isSaved) false else true,
            showCancelWorkoutDialog = false,
        )
        store.update { it.copy(isStopWatchRunning = false, workoutState = ActiveWorkoutState()) }
        stopWatch.reset()
    }

    private fun handleToggleIsPublic() {
        _uiState.value = _uiState.value.copy(isPublic = !_uiState.value.isPublic)
    }

    private fun handleToggleOnEditWorkout() {
        if (!_uiState.value.workoutIsRunning)
            _uiState.value = _uiState.value.copy(editWorkout = !_uiState.value.editWorkout)
        else sendUiEvent(ToastMessage.CannotEditDuringActiveWorkout)
    }

    private fun handleOnTogglePRCongratsDialog() {
        _uiState.value = _uiState.value.copy(newPR = !_uiState.value.newPR)
    }

    private fun navigateBack() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(navigateBack = !_uiState.value.navigateBack)
//        store.update { it.copy(isStopWatchRunning = false) }
    }

    private fun updateSelectedMood(mood: WorkoutMood) {
        _uiState.value = _uiState.value.copy(mood = mood)
    }

    private fun updateReps(reps: Int) {
        _uiState.value = _uiState.value.copy(reps = reps)
    }

    private fun updateWeight(weight: Int) {
        _uiState.value = _uiState.value.copy(weight = weight)
    }

    private fun setDone() = viewModelScope.launch {

        val currentExercise = _uiState.value.currentExercise
        val warmUpCount = currentExercise.warmUpSets.toInt()
        val workingCount = currentExercise.workingSets.toInt()
        val totalSets = warmUpCount + workingCount
        val isLastSet = currentSet == totalSets
        val isLastExercise = currentExerciseIndex == exercises.size - 1

        _uiState.value = _uiState.value.copy(
            exerciseLogs = ImmutableList(
                _uiState.value.exerciseLogs +
                        ExerciseLogDB(
                            exercise = currentExercise.gymExercise,
                            set = _uiState.value.currentSet,
                            reps = _uiState.value.reps,
                            weight = _uiState.value.weight
                        )
            ),
        )

        // checking if current set is a Personal Record
        if (store.state.value.personalRecords.isNotEmpty() &&
            store.state.value.personalRecords.map { it.exercise }
                .contains(currentExercise.gymExercise) &&
            store.state.value.personalRecords.filter { it.exercise == currentExercise.gymExercise }
                .map { it.reps * it.weight }.max()
            < (_uiState.value.reps * _uiState.value.weight)
        ) {
            val newPR = PersonalRecord(
                id = store.state.value.personalRecords.maxOf { it.id } + 1,
                exercise = currentExercise.gymExercise,
                reps = _uiState.value.reps,
                weight = _uiState.value.weight.toDouble(),
                date = System.currentTimeMillis(),
                unit = store.state.value.units,
            )

            localDb.insertPersonalRecord(newPR.toDbEntity())

            remoteDb.savePersonalRecord(
                userId = store.state.value.userProfile.id,
                pr = newPR.toDbEntity(),
            )
            store.update { it.copy(personalRecords = store.state.value.personalRecords + newPR) }
            _uiState.value = _uiState.value.copy(newPR = true, pr = newPR)
        }

        if (isLastSet && isLastExercise) {
            finishWorkout()
            return@launch
        }

        stopWatch.reset()
        stopWatch.start()

        if (currentExercise.hasCircuitNext) {
            currentExerciseIndex++
            val nextExercise = exercises[currentExerciseIndex]
            val nextExerciseTotalSets =
                nextExercise.warmUpSets.toInt() + nextExercise.workingSets.toInt()
            if (currentSet <= nextExerciseTotalSets) {
                _uiState.value = _uiState.value.copy(currentExercise = nextExercise)
            } else {
                currentSet = 1
            }
        } else if (currentExercise.hasCircuitPrev) {
            currentSet++
            val prevExercise = findStartOfCircuitBackwards(
                exercises,
                currentExerciseIndex
            )?.first
            if (prevExercise == null) {
                _uiState.value =
                    _uiState.value.copy(currentExercise = exercises.first())
                return@launch
            }
            val prevExerciseTotalSets =
                prevExercise.warmUpSets.toInt() + prevExercise.workingSets.toInt()
            if (currentSet <= prevExerciseTotalSets) {
                _uiState.value = _uiState.value.copy(currentExercise = prevExercise)
                currentExerciseIndex = findStartOfCircuitBackwards(
                    exercises,
                    currentExerciseIndex
                )?.second ?: 0
            } else {
                currentSet = 1
                if (exercises.size > currentExerciseIndex) {
                    currentExerciseIndex++
                    _uiState.value =
                        _uiState.value.copy(currentExercise = exercises[currentExerciseIndex])
                }
            }
        } else if (currentSet < totalSets) {
            currentSet++
        } else {
            currentSet = 1
            currentExerciseIndex++
            _uiState.value =
                _uiState.value.copy(currentExercise = exercises[currentExerciseIndex])
        }



        updateCurrentSetLabel()
    }

    fun findStartOfCircuitBackwards(exercises: List<Exercise>, pos: Int): Pair<Exercise, Int>? {
        if (pos !in exercises.indices) return null

        var index = pos

        // Move backwards while the previous item has hasCircuitNext == true
        while (index > 0 && exercises[index - 1].hasCircuitNext) {
            index--
        }

        return exercises.getOrNull(index)?.let { it to index }
    }


    fun findEndOfCircuitForwards(exercises: List<Exercise>, pos: Int): Pair<Exercise, Int>? {
        if (pos !in exercises.indices) return null

        var index = pos

        // Move forward while the current item has hasCircuitNext == true
        while (index < exercises.lastIndex && exercises[index].hasCircuitNext) {
            index++
        }

        return exercises.getOrNull(index)?.let { it to index }
    }


    private fun updateCurrentSetLabel() {
        val exercise = _uiState.value.currentExercise
        val warmUpCount = exercise.warmUpSets.toInt()
        val workingCount = exercise.workingSets.toInt()
        val isWarmUp = currentSet <= warmUpCount

        val setLabel = if (isWarmUp)
            "Warm-up $currentSet"
        else
            "Working set ${currentSet - warmUpCount}"

        val isLastSet = currentSet == warmUpCount + workingCount
        val isLastExercise = currentExerciseIndex == exercises.lastIndex

        val buttonText = if (isLastSet && isLastExercise) "Finish"
        else if (_uiState.value.exerciseLogs.isEmpty() && startTimestamp == 0L)
            "Start" else "Set Done"

        _uiState.value = _uiState.value.copy(
            currentSet = setLabel,
            startFinishBtnText = buttonText,
            reps = _uiState.value.previousWorkoutLog.exerciseLogs
                .filter { it.exercise == exercise.gymExercise }.find { it.set == setLabel }?.reps
                ?: 0,
            weight = _uiState.value.previousWorkoutLog.exerciseLogs
                .filter { it.exercise == exercise.gymExercise }.find { it.set == setLabel }?.weight
                ?: 0,
        )
        println("Reps = ${uiState.value.reps} , Weight = ${uiState.value.weight}")
    }


    private fun setNext() {
        val currentExercise = _uiState.value.currentExercise

        val warmUpCount = currentExercise.warmUpSets.toInt()
        val workingCount = currentExercise.workingSets.toInt()
        val totalSets = warmUpCount + workingCount
        val isLastSet = currentSet == totalSets
        val isLastExercise = currentExerciseIndex == exercises.lastIndex

        if ((isLastSet && isLastExercise) || store.state.value.workoutState.workoutIsRunning) {
            return
        }

        if (_uiState.value.currentSet == "Warm-up 1"
            && currentExerciseIndex < exercises.size - 1
            && _uiState.value.exerciseLogs.map { it.exercise }.distinct().size < exercises.size
            && !exercises[currentExerciseIndex].hasCircuitPrev // im checking to see if i'm not past the first exercise in a circuit
        ) {
            if (exercises[currentExerciseIndex].hasCircuitNext) {
                findEndOfCircuitForwards(exercises, currentExerciseIndex)?.second?.let {
                    var circuitExercises = mutableListOf<Exercise>()
                    for (i in currentExerciseIndex..it) {
                        circuitExercises.add(exercises[i])
                    }
                    circuitExercises.forEach { exercises.remove(it) }
                    exercises.addAll(circuitExercises)
                }
            } else {
                val exercise = exercises.removeAt(currentExerciseIndex)
                exercises.add(exercise)

            }
            _uiState.value = _uiState.value.copy(currentExercise = exercises[currentExerciseIndex])
        } else {
            currentExerciseIndex = 1
        }

        updateCurrentSetLabel()
    }


    private fun startWorkout() {
        if (store.state.value.workoutState.workoutIsRunning &&
            _uiState.value.workout.id != store.state.value.workoutState.workoutId
        ) {
            sendUiEvent(ToastMessage.OneWorkoutIsAlreadyActive)
            return
        }

        // Set the workout start timestamp only when starting the workout
        if (startTimestamp == 0L) {
            startTimestamp = System.currentTimeMillis()
        }

        if (!_uiState.value.workoutIsRunning) {
            _uiState.value = _uiState.value.copy(
                workoutIsRunning = true,
                startFinishBtnText = "Set Done",
            )
            updateCurrentSetLabel()
            stopWatch.start()
            viewModelScope.launch {
                store.update {
                    it.copy(
                        isStopWatchRunning = true,
                        workoutState = ActiveWorkoutState(
                            workoutId = _uiState.value.workout.id,
                            stopWatchTime = _uiState.value.stopWatchTime,
                            workoutIsRunning = _uiState.value.workoutIsRunning,
                            workoutTitle = _uiState.value.workout.title,
                            currentExercise = _uiState.value.currentExercise,
                            currentSet = _uiState.value.currentSet,
                        )
                    )
                }
            }
        } else if (currentExerciseIndex <= exercises.lastIndex || currentSet <
            (_uiState.value.currentExercise.warmUpSets.toInt() + _uiState.value.currentExercise.workingSets.toInt())) {
            setDone()
        } else {
            finishWorkout()
        }
    }


    private fun finishWorkout() = viewModelScope.launch {

        if (isFinishing) return@launch
        isFinishing = true

        try {
            stopWatch.pause()
            endTimestamp = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(workoutIsFinished = true)
            store.update { it.copy(isStopWatchRunning = false, workoutState = ActiveWorkoutState()) }

            val newWorkoutLog = WorkoutLogDB(
                id = (store.state.value.workoutLogs.maxOfOrNull { it.id } ?: 0) + 1,
                workoutId = _uiState.value.workout.id,
                exerciseLogs = _uiState.value.exerciseLogs,
                startedTimestamp = startTimestamp,
                finishedTimestamp = endTimestamp,
            )
            localDb.insertWorkoutLog(newWorkoutLog)
            remoteDb.saveWorkoutLog(
                store.state.value.userProfile.id,
                workoutLog = newWorkoutLog
            )
            _uiState.value = _uiState.value.copy(isSaved = true)

            stopWatch.reset()
        } finally {
            isFinishing = false
        }
    }

    private fun sendUiEvent(toast: ToastMessage) = viewModelScope.launch {
        if (!messageIsActive) {
            _eventChannel.send(toast.message)
            messageIsActive = true
            delay(3500)
            messageIsActive = false
        }
    }

    sealed class ToastMessage(
        val message: String,
    ) {

        object CannotEditDuringActiveWorkout : ToastMessage(
            "Cannot edit during active workout"
        )

        object OneWorkoutIsAlreadyActive : ToastMessage(
            "One workout is already active"
        )

        object CannotSkipExerciseIfStarted : ToastMessage(
            "Cannot skip exercise if started"
        )

    }


}

data class WorkoutState(
    val workout: Workout = Workout(),
    val totalTime: String = "0h 0m",
    val workoutIsRunning: Boolean = false,
    val currentSet: String = "Warm-up 1",
    val exerciseLogs: ImmutableList<ExerciseLogDB> = ImmutableList(),
    val currentExercise: Exercise = Exercise(),
    val previousWorkoutLog: WorkoutLog = WorkoutLog(),
    val startFinishBtnText: String = "Start",
    val reps: Int = 0,
    val weight: Int = 0,
    val stopWatchTime: String = "00:00",
    val stopWatchIcon: ImageVector = Icons.Rounded.PlayArrow,
    val isStopWatchRunning: Boolean = false,
    val isSaved: Boolean = false,
    val mood: WorkoutMood = WorkoutMood.TERRIBLE,
    val workoutIsFinished: Boolean = false,
    val navigateBack: Boolean = false,
    val newPR: Boolean = false,
    val pr: PersonalRecord = PersonalRecord(),
    val editWorkout: Boolean = false,
    val isPublic: Boolean = true,
    val isEditButtonEnabled: Boolean = true,
    val showCancelWorkoutDialog: Boolean = false,
)

sealed interface WorkoutEvent {
    object StartWorkout : WorkoutEvent
    object FinishWorkout : WorkoutEvent

    object OnSetDone : WorkoutEvent
    object OnSetNext : WorkoutEvent
    object OnTogglePRCongratsDialog : WorkoutEvent

    data class OnUpdateReps(val reps: Int) : WorkoutEvent
    data class OnUpdateWeight(val weight: Int) : WorkoutEvent

    data class OnMoodSelected(val mood: WorkoutMood) : WorkoutEvent
    data object OnNavigateBack : WorkoutEvent
    data object OnToggleEditWorkout : WorkoutEvent
    data object OnToggleIsPublic : WorkoutEvent
    data class OnCancelWorkout(val saveWorkout: Boolean) : WorkoutEvent
    data object OnToggleCancelWorkoutDialog : WorkoutEvent
}

enum class WorkoutMood(val emoji: String, val label: String, val value: Int) {
    TERRIBLE("😫", "Terrible", 1),
    BAD("😤", "Tough", 2),
    OKAY("😐", "Okay", 3),
    GOOD("😊", "Good", 4),
    AMAZING("🔥", "Amazing", 5)
}