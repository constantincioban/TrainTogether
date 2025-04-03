package com.konovus.traintogether.data.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import com.konovus.traintogether.data.utils.toNavigationPath

sealed class Screens(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    data object Auth: Screens("auth_screen", "Auth", null)
    data object Home: Screens("home_screen", "Home", Icons.Rounded.Home)
    data object Profile: Screens("profile_screen", "Profile", Icons.Rounded.PersonOutline)

    companion object {
        val all by lazy {   listOf(
            Home, Profile, Auth
        )}
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
            navController.navigate(uri)
        }
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