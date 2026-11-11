package com.konovus.traintogether.presentation.ui.screens.workoutScreen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.konovus.traintogether.data.utils.observeWithLifecycle
import com.konovus.traintogether.navigation.Screens
import com.konovus.traintogether.presentation.ui.screens.workoutScreen.components.ControlButtonsSection
import com.konovus.traintogether.presentation.ui.screens.workoutScreen.components.HeaderSection
import com.konovus.traintogether.presentation.ui.screens.workoutScreen.components.PRCongratsDialog
import com.konovus.traintogether.presentation.ui.screens.workoutScreen.components.StopWatchSection
import com.konovus.traintogether.presentation.ui.screens.workoutScreen.components.WorkoutLoggingSection
import com.konovus.traintogether.presentation.ui.screens.workoutScreen.components.WorkoutSummaryDialog

@Composable
fun WorkoutScreen(
    workoutEditId: Int = 0,
    navigateBack: () -> Unit,
    navigateTo: (String, List<Any>) -> Unit,
    navigateAndClearStack: (route: String, params: List<Any>) -> Unit,
    navigateToAndRestoreState: (route: String, params: List<Any>) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
) {

    val uiState = viewModel.uiState.value
    val navigateBackStable = remember { navigateBack }
    val navigateToStable = remember { navigateTo }
    val navigateToAndRestoreStateStable = remember { navigateToAndRestoreState }

    viewModel.eventChannel.observeWithLifecycle {
        snackbarHostState.showSnackbar(message = it)
    }

    Content(
        workoutEditId = workoutEditId,
        uiState = uiState,
        sendEvent = viewModel::onEvent,
        navigateTo = navigateToStable,
        navigateBack = navigateBackStable,
        navigateToAndRestoreState = navigateToAndRestoreStateStable,
        navigateAndClearStack = navigateAndClearStack,
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Content(
    workoutEditId: Int = 0,
    uiState: WorkoutState,
    sendEvent: (WorkoutEvent) -> Unit = {},
    navigateBack: () -> Unit = {},
    navigateTo: (String, List<Any>) -> Unit,
    navigateToAndRestoreState: (route: String, params: List<Any>) -> Unit,
    navigateAndClearStack: (String, List<Any>) -> Unit = {_, _ ->},
) {

    BackHandler(enabled = true) {
        sendEvent(WorkoutEvent.OnNavigateBack)
    }
    LaunchedEffect(uiState.navigateBack) {
        if (uiState.navigateBack) {
            navigateTo(Screens.Home.route, emptyList())
            sendEvent(WorkoutEvent.OnNavigateBack)
        }
    }
    LaunchedEffect(uiState.editWorkout) {
        if (uiState.editWorkout) {
            navigateTo(Screens.AddWorkout.route, listOf(workoutEditId))
            sendEvent(WorkoutEvent.OnToggleEditWorkout)
        }
    }

    if (uiState.newPR) {
        PRCongratsDialog(
            exerciseName = uiState.pr.exercise.displayName,
            weight = uiState.pr.weight,
            reps = uiState.pr.reps,
            onDismiss = {
                sendEvent(WorkoutEvent.OnTogglePRCongratsDialog)
            }
        )
    }

    if (uiState.isSaved) {
        WorkoutSummaryDialog(
            uiState = uiState,
            onToggleIsPublic = { sendEvent(WorkoutEvent.OnToggleIsPublic) },
            onMoodSelected = { mood ->
                sendEvent(WorkoutEvent.OnMoodSelected(mood))
            },
            onDismiss = {
                navigateAndClearStack(Screens.Home.route, emptyList())
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        HeaderSection(
            navigateBack = { sendEvent(WorkoutEvent.OnNavigateBack) },
            totalTime = uiState.totalTime,
            workoutTitle = uiState.workout.title,
            onEditClick = { sendEvent(WorkoutEvent.OnToggleEditWorkout)}
        )

        StopWatchSection(
            stopWatchTime = uiState.stopWatchTime,
            currentExercise = uiState.currentExercise.gymExercise.displayName,
            currentSet = uiState.currentSet
        )

        WorkoutLoggingSection(
            reps = uiState.reps,
            weight = uiState.weight,
            sendEvent = sendEvent
        )

        ControlButtonsSection(
            showCancelDialog = uiState.showCancelWorkoutDialog,
            startFinishBtnText = uiState.startFinishBtnText,
            workoutIsRunning = uiState.workoutIsRunning,
            sendEvent = sendEvent
        )
    }

}


@Composable
@Preview(showBackground = true, showSystemUi = true)
fun WorkoutScreenContentPreview() {
    Content(
        uiState = WorkoutState(),
        navigateBack = {},
        navigateTo = { _, _ -> },
        navigateAndClearStack = { _, _ -> },
        navigateToAndRestoreState = { _, _ -> }

    )
}