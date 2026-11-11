package com.konovus.traintogether.presentation.ui.screens.homeScreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.konovus.traintogether.data.auth.IAuthHelper
import com.konovus.traintogether.data.local.db.ILocalDBRepository
import com.konovus.traintogether.data.local.entities.WorkoutLogDB
import com.konovus.traintogether.data.local.models.UserProfile
import com.konovus.traintogether.data.local.models.Workout
import com.konovus.traintogether.data.local.preferences.SettingsManager
import com.konovus.traintogether.data.remote.IWorkoutRepository
import com.konovus.traintogether.data.remote.OrderDirection
import com.konovus.traintogether.data.remote.QueryOptions
import com.konovus.traintogether.data.utils.ImmutableList
import com.konovus.traintogether.data.utils.ImmutableMap
import com.konovus.traintogether.data.utils.PeriodType
import com.konovus.traintogether.data.utils.WorkoutFrequency
import com.konovus.traintogether.data.utils.calculateStreak
import com.konovus.traintogether.data.utils.toAppTheme
import com.konovus.traintogether.data.utils.toTimeFormat
import com.konovus.traintogether.presentation.redux.AppState
import com.konovus.traintogether.presentation.redux.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject


@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val remoteDb: IWorkoutRepository,
    private val localDb: ILocalDBRepository,
    private val store: Store<AppState>,
    private val settingsManager: SettingsManager,
    private val authHelper: IAuthHelper,
) : ViewModel() {

    private val _uiState = mutableStateOf(HomeState())
    val uiState = _uiState

    private val workoutLogs by lazy { mutableListOf<WorkoutLogDB>() }

    init {
        initializeUserData()
        observeLocalDatabase()
        observeStateChanges()
    }

    // =================================
    // INITIALIZATION METHODS
    // =================================

    private fun initializeUserData() {
        viewModelScope.launch {
            val username = settingsManager.readStringSetting(SettingsManager.USERNAME)
                .trim().ifEmpty { "User" }
            val userEmail = FirebaseAuth.getInstance().currentUser?.email
            val profilePicUri = settingsManager.readStringSetting(SettingsManager.PROFILE_PIC_URI)
            val theme = settingsManager.readStringSetting(SettingsManager.THEME)

            if (username == "User") {
                remoteDb
                    .getUserProfile(store.state.value.userProfile.id)
                    .onSuccess { userProfileDB ->
                        if (userProfileDB != null) {
                            store.update { it.copy(userProfile = userProfileDB.toUiModel()) }
                            return@launch
                        }
                    }
            }

            _uiState.value = _uiState.value.copy(
                name = username,
                userEmail = userEmail.toString(),
                profilePicUri = profilePicUri,
            )

            store.update {
                it.copy(
                    userProfile = UserProfile(
                        id = store.state.value.userProfile.id,
                        email = userEmail.toString(),
                        username = username,
                        profilePicUri = profilePicUri
                    ),
                    theme = theme.toAppTheme()
                )
            }
        }
    }

    /**
     * Observe local database for real-time updates
     */
    private fun observeLocalDatabase() {
        viewModelScope.launch {
            // Observe workouts from local DB
            localDb.getAllWorkouts().collect { workouts ->
                val workoutUiModels = workouts.map { it.toUiModel() }
                _uiState.value = _uiState.value.copy(
                    workouts = ImmutableList(workoutUiModels),
                    workoutsListIsLoading = false
                )
                store.update { it.copy(workouts = workoutUiModels) }
            }
        }

        viewModelScope.launch {
            // Observe workout logs from local DB
            localDb.getAllWorkoutLogs().collect { logs ->
                if (logs.isEmpty()) {
                    println("No workout logs found in local DB")
                    return@collect
                }

                val workoutFrequency = settingsManager
                    .readStringSetting(SettingsManager.WORKOUT_FREQUENCY)
                    .ifEmpty { WorkoutFrequency.ONCE_A_WEEK.name }

                val streak = logs.calculateStreak(WorkoutFrequency.valueOf(workoutFrequency))

                store.update {
                    it.copy(workoutLogs = logs, streak = streak.currentStreak)
                }
                _uiState.value = _uiState.value.copy(
                    streak = streak.currentStreak,
                    longestStreak = streak.longestStreak,
                    totalWorkoutLogs = logs.size,
                    totalVolume = formatWeight(calculateTotalWeight(logs))
                )

            }
        }

        viewModelScope.launch {
            // Observe personal records from local DB
            localDb.getAllPersonalRecords().collect { records ->
                if (records.isNotEmpty()) {
                    store.update { it.copy(personalRecords = records.map { it.toUiModel() }) }
                }
            }
        }
    }

    private fun syncFromRemote() {
        viewModelScope.launch {
            val userId = store.state.value.userProfile.id

            _uiState.value = _uiState.value.copy(isSyncing = true)

            // Sync workouts
            remoteDb.getWorkouts(
                userId,
                QueryOptions(orderBy = "id", orderDirection = OrderDirection.DESCENDING)
            ).onSuccess { remoteWorkouts ->
                localDb.insertWorkouts(remoteWorkouts)
                println("Synced ${remoteWorkouts.size} workouts from remote")
            }.onError { error ->
                println("Error syncing workouts: ${error.message}")
            }

            // Sync workout logs
            remoteDb.getWorkoutLogs(userId = userId)
                .onSuccess { remoteLogs ->
                    localDb.insertWorkoutLogs(remoteLogs)
                    println("Synced ${remoteLogs.size} workout logs from remote")
                }
                .onError { error ->
                    println("Error syncing workout logs: $error")
                }

            // Sync personal records
            remoteDb.getAllTimePRs(userId)
                .onSuccess { remotePRs ->
                    localDb.insertPersonalRecords(remotePRs)
                    println("Synced ${remotePRs.size} personal records from remote")
                }
                .onError { error ->
                    println("Error syncing personal records: $error")
                }

            _uiState.value = _uiState.value.copy(isSyncing = false)
        }
    }

    suspend fun observeStore() {
        store.state.collect { data ->
            workoutLogs.clear()
            workoutLogs.addAll(data.workoutLogs)

            val checkedDays = data.workoutLogs
                .map { it.startedTimestamp }
                .distinct()
                .map { timestamp ->
                    timestampToLocalDate(timestamp)
                }

            val streak = workoutLogs.calculateStreak(data.workoutFrequency)

            _uiState.value = _uiState.value.copy(
                checkedDays = ImmutableList(checkedDays),
                profilePicUri = data.userProfile.profilePicUri,
                name = data.userProfile.username,
                streak = streak.currentStreak,
            )

            updateTimeFrameStats(_uiState.value.currentTimeOffset)
        }
    }

    private fun observeStateChanges() = viewModelScope.launch {
        snapshotFlow { _uiState.value.showLogoutDialog }
            .distinctUntilChanged()
            .collect { showLogoutDialog ->
                if (showLogoutDialog) {
                    delay(50)
                    _uiState.value = _uiState.value.copy(showProfilePopupDialog = false)
                }
            }
    }

    // =================================
    // EVENT HANDLING
    // =================================

    fun sendEvent(event: HomeEvents) {
        when (event) {
            is HomeEvents.OnUpdateProfilePic -> updateProfilePic(event.uri)
            is HomeEvents.OnUpdateTimeFrame -> updateTimeFrame(event.timeFrame)
            is HomeEvents.OnTimePeriodsChanged -> updateTimeFrameStats(event.offset)
            HomeEvents.OnProfilePicClick -> toggleProfilePicDialogVisibility()
            HomeEvents.OnToggleShowLogoutDialog -> toggleShowLogoutDialog()
            HomeEvents.OnSignOut -> handleSignOut()
            HomeEvents.OnToggleShowStreakDetailDialog -> toggleShowStreakDetailDialog()
            is HomeEvents.OnSortChanged -> updateSort(event.sort)
            HomeEvents.OnToggleSortDialog -> toggleSortDialog()
            is HomeEvents.OnDateClicked -> handleOnDateClicked(event.date)
            HomeEvents.OnDismissWorkoutDetail -> handleDismissWorkoutDetail()
            HomeEvents.OnRefresh -> syncFromRemote()
        }
    }

    private fun handleDismissWorkoutDetail() {
        _uiState.value = _uiState.value.copy(
            showWorkoutDetailDialog = false,
            selectedWorkoutLog = null
        )
    }

    private fun handleOnDateClicked(date: LocalDate) {
        val workout = workoutLogs.find { log ->
            timestampToLocalDate(log.startedTimestamp) == date
        }
        val workoutName = store.state.value.workouts
            .find { it.id == workout?.workoutId }
            ?.title ?: "Workout Session"

        _uiState.value = _uiState.value.copy(
            showWorkoutDetailDialog = true,
            selectedWorkoutLog = workout,
            selectedWorkoutName = workoutName
        )
    }

    private fun updateSort(sort: WorkoutSort) {
        _uiState.value = _uiState.value.copy(workoutSort = sort)
        applySortToWorkouts()
    }

    private fun toggleSortDialog() {
        _uiState.value = _uiState.value.copy(
            showSortDialog = !_uiState.value.showSortDialog
        )
    }

    private fun applySortToWorkouts() {
        val currentSort = _uiState.value.workoutSort
        val workoutsFromStore = store.state.value.workouts
        val workoutLogsFromStore = store.state.value.workoutLogs

        val sortedWorkouts = when (currentSort.type) {
            WorkoutSortType.DATE -> {
                workoutsFromStore.sortedBy { it.id }
            }

            WorkoutSortType.USAGE -> {
                val usageCount = workoutsFromStore.map { workout ->
                    val count =
                        workoutLogsFromStore.count { log -> log.toUiModel().workoutId == workout.id }
                    workout to count
                }.sortedBy { it.second }

                usageCount.map { it.first }
            }

            WorkoutSortType.VOLUME -> {
                val volumeMap = workoutsFromStore.map { workout ->
                    val totalVolume = workoutLogsFromStore
                        .filter { log -> log.toUiModel().workoutId == workout.id }
                        .flatMap { it.exerciseLogs }
                        .sumOf { exerciseLog ->
                            (exerciseLog.weight * exerciseLog.reps).toDouble()
                        }
                    workout to totalVolume
                }.sortedBy { it.second }

                volumeMap.map { it.first }
            }
        }

        val finalSorted = if (currentSort.order == SortOrder.DESCENDING) {
            sortedWorkouts.reversed()
        } else {
            sortedWorkouts
        }

        _uiState.value = _uiState.value.copy(workouts = ImmutableList(finalSorted))
    }

    private fun toggleShowStreakDetailDialog() {
        _uiState.value =
            _uiState.value.copy(showStreakDetailDialog = !_uiState.value.showStreakDetailDialog)
    }

    private fun handleSignOut() {
        viewModelScope.launch {
            // Clear local database on sign out
            localDb.clearAllData()

            authHelper.signOut { success, message ->
                if (success) {
                    _uiState.value = _uiState.value.copy(signOutSuccessfully = true)
                    println("Signed out successfully: $message")
                } else {
                    println("Sign out failed: $message")
                }
            }
        }
    }

    private fun toggleShowLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = !_uiState.value.showLogoutDialog)
    }

    private fun toggleProfilePicDialogVisibility() {
        _uiState.value =
            _uiState.value.copy(showProfilePopupDialog = !_uiState.value.showProfilePopupDialog)
    }

    private fun updateProfilePic(uri: String) {
        _uiState.value = _uiState.value.copy(profilePicUri = uri)
    }

    private fun updateTimeFrame(timeFrame: TimeFrame) {
        _uiState.value = _uiState.value.copy(currentTimeOffset = 0)
        _uiState.value = _uiState.value.copy(timeFrame = timeFrame)
        updateTimeFrameStats()
    }

    private fun updateTimeFrameStats(offset: Int = 0) {
        val filteredLogs = getWorkoutsForTimeFrame(_uiState.value.timeFrame, offset)
        val totalWorkouts = filteredLogs.size
        val totalWeightKg = calculateTotalWeight(filteredLogs)
        val totalTimeMs = calculateTotalTime(filteredLogs)

        _uiState.value = _uiState.value.copy(
            currentTimeOffset = offset,
            totalWorkoutLogsForTimeframe = totalWorkouts,
            totalWeightForTimeframe = formatWeight(totalWeightKg),
            totalTime = if (totalTimeMs > 0) totalTimeMs.toTimeFormat() else "0m"
        )
    }

    // =================================
    // STATISTICS CALCULATION
    // =================================

    private fun getWorkoutsForTimeFrame(timeFrame: TimeFrame, offset: Int): List<WorkoutLogDB> {
        return when (timeFrame) {
            TimeFrame.WEEK -> getWorkoutsForWeek(offset)
            TimeFrame.MONTH -> getWorkoutsForMonth(offset)
            TimeFrame.YEAR -> getWorkoutsForYear(offset)
        }
    }

    private fun getWorkoutsForWeek(offset: Int): List<WorkoutLogDB> {
        val today = LocalDate.now()
        val weekStart = today.minusDays(6 + (kotlin.math.abs(offset) * 7).toLong())
        val weekEnd = weekStart.plusDays(6)

        return filterWorkoutsByDateRange(weekStart, weekEnd)
    }

    private fun getWorkoutsForMonth(offset: Int): List<WorkoutLogDB> {
        val targetMonth = YearMonth.from(LocalDate.now().plusMonths(offset.toLong()))

        return workoutLogs.filter { workout ->
            val workoutDate = timestampToLocalDate(workout.startedTimestamp)
            workoutDate.month == targetMonth.month && workoutDate.year == targetMonth.year
        }
    }

    private fun getWorkoutsForYear(offset: Int): List<WorkoutLogDB> {
        val targetYear = LocalDate.now().year + offset

        val workoutsForTargetYear = workoutLogs.filter { log ->
            val logDate = timestampToLocalDate(log.startedTimestamp)
            logDate.year == targetYear
        }

        val monthlyWorkouts: Map<Int, Int> = (1..12).associateWith { monthIndex ->
            workoutsForTargetYear.count { log ->
                val logDate = timestampToLocalDate(log.startedTimestamp)
                logDate.monthValue == monthIndex
            }
        }

        _uiState.value = _uiState.value.copy(monthlyWorkouts = ImmutableMap(monthlyWorkouts))

        return workoutsForTargetYear
    }

    private fun calculateTotalWeight(workouts: List<WorkoutLogDB>): Int {
        return workouts
            .flatMap { it.exerciseLogs }
            .sumOf { it.weight * it.reps }
    }

    private fun calculateTotalTime(workouts: List<WorkoutLogDB>): Long {
        return workouts.sumOf { it.finishedTimestamp - it.startedTimestamp }
    }

    // =================================
    // UTILITY METHODS
    // =================================

    private fun formatWeight(weightInKg: Int): String {
        return if (weightInKg >= 1000) {
            val thousands = weightInKg / 1000.0
            String.format("%.1fk", thousands)
        } else {
            "${weightInKg}kg"
        }
    }

    private fun filterWorkoutsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<WorkoutLogDB> {
        return workoutLogs.filter { workout ->
            val workoutDate = timestampToLocalDate(workout.startedTimestamp)
            workoutDate >= startDate && workoutDate <= endDate
        }
    }

    private fun timestampToLocalDate(timestamp: Long): LocalDate {
        return LocalDate.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
    }

    /**
     * Public method to trigger manual sync
     */
    fun refreshData() {
        syncFromRemote()
    }
}
// =================================
// DATA CLASSES & SEALED INTERFACES
// =================================

data class HomeState(
    val name: String = "User",
    val userEmail: String = "",
    val profilePicUri: String = "",
    val timeFrame: TimeFrame = TimeFrame.WEEK,
    val checkedDays: ImmutableList<LocalDate> = ImmutableList(),
    val monthlyWorkouts: ImmutableMap<Int, Int> = ImmutableMap(),
    val totalWorkoutLogsForTimeframe: Int = 0,
    val totalWorkoutLogs: Int = 0,
    val totalWeightForTimeframe: String = "0kg",
    val totalVolume: String = "",
    val totalTime: String = "0m",
    val workouts: ImmutableList<Workout> = ImmutableList(),
    val workoutsListIsLoading: Boolean = false,
    val streak: Int = 0,
    val streakPeriodType: PeriodType = PeriodType.WEEK,
    val showProfilePopupDialog: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val signOutSuccessfully: Boolean = false,
    val longestStreak: Int = 0,
    val showStreakDetailDialog: Boolean = false,
    val workoutSort: WorkoutSort = WorkoutSort(),
    val showSortDialog: Boolean = false,
    val currentTimeOffset: Int = 0,
    val showWorkoutDetailDialog: Boolean = false,
    val selectedWorkoutLog: WorkoutLogDB? = null,
    val selectedWorkoutName: String = "",
    val isSyncing: Boolean = false,
)

@Immutable
data class ProgressState(
    val timeFrame: TimeFrame = TimeFrame.WEEK,
    val checkedDays: List<LocalDate> = emptyList(),
    val monthlyWorkouts: Map<Int, Int> = emptyMap(),
    val totalWorkoutLogsForTimeframe: Int = 0,
    val totalWeightForTimeframe: String = "0kg",
    val totalTime: String = "00:00",
)

enum class TimeFrame {
    WEEK, MONTH, YEAR
}

sealed interface HomeEvents {
    data class OnUpdateProfilePic(val uri: String) : HomeEvents
    data class OnUpdateTimeFrame(val timeFrame: TimeFrame) : HomeEvents
    data class OnTimePeriodsChanged(val offset: Int) : HomeEvents
    object OnProfilePicClick : HomeEvents
    object OnToggleShowLogoutDialog : HomeEvents
    object OnSignOut : HomeEvents
    object OnToggleShowStreakDetailDialog : HomeEvents
    data class OnSortChanged(val sort: WorkoutSort) : HomeEvents
    object OnToggleSortDialog : HomeEvents
    data class OnDateClicked(val date: LocalDate) : HomeEvents
    object OnDismissWorkoutDetail : HomeEvents
    object OnRefresh : HomeEvents
}

enum class WorkoutSortType {
    DATE,
    USAGE,
    VOLUME
}

enum class SortOrder {
    ASCENDING,
    DESCENDING
}

data class WorkoutSort(
    val type: WorkoutSortType = WorkoutSortType.DATE,
    val order: SortOrder = SortOrder.DESCENDING
)