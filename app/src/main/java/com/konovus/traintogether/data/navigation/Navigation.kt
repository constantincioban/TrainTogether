package com.konovus.traintogether.data.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.konovus.traintogether.presentation.ui.screens.authScreen.AuthScreen
import com.konovus.traintogether.presentation.ui.screens.homeScreen.HomeScreen
import com.konovus.traintogether.presentation.ui.screens.profileScreen.ProfileScreen

@Composable
fun Navigation(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    authPreferenceWasAsked: Boolean = false,
) {
    val actions = remember(navController) { Actions(navController) }
    val startDestination = Screens.Auth.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {


        composable(
            route = Screens.Auth.route,
        ) {
            AuthScreen(
                snackbarHostState = snackbarHostState,
                navigateTo = { route, params -> actions.navigateAndClearStack(route, params) },
            )
        }

        composable(
            route = Screens.Home.route,
        ) {
            HomeScreen(
                navigateTo = { route, params -> actions.navigateTo(route, params) },
            )
        }

        composable(
            route = Screens.Profile.route
        ) {
            ProfileScreen(
                snackbarHostState = snackbarHostState,
                navigateAndClearStack = { route, params -> actions.navigateAndClearStack(route, params) },
            )
        }

    }

}