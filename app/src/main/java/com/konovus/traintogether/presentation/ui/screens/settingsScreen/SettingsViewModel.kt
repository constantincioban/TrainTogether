package com.konovus.traintogether.presentation.ui.screens.settingsScreen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konovus.traintogether.data.auth.IAuthHelper
import com.konovus.traintogether.data.local.backup.IBackupManager
import com.konovus.traintogether.data.local.models.PersonalRecord
import com.konovus.traintogether.data.local.preferences.SettingsManager
import com.konovus.traintogether.data.remote.IWorkoutRepository
import com.konovus.traintogether.data.utils.WorkoutFrequency
import com.konovus.traintogether.data.utils.calculateStreak
import com.konovus.traintogether.presentation.redux.AppState
import com.konovus.traintogether.presentation.redux.Store
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.components.BackupStatus
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.components.ImportStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val store: Store<AppState>,
    private val settingsManager: SettingsManager,
    private val remoteDB: IWorkoutRepository,
    private val authHelper: IAuthHelper,
    private val backupManager: IBackupManager,
) : ViewModel() {

    private val _uiState = mutableStateOf(SettingsState())
    val uiState = _uiState

    init {
        viewModelScope.launch {
            val workoutsCount = store.state.value.workouts.size
            val totalWeight =
                store.state.value.workoutLogs.map { it.exerciseLogs }.flatten()
                    .sumOf { it.weight * it.reps }
            val workoutFrequency =
                settingsManager.readStringSetting(SettingsManager.WORKOUT_FREQUENCY)
                    .ifEmpty { WorkoutFrequency.ONCE_A_WEEK.name }
            val streak = store.state.value.workoutLogs.calculateStreak(
                WorkoutFrequency.valueOf(workoutFrequency)
            ).currentStreak
            val prs = store.state.value.personalRecords.distinctBy { it.exercise }
            val unit = settingsManager.readStringSetting(SettingsManager.UNITS)
                .ifEmpty { Units.METRIC.name }

            _uiState.value = _uiState.value.copy(
                profileImageUrl = store.state.value.userProfile.profilePicUri,
                username = store.state.value.userProfile.username,
                theme = store.state.value.theme,
                workoutCount = workoutsCount,
                totalWeight = totalWeight,
                currentStreak = streak,
                workoutFrequency = WorkoutFrequency.valueOf(workoutFrequency),
                personalRecords = prs,
                units = Units.valueOf(unit),
            )
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ChangeSettingsPicture -> handleChangeSettingsPicture(event.profileImageUrl)
            SettingsEvent.DeleteAccount -> handleDeleteAccount()
            SettingsEvent.ToggleEditUsernameDialog -> handleToggleEditUsernameDialog()
            is SettingsEvent.LanguageChanged -> handleLanguageChanged(event.language)
            SettingsEvent.NotificationsToggled -> handleNotificationsToggled()
            SettingsEvent.SignOut -> handleSignOut()
            is SettingsEvent.ThemeChanged -> handleThemeChanged(event.theme)
            is SettingsEvent.UnitsChanged -> handleUnitsChanged(event.units)
            is SettingsEvent.UpdateUsername -> handleUpdateUsername(event.newName)
            is SettingsEvent.UpdateWorkoutFrequency -> handleUpdateWorkoutFrequency(event.newFrequency)
            is SettingsEvent.UpdatePersonalRecords -> handleUpdatePersonalRecords(event.pr)

            SettingsEvent.TogglePRDialog -> handleTogglePRDialog()
            is SettingsEvent.RemovePersonalRecord -> handleRemovePersonalRecord(event.pr)
            SettingsEvent.ToggleShowLogoutDialog -> toggleShowLogoutDialog()

            SettingsEvent.OpenBackupDialog -> handleOpenBackupDialog()
            SettingsEvent.CloseBackupDialog -> handleCloseBackupDialog()
            SettingsEvent.ExportBackupToDownloads -> handleExportBackupToDownloads()
            SettingsEvent.OpenImportDialog -> handleOpenImportDialog()
            SettingsEvent.CloseImportDialog -> handleCloseImportDialog()
            SettingsEvent.PickFileFromDevice -> handlePickFileFromDevice()
            is SettingsEvent.ImportBackupFromUri -> handleImportBackupFromUri(event.uri)
        }
    }

    private fun handleOpenBackupDialog() {
        _uiState.value = _uiState.value.copy(
            showBackupDialog = true,
            backupStatus = BackupStatus.Idle
        )
    }

    private fun handleCloseBackupDialog() {
        _uiState.value = _uiState.value.copy(showBackupDialog = false)
    }

    private fun handleExportBackupToDownloads() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBackupLoading = true,
                backupStatus = BackupStatus.Loading
            )

            val result = backupManager.exportBackupToDownloads(
                userId = store.state.value.userProfile.id,
                username = store.state.value.userProfile.username,
                workoutLogs = store.state.value.workoutLogs,
                personalRecords = store.state.value.personalRecords
            )

            result.onSuccess { filePath ->
                _uiState.value = _uiState.value.copy(
                    isBackupLoading = false,
                    backupStatus = BackupStatus.Success(filePath)
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isBackupLoading = false,
                    backupStatus = BackupStatus.Error(error.message ?: "Unknown error")
                )
            }
        }
    }

    private fun handleOpenImportDialog() {
        _uiState.value = _uiState.value.copy(
            showImportDialog = true,
            importStatus = ImportStatus.Idle
        )
    }

    private fun handleCloseImportDialog() {
        _uiState.value = _uiState.value.copy(showImportDialog = false)
    }

    private fun handlePickFileFromDevice() {
        // This will be triggered by the UI to open file picker
        // The actual file picker is in the Composable
    }

    private fun handleImportBackupFromUri(uri: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isImportLoading = true,
                importStatus = ImportStatus.Loading
            )

            val result = backupManager.importBackupFromUri(uri)

            result.onSuccess { backupData ->
                store.update {
                    it.copy(
                        workoutLogs = backupData.workoutLogs,
                        personalRecords = backupData.personalRecords
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isImportLoading = false,
                    importStatus = ImportStatus.Success(
                        backupData.workoutLogs.size,
                        backupData.personalRecords.size
                    )
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isImportLoading = false,
                    importStatus = ImportStatus.Error(error.message ?: "Unknown error")
                )
            }
        }
    }

    private fun toggleShowLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = !_uiState.value.showLogoutDialog)
    }

    private fun handleRemovePersonalRecord(pr: PersonalRecord) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(personalRecords = _uiState.value.personalRecords - pr)
        settingsManager.saveObjectList(SettingsManager.EXERCISE_PR, _uiState.value.personalRecords)
        remoteDB.deletePersonalRecord(store.state.value.userProfile.id, pr.id)
    }

    private fun handleTogglePRDialog() {
        _uiState.value = _uiState.value.copy(showAddPRDialog = !_uiState.value.showAddPRDialog)
    }

    private fun handleUpdatePersonalRecords(pr: PersonalRecord) = viewModelScope.launch {
        val updatedPR = pr.copy(
            unit = _uiState.value.units,
            id = store.state.value.personalRecords.maxOf { it.id } + 1
        )
        _uiState.value = _uiState.value.copy(
            personalRecords = _uiState.value.personalRecords + updatedPR,
            showAddPRDialog = false
        )
        store.update { it.copy(personalRecords = _uiState.value.personalRecords) }
        remoteDB.savePersonalRecord(store.state.value.userProfile.id, updatedPR.toDbEntity())

    }

    private fun handleUpdateWorkoutFrequency(newFrequency: WorkoutFrequency) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                workoutFrequency = newFrequency,
                currentStreak = store.state.value.workoutLogs.calculateStreak(newFrequency).currentStreak,
            )
            store.update { it.copy(workoutFrequency = newFrequency) }
            settingsManager.saveStringSetting(SettingsManager.WORKOUT_FREQUENCY, newFrequency.name)
        }

    private fun handleUpdateUsername(newName: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(username = newName)
        store.update { it.copy(userProfile = store.state.value.userProfile.copy(username = newName)) }
        settingsManager.saveStringSetting(SettingsManager.USERNAME, newName)
        remoteDB.saveUserProfile(
            store.state.value.userProfile.id,
            store.state.value.userProfile.copy(username = newName).toDbEntity()
        )
    }

    private fun handleToggleEditUsernameDialog() {
        _uiState.value =
            _uiState.value.copy(showEditUsernameDialog = !_uiState.value.showEditUsernameDialog)
    }

    private fun handleChangeSettingsPicture(profileImageUrl: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(profileImageUrl = profileImageUrl)
        store.update { it.copy(userProfile = store.state.value.userProfile.copy(profilePicUri = profileImageUrl)) }
        settingsManager.saveStringSetting(SettingsManager.PROFILE_PIC_URI, profileImageUrl)
        remoteDB.saveUserProfile(
            store.state.value.userProfile.id,
            store.state.value.userProfile.copy(profilePicUri = profileImageUrl).toDbEntity()
        )
    }

    private fun handleThemeChanged(theme: AppTheme) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(theme = theme)

                store.update { currentState ->
                    currentState.copy(theme = theme)
                }
                settingsManager.saveStringSetting(SettingsManager.THEME, theme.displayName)

            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(errorMessage = "Failed to change theme: ${e.message}")
            }
        }
    }

    private fun handleNotificationsToggled() {
        viewModelScope.launch {
            val newValue = !_uiState.value.notificationsEnabled
            try {
                _uiState.value = _uiState.value.copy(notificationsEnabled = newValue)

            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(errorMessage = "Failed to toggle notifications: ${e.message}")
                // Revert state on error
                _uiState.value = _uiState.value.copy(notificationsEnabled = !newValue)
            }
        }
    }

    private fun handleUnitsChanged(units: Units) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(units = units)
                store.update { it.copy(units = units) }
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(errorMessage = "Failed to change units: ${e.message}")
            }
        }
    }

    private fun handleLanguageChanged(language: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(language = language)

            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(errorMessage = "Failed to change language: ${e.message}")
            }
        }
    }

    private fun handleSignOut() {
        _uiState.value = _uiState.value.copy(showSignOutConfirmation = true)
        authHelper.signOut { success, message ->
            if (success) {
                _uiState.value = _uiState.value.copy(signOutSuccessfully = true)
                println("Signed out successfully: $message")
            } else {
                println("Sign out failed: $message")
            }
        }
    }

    private fun handleDeleteAccount() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = true)
    }

}

data class SettingsState(
    // Core profile data
    val username: String = "",
    val profileImageUrl: String? = null,
    val memberSince: String = "2024",
    val workoutCount: Int = 0,
    val totalWeight: Int = 0,
    val currentStreak: Int = 0,
    val personalRecords: List<PersonalRecord> = emptyList(),
    val workoutFrequency: WorkoutFrequency = WorkoutFrequency.ONCE_A_WEEK,

    // Settings
    val theme: AppTheme = AppTheme.DARK,
    val notificationsEnabled: Boolean = true,
    val units: Units = Units.METRIC,
    val language: String = "English",

    // UI States
    val showEditUsernameDialog: Boolean = false,
    val showImagePicker: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showSignOutConfirmation: Boolean = false,
    val showAddPRDialog: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val signOutSuccessfully: Boolean = false,
    val showLogoutDialog: Boolean = false,

    // Backup/Import States
    val showBackupDialog: Boolean = false,
    val showImportDialog: Boolean = false,
    val backupStatus: BackupStatus = BackupStatus.Idle,
    val importStatus: ImportStatus = ImportStatus.Idle,
    val isBackupLoading: Boolean = false,
    val isImportLoading: Boolean = false
)

sealed class SettingsEvent {
    object ToggleEditUsernameDialog : SettingsEvent()
    data class UpdateUsername(val newName: String) : SettingsEvent()
    data class UpdateWorkoutFrequency(val newFrequency: WorkoutFrequency) : SettingsEvent()
    data class UpdatePersonalRecords(val pr: PersonalRecord) : SettingsEvent()
    data class RemovePersonalRecord(val pr: PersonalRecord) : SettingsEvent()
    object TogglePRDialog : SettingsEvent()
    data class ChangeSettingsPicture(val profileImageUrl: String) : SettingsEvent()
    data class ThemeChanged(val theme: AppTheme) : SettingsEvent()
    object NotificationsToggled : SettingsEvent()
    data class UnitsChanged(val units: Units) : SettingsEvent()
    data class LanguageChanged(val language: String) : SettingsEvent()
    object DeleteAccount : SettingsEvent()
    object SignOut : SettingsEvent()
    object ToggleShowLogoutDialog : SettingsEvent()

    object OpenBackupDialog : SettingsEvent()
    object CloseBackupDialog : SettingsEvent()
    object ExportBackupToDownloads : SettingsEvent()
    object OpenImportDialog : SettingsEvent()
    object CloseImportDialog : SettingsEvent()
    object PickFileFromDevice : SettingsEvent()
    data class ImportBackupFromUri(val uri: String) : SettingsEvent()
}

enum class AppTheme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    AUTO("Auto")
}

enum class Units(val displayName: String) {
    METRIC("kg"),
    IMPERIAL("lbs")
}

