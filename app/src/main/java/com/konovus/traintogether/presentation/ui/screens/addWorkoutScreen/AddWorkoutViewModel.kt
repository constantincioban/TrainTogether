package com.konovus.traintogether.presentation.ui.screens.addWorkoutScreen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konovus.traintogether.data.local.db.ILocalDBRepository
import com.konovus.traintogether.data.local.entities.ExerciseDB
import com.konovus.traintogether.data.local.entities.WorkoutDB
import com.konovus.traintogether.data.local.models.Exercise
import com.konovus.traintogether.data.remote.IWorkoutRepository
import com.konovus.traintogether.data.utils.GymExercise
import com.konovus.traintogether.navigation.Screens
import com.konovus.traintogether.presentation.redux.AppState
import com.konovus.traintogether.presentation.redux.Store
import com.konovus.traintogether.presentation.ui.screens.addWorkoutScreen.AddWorkoutViewModel.ToastMessage.NoExercisesSelected
import com.konovus.traintogether.presentation.ui.screens.addWorkoutScreen.AddWorkoutViewModel.ToastMessage.TitleEmpty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


@HiltViewModel
class AddWorkoutViewModel @Inject constructor(
    private val remoteDb: IWorkoutRepository,
    private val localDb: ILocalDBRepository,
    private val store: Store<AppState>,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = mutableStateOf(AddWorkoutState())
    val uiState: State<AddWorkoutState> = _uiState


    private var messageIsActive = false
    private val _eventChannel = Channel<String>(0)
    val eventChannel = _eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            savedStateHandle.get<Int>(Screens.AddWorkout.workoutIdArg)?.let { workoutId ->
                if (workoutId == 0) return@let
                store.state.value.workouts.find { it.id == workoutId }?.let { workout ->
                    _uiState.value = _uiState.value.copy(
                        workoutId = workoutId,
                        title = workout.title,
                        exercises = workout.exercises,
                        circuitExerciseButtonPositions = calculateCircuitPositions(workout.exercises)
                    )
                }
            }
        }
    }

    private fun calculateCircuitPositions(exercises: List<Exercise>): List<Int> {
        return exercises.mapIndexedNotNull { index, exercise ->
            if (exercise.hasCircuitNext ) index else null
        }
    }

    fun onEvent(event: AddWorkoutEvent) {
        when (event) {
            is AddWorkoutEvent.OnTitleChanged -> updateTitle(event.title)
            is AddWorkoutEvent.OnExerciseToggled -> toggleExercise(event.exercise)
            AddWorkoutEvent.SaveWorkout -> saveWorkout()
            AddWorkoutEvent.DeleteWorkout -> deleteWorkout()
            is AddWorkoutEvent.OnChangeWarmUpSets -> updateWarmUpSets(
                event.warmUpSets,
                event.exercise
            )

            is AddWorkoutEvent.OnChangeWorkingSets -> updateWorkingSets(
                event.workingSets,
                event.exercise
            )

            is AddWorkoutEvent.OnToggleCircuitExerciseButton -> toggleCircuitExerciseButton(event.pos)
            AddWorkoutEvent.OnNavigateBack -> navigateBack()
        }
    }

    private fun navigateBack() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(navigateBack = !_uiState.value.navigateBack)
    }

    private fun toggleCircuitExerciseButton(pos: Int) {
        _uiState.value = _uiState.value.copy(
            circuitExerciseButtonPositions = if (_uiState.value.circuitExerciseButtonPositions.contains(
                    pos
                )
            )
                _uiState.value.circuitExerciseButtonPositions - pos else
                _uiState.value.circuitExerciseButtonPositions + pos
        )
    }

    private fun updateWarmUpSets(warmUpSets: String, exercise: GymExercise) {
        if (warmUpSets.isEmpty() || (warmUpSets.isDigitsOnly() && warmUpSets.toInt() in 0..99)) {
            val currentExercises = _uiState.value.exercises
            val updatedExercises = currentExercises.map {
                if (it.gymExercise == exercise) {
                    it.copy(warmUpSets = warmUpSets)
                } else {
                    it
                }
            }
            _uiState.value = _uiState.value.copy(exercises = updatedExercises)
        }
    }

    private fun updateWorkingSets(workingSets: String, exercise: GymExercise) {
        if (workingSets.isEmpty() || workingSets.isDigitsOnly()&& workingSets.toInt() in 0..99) {
            val currentExercises = _uiState.value.exercises
            val updatedExercises = currentExercises.map {
                if (it.gymExercise == exercise) {
                    it.copy(workingSets = workingSets)
                } else {
                    it
                }
            }
            _uiState.value = _uiState.value.copy(exercises = updatedExercises)
        }
    }

    private fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    private fun toggleExercise(exercise: GymExercise) {
        val isAlreadyInList = _uiState.value.exercises.any { it.gymExercise == exercise }

        val updatedList = if (isAlreadyInList) {
            _uiState.value.exercises.filterNot { it.gymExercise == exercise }
        } else {
            _uiState.value.exercises + Exercise(gymExercise = exercise)
        }

        _uiState.value = _uiState.value.copy(exercises = updatedList)
    }

    private fun saveWorkout() {
        if (_uiState.value.title.isBlank()) {
            sendUiEvent(TitleEmpty)
            return
        }
        if (_uiState.value.exercises.isEmpty()) {
            sendUiEvent(NoExercisesSelected)
            return
        }

        val maxId =  store.state.value.workouts.maxByOrNull { it.id }?.id ?: 0
        val workout = WorkoutDB(
            id = _uiState.value.workoutId ?: (maxId + 1),
            title = _uiState.value.title,
            exercises = _uiState.value.exercises.map { exercise ->
                ExerciseDB(
                    id = _uiState.value.exercises.indexOf(exercise),
                    gymExercise = exercise.gymExercise,
                    warmUpSets = exercise.warmUpSets.toInt(),
                    workingSets = exercise.workingSets.toInt(),
                    hasCircuitNext = _uiState.value.circuitExerciseButtonPositions.contains(
                        _uiState.value.exercises.indexOf(exercise)
                    ),
                    hasCircuitPrev = _uiState.value.circuitExerciseButtonPositions.contains(
                            _uiState.value.exercises.indexOf(exercise) - 1
                        ),
                )
            }

        )
        // Save
        viewModelScope.launch {
            localDb.insertWorkout(workout)
            remoteDb.saveWorkout(
                store.state.value.userProfile.id,
                workout,
            ).onSuccess {
                _uiState.value = _uiState.value.copy(isSaved = true)
            }.onError { error ->
                sendUiEvent(ToastMessage.ErrorSavingWorkout("Error while saving: ${error.message}"))
            }
        }
    }

    private fun deleteWorkout() {
        _uiState.value = AddWorkoutState()
    }

    private fun sendUiEvent(toast: ToastMessage) = viewModelScope.launch {
        if (!messageIsActive) {
            _eventChannel.send(toast.message)
            messageIsActive = true
            delay(3500.milliseconds)
            messageIsActive = false
        }
    }

    private sealed class ToastMessage(
        val message: String,
    ) {

        object TitleEmpty : ToastMessage(
            "Title cannot be empty"
        )

        object NoExercisesSelected : ToastMessage(
            "Select at least one exercise"
        )

        class ErrorSavingWorkout(val customMessage: String) : ToastMessage(
            customMessage
        )
    }

}

sealed interface AddWorkoutEvent {
    data class OnTitleChanged(val title: String) : AddWorkoutEvent
    data class OnExerciseToggled(val exercise: GymExercise) : AddWorkoutEvent
    object SaveWorkout : AddWorkoutEvent
    object DeleteWorkout : AddWorkoutEvent
    data class OnChangeWarmUpSets(val warmUpSets: String, val exercise: GymExercise) :
        AddWorkoutEvent

    data class OnChangeWorkingSets(val workingSets: String, val exercise: GymExercise) :
        AddWorkoutEvent

    data class OnToggleCircuitExerciseButton(val pos: Int) : AddWorkoutEvent
    object OnNavigateBack : AddWorkoutEvent
}

data class AddWorkoutState(
    val title: String = "",
    val exercises: List<Exercise> = emptyList(),
    val circuitExerciseButtonPositions: List<Int> = emptyList(),
    val isSaved: Boolean = false,
    val workoutId: Int? = null,
    val navigateBack: Boolean = false,
)

