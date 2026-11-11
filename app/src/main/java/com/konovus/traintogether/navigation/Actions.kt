package com.konovus.traintogether.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.LineStyle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.konovus.traintogether.data.utils.toNavigationPath

sealed class Screens(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    data object Auth: Screens("auth_screen", "Auth", null)
    data object Home: Screens("home_screen", "Home", Icons.Rounded.Home)
    data object Profile: Screens("profile_screen", "Profile", Icons.Rounded.PersonOutline)
    data object Settings: Screens("settings_screen", "Settings", null)
    data object Feed: Screens("feed_screen", "Feed", Icons.Filled.LineStyle)
    data object Stats: Screens("stats_screen", "Stats", Icons.Default.BubbleChart)
    data object AddWorkout: Screens("add_workout_screen", "AddWorkout", null){
        const val workoutIdArg = "workoutId"
        val routeWithArgs = "$route/{$workoutIdArg}"
        val arguments = listOf(
            navArgument(workoutIdArg) { type = NavType.IntType }
        )

    }
    data object Workout: Screens("workout_screen", "Workout", null){
        const val workoutIdArg = "workoutId"
        val routeWithArgs = "$route/{$workoutIdArg}"
        val arguments = listOf(
            navArgument(workoutIdArg) { type = NavType.IntType }
        )
    }

    companion object {
        val all by lazy {   listOf(
            Home, Auth, AddWorkout, Workout, Stats, Feed, Profile, Settings
        )}
        val bottomNavRoutes by lazy { listOf(Home.route, Stats.route, Feed.route, Profile.route,) }
    }
}



class Actions(private val navController: NavHostController) {

    /**
     * Navigates to the specified route with the given parameters.
     *
     * If the route equals "back", this function will pop the back stack.
     *
     * @param route The route to navigate to. If empty or "back", navigates back.
     * @param params A list of parameters to be appended to the route as part of the URI path.
     */
    fun navigateTo(route: String, params: List<Any>) {
        if (route.isEmpty() || route == "back") {
            navController.popBackStack()
        } else {
            val uri = route + params.toNavigationPath()
            navController.navigate(uri) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
            }
        }
    }

    fun navigateToAndRestoreState(route: String, params: List<Any>) {
        if (route.isEmpty() || route == "back") {
            navController.popBackStack()
        } else {
            val uri = route + params.toNavigationPath()
            navController.navigate(uri) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }



    fun navigateBack() {
        navController.popBackStack()
    }

    fun navigateAndClearStack(route: String, params: List<Any>) {
        if (route.isNotEmpty()) {
            val uri = route + params.toNavigationPath()
            navController.navigate(uri) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}