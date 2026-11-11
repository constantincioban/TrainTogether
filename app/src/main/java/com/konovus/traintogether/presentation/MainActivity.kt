package com.konovus.traintogether.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.konovus.traintogether.R
import com.konovus.traintogether.data.local.models.UserProfile
import com.konovus.traintogether.data.permission.NOTIFICATION_PERMISSION
import com.konovus.traintogether.data.permission.ShowPermissionRationale
import com.konovus.traintogether.data.permission.isPermissionGranted
import com.konovus.traintogether.data.service.TimeTrackingService
import com.konovus.traintogether.navigation.Actions
import com.konovus.traintogether.navigation.Navigation
import com.konovus.traintogether.navigation.Screens
import com.konovus.traintogether.presentation.redux.AppState
import com.konovus.traintogether.presentation.redux.Store
import com.konovus.traintogether.presentation.ui.composables.ActiveWorkoutBanner
import com.konovus.traintogether.presentation.ui.composables.BottomBar
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.AppTheme
import com.konovus.traintogether.presentation.ui.theme.TrainTogetherTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var store: Store<AppState>


    @SuppressLint("WrongConstant")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val selectedUri: Uri = data?.data ?: return
            val takeFlags = data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION)
            contentResolver.takePersistableUriPermission(selectedUri, takeFlags)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )

        ) // tells the system to draw behind status/nav bars

        lifecycleScope.launch {
            checkAuthStatus()
        }

        val isLoggedIn = store.state.value.isLoggedIn


        setContent {
            val storeState = store.state.collectAsState()
            val theme = when (storeState.value.theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                else -> isSystemInDarkTheme()
            }

            val isStopWatchRunning = storeState.value.isStopWatchRunning
            LaunchedEffect(isStopWatchRunning) {
                val intent = Intent(this@MainActivity, TimeTrackingService::class.java)
                if (isStopWatchRunning) {
                    intent.action = TimeTrackingService.ACTION_START_STOPWATCH
                    startForegroundService(intent)
                } else {
                    stopService(intent)
                }
            }

            TrainTogetherTheme(darkTheme = theme) {
                val view = LocalView.current
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as Activity).window
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                            !theme
                    }
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val snackbarHostState = remember { SnackbarHostState() }
                val actions = remember(navController) { Actions(navController) }

                if (currentRoute == Screens.Home.route) {
                    var showPermissionDialog by remember { mutableStateOf(false) }

                    if (!isPermissionGranted(this, NOTIFICATION_PERMISSION))
                        showPermissionDialog = true


                    ShowPermissionRationale(
                        context = this,
                        showPermissionDialog = showPermissionDialog,
                        permission = NOTIFICATION_PERMISSION,
                        description = stringResource(R.string.post_notifications_permission_description),
                        permanentlyDeclinedDescription = stringResource(R.string.post_notifications_permanently_declined_description)
                    )
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    bottomBar = {
                        Column {
                            println("MainActivity: workoutIsRunning = ${storeState.value.workoutState.workoutIsRunning}")
                            if (currentRoute != Screens.Workout.routeWithArgs)
                                ActiveWorkoutBanner(
                                    isVisible = storeState.value.workoutState.workoutIsRunning,
                                    workoutTitle = storeState.value.workoutState.workoutTitle,
                                    currentExercise = storeState.value.workoutState.currentExercise.gymExercise.displayName,
                                    currentSet = storeState.value.workoutState.currentSet,
                                    stopWatchTime = storeState.value.workoutState.stopWatchTime,
                                    onClick = {
                                        actions.navigateTo(
                                            Screens.Workout.route,
                                            listOf(storeState.value.workoutState.workoutId)
                                        )
                                    }
                                )

                            if (Screens.bottomNavRoutes.contains(currentRoute)) {
                                BottomBar(navController = navController, actions = actions)
                            }
                        }

                    }
                ) { innerPadding ->

                    Navigation(
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                        isLoggedIn = isLoggedIn,
                        store = storeState
                    )
                }

            }
        }
    }

    private fun startTimeTrackingService() {
        if (store.state.value.isStopWatchRunning) {
            println("Starting time tracking service")
            val intent = Intent(this, TimeTrackingService::class.java)
            intent.action = TimeTrackingService.ACTION_START_STOPWATCH
            startForegroundService(intent)
        }
    }

    private fun stopTimeTrackingService() {
        if (store.state.value.isStopWatchRunning) {
            println("Stopping time tracking service")
            val intent = Intent(this, TimeTrackingService::class.java)
            intent.action = TimeTrackingService.ACTION_STOP
            startForegroundService(intent)

        }
    }

    suspend fun checkAuthStatus() {
        val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        store.update {
            it.copy(
                userProfile = UserProfile(
                    id = userId.toString(),
                    email = userEmail.toString(),
                ),
                isLoggedIn = isLoggedIn
            )
        }
    }
}



