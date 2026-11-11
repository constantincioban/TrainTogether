package com.konovus.traintogether.presentation.ui.screens.homeScreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.konovus.traintogether.data.local.models.Workout
import com.konovus.traintogether.data.utils.ImmutableList
import com.konovus.traintogether.navigation.Screens
import com.konovus.traintogether.presentation.ui.composables.LogoutConfirmationDialog
import com.konovus.traintogether.presentation.ui.screens.homeScreen.components.EmptyWorkoutsStateSection
import com.konovus.traintogether.presentation.ui.screens.homeScreen.components.GreetingSection
import com.konovus.traintogether.presentation.ui.screens.homeScreen.components.ProfilePopupDialog
import com.konovus.traintogether.presentation.ui.screens.homeScreen.components.ProgressSection
import com.konovus.traintogether.presentation.ui.screens.homeScreen.components.ShimmerListItem
import com.konovus.traintogether.presentation.ui.screens.homeScreen.components.SortDialog
import com.konovus.traintogether.presentation.ui.screens.homeScreen.components.StreakDetailDialog
import com.konovus.traintogether.presentation.ui.screens.homeScreen.components.WorkoutCardComponent
import com.konovus.traintogether.presentation.ui.screens.homeScreen.components.WorkoutSessionDetailDialog
import com.konovus.traintogether.presentation.ui.screens.homeScreen.components.WorkoutsHeaderSection

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun HomeScreen(
    navigateTo: (route: String, params: List<Any>) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    navigateAndClearStack: (route: String, params: List<Any>) -> Unit,
) {
    val uiState = viewModel.uiState.value
    val navigateToStable = remember { navigateTo }
    val workouts: ImmutableList<Workout> = uiState.workouts
    val sendEventStable = remember { viewModel::sendEvent }

    val lazyRowState = rememberLazyListState()
    LaunchedEffect(uiState.workoutSort) {
        lazyRowState.animateScrollToItem(0)
    }

    LaunchedEffect(Unit) {
        viewModel.observeStore()
    }

    WorkoutSessionDetailDialog(
        showDialog = uiState.showWorkoutDetailDialog,
        onDismiss = { viewModel.sendEvent(HomeEvents.OnDismissWorkoutDetail) },
        workoutLog = uiState.selectedWorkoutLog,
        workoutName = uiState.selectedWorkoutName
    )

    StreakDetailDialog(
        showDialog = uiState.showStreakDetailDialog,
        onDismiss = { viewModel.sendEvent(HomeEvents.OnToggleShowStreakDetailDialog) },
        currentStreak = uiState.streak,
        streakPeriodType = uiState.streakPeriodType,
        checkedDays = uiState.checkedDays,
        longestStreak = uiState.longestStreak
    )

    ProfilePopupDialog(
        showDialog = uiState.showProfilePopupDialog,
        onViewProfile = { navigateToStable(Screens.Profile.route, emptyList()) },
        userName = uiState.name,
        userEmail = uiState.userEmail,
        totalWorkouts = uiState.totalWorkoutLogs,
        totalVolume = uiState.totalVolume,
        currentStreak = uiState.streak,
        streakPeriodType = uiState.streakPeriodType,
        profilePicUri = uiState.profilePicUri,
        onSignOut = { viewModel.sendEvent(HomeEvents.OnToggleShowLogoutDialog) },
        onViewSettings = { navigateToStable(Screens.Settings.route, emptyList()) },
        onDismiss = { viewModel.sendEvent(HomeEvents.OnProfilePicClick) },
    )

    LogoutConfirmationDialog(
        showDialog = uiState.showLogoutDialog,
        onDismiss = { viewModel.sendEvent(HomeEvents.OnToggleShowLogoutDialog) },
        onConfirm = { viewModel.sendEvent(HomeEvents.OnSignOut) }
    )

    SortDialog(
        showDialog = uiState.showSortDialog,
        currentSort = uiState.workoutSort,
        onDismiss = { viewModel.sendEvent(HomeEvents.OnToggleSortDialog) },
        onSortChange = { newSort ->
            viewModel.sendEvent(HomeEvents.OnSortChanged(newSort))
        }
    )

    LaunchedEffect(uiState.signOutSuccessfully) {
        if (uiState.signOutSuccessfully) {
            navigateAndClearStack(Screens.Auth.route, emptyList())
        }
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = uiState.isSyncing),
        onRefresh = { viewModel.sendEvent(HomeEvents.OnRefresh) },
        indicator = { state, trigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = trigger,
                contentColor = MaterialTheme.colorScheme.primary,
                backgroundColor = MaterialTheme.colorScheme.background
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                bottom = 100.dp
            ),
        ) {
            item {
                GreetingSection(
                    name = uiState.name,
                    profilePicUri = uiState.profilePicUri,
                    streak = uiState.streak.toString(),
                    periodType = uiState.streakPeriodType.displayName,
                    onProfilePicClick = { viewModel.sendEvent(HomeEvents.OnProfilePicClick) },
                    onStreakClick = { viewModel.sendEvent(HomeEvents.OnToggleShowStreakDetailDialog) }
                )
            }

            item {
                ProgressSection(
                    timeFrame = uiState.timeFrame,
                    checkedDays = uiState.checkedDays,
                    currentTimeOffset = uiState.currentTimeOffset,
                    monthlyWorkouts = uiState.monthlyWorkouts,
                    totalWeightForTimeframe = uiState.totalWeightForTimeframe,
                    totalWorkoutLogsForTimeframe = uiState.totalWorkoutLogsForTimeframe,
                    sendEvent = sendEventStable,
                    totalTime = uiState.totalTime,
                )
            }

            item {
                WorkoutsHeaderSection(
                    onToggleSortDialog = { viewModel.sendEvent(HomeEvents.OnToggleSortDialog) }
                )
            }

            item {
                if (uiState.workoutsListIsLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(3) {
                            ShimmerListItem()
                        }
                    }
                } else if (workouts.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        workouts.forEach { workout ->
                            WorkoutCardComponent(workout) {
                                navigateToStable(
                                    Screens.Workout.route,
                                    listOf(workout.id)
                                )
                            }
                        }
                    }
                } else {
                    EmptyWorkoutsStateSection(
                        onCreateWorkout = {
                            navigateToStable(Screens.AddWorkout.route, emptyList())
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}