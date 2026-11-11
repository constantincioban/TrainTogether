package com.konovus.traintogether.navigation

import ProfileScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.konovus.traintogether.presentation.redux.AppState
import com.konovus.traintogether.presentation.ui.screens.addWorkoutScreen.AddWorkoutScreen
import com.konovus.traintogether.presentation.ui.screens.authScreen.AuthScreen
import com.konovus.traintogether.presentation.ui.screens.feedScreen.FeedScreen
import com.konovus.traintogether.presentation.ui.screens.homeScreen.HomeScreen
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.SettingsScreen
import com.konovus.traintogether.presentation.ui.screens.statsScreen.StatsScreen
import com.konovus.traintogether.presentation.ui.screens.workoutScreen.WorkoutScreen

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun Navigation(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    isLoggedIn: Boolean,
    store: State<AppState>,
) {
    val actions = remember(navController) { Actions(navController) }
    val startDestination = if (isLoggedIn) {
        Screens.Home.route
    } else if (store.value.isStopWatchRunning) {
        Screens.Workout.route
    } else {
        Screens.Auth.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = 250,
                    easing = FastOutSlowInEasing
                )
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = FastOutSlowInEasing
                )
            )
        },
        popEnterTransition = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = 250,
                    easing = FastOutSlowInEasing
                )
            )
        },
        popExitTransition = {
            fadeOut(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = FastOutSlowInEasing
                )
            )
        },
    ) {

        composable(route = Screens.Auth.route) {
            AuthScreen(
                snackbarHostState = snackbarHostState,
                navigateTo = { route, params -> actions.navigateAndClearStack(route, params) },
            )
        }

        composable(
            route = Screens.AddWorkout.routeWithArgs,
            arguments = Screens.AddWorkout.arguments,
        ) { backStackEntry ->
            AddWorkoutScreen(
                workoutId = backStackEntry.arguments?.getInt(Screens.AddWorkout.workoutIdArg) ?: 0,
                snackbarHostState = snackbarHostState,
                navigateBack = { actions.navigateBack() },
                navigateTo = { route, params -> actions.navigateTo(route, params) },
                navigateAndClearStack = { route, params -> actions.navigateAndClearStack(route, params) },
            )
        }

        composable(route = Screens.Home.route) {
            HomeScreen(
                navigateTo = { route, params -> actions.navigateTo(route, params) },
                navigateAndClearStack = { route, params ->  actions.navigateAndClearStack(route, params) }
            )
        }

        composable(route = Screens.Profile.route) {
            ProfileScreen(
                navigateAndClearStack = { route, params -> actions.navigateTo(route, params) },
            )
        }

        composable(route = Screens.Settings.route) {
            SettingsScreen(
                snackbarHostState = snackbarHostState,
                navigateAndClearStack = { route, params -> actions.navigateTo(route, params) },
            )
        }

        composable(route = Screens.Feed.route) {
            FeedScreen(
                snackbarHostState = snackbarHostState,
                navigateAndClearStack = { route, params -> actions.navigateTo(route, params) },
            )
        }

        composable(route = Screens.Stats.route) {
            StatsScreen(
                navigateBack = { actions.navigateBack() },
            )
        }

        composable(
            route = Screens.Workout.routeWithArgs,
            arguments = Screens.Workout.arguments,
        ) { backStackEntry ->
            WorkoutScreen(
                workoutEditId = backStackEntry.arguments?.getInt(Screens.Workout.workoutIdArg) ?: 0,
                navigateBack = { actions.navigateBack() },
                navigateTo = { route, params -> actions.navigateTo(route, params) },
                navigateToAndRestoreState = { route, params -> actions.navigateToAndRestoreState(route, params) },
                navigateAndClearStack = { route, params -> actions.navigateTo(route, params) },
                snackbarHostState = snackbarHostState
            )
        }
    }
}