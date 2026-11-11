package com.konovus.traintogether.presentation.ui.screens.settingsScreen

import ImportDataDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.konovus.traintogether.data.local.models.PersonalRecord
import com.konovus.traintogether.navigation.Screens
import com.konovus.traintogether.presentation.ui.composables.LogoutConfirmationDialog
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.components.AccountSection
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.components.AddPRDialog
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.components.BackupDataDialog
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.components.EditNameDialog
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.components.PreferencesSection
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.components.SettingsHeaderSection
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.components.TrainingSetupSection


@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    navigateAndClearStack: (String, List<Any>) -> Unit,
) {
    val uiState = viewModel.uiState.value
    val onEvent = viewModel::onEvent

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onEvent(SettingsEvent.ImportBackupFromUri(it.toString()))
        }
    }

    LogoutConfirmationDialog(
        showDialog = uiState.showLogoutDialog,
        onDismiss = { onEvent(SettingsEvent.ToggleShowLogoutDialog) },
        onConfirm = { onEvent(SettingsEvent.SignOut) }
    )

    if (uiState.showEditUsernameDialog) {
        EditNameDialog(
            currentName = uiState.username,
            onDismiss = { onEvent(SettingsEvent.ToggleEditUsernameDialog) },
            onConfirm = { newName ->
                onEvent(SettingsEvent.UpdateUsername(newName))
                onEvent(SettingsEvent.ToggleEditUsernameDialog)
            }
        )
    }

    if (uiState.showAddPRDialog) {
        AddPRDialog(
            units = uiState.units,
            onDismiss = { onEvent(SettingsEvent.TogglePRDialog) },
            onConfirm = { exercise, weight, reps ->
                onEvent(
                    SettingsEvent.UpdatePersonalRecords(
                        PersonalRecord(
                            exercise = exercise,
                            weight = weight,
                            reps = reps
                        )
                    )
                )
            }
        )
    }

    BackupDataDialog(
        showDialog = uiState.showBackupDialog,
        onDismiss = { onEvent(SettingsEvent.CloseBackupDialog) },
        isLoading = uiState.isBackupLoading,
        backupStatus = uiState.backupStatus,
        onExportToDownloads = { onEvent(SettingsEvent.ExportBackupToDownloads) }
    )

    ImportDataDialog(
        showDialog = uiState.showImportDialog,
        onDismiss = { onEvent(SettingsEvent.CloseImportDialog) },
        isLoading = uiState.isImportLoading,
        importStatus = uiState.importStatus,
        onPickFile = { filePickerLauncher.launch("application/json") }
    )

    LaunchedEffect(uiState.signOutSuccessfully) {
        if (uiState.signOutSuccessfully) {
            navigateAndClearStack(Screens.Auth.route, emptyList())
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 122.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            SettingsHeaderSection(
                name = uiState.username,
                profileImageUrl = uiState.profileImageUrl,
                memberSince = uiState.memberSince,
                onEditProfile = { onEvent(SettingsEvent.ToggleEditUsernameDialog) },
                onChangeProfilePicture = { onEvent(SettingsEvent.ChangeSettingsPicture(it)) },
                onBackClick = { navigateAndClearStack(Screens.Home.route, emptyList()) }
            )
        }

        item {
            TrainingSetupSection(
                workoutFrequency = uiState.workoutFrequency,
                exercisePRs = uiState.personalRecords,
                onFrequencyChange = { onEvent(SettingsEvent.UpdateWorkoutFrequency(it)) },
                onAddPR = { onEvent(SettingsEvent.TogglePRDialog) },
                onRemovePR = { pr ->
                    onEvent(SettingsEvent.RemovePersonalRecord(pr))
                }
            )
        }

        item {
            PreferencesSection(
                theme = uiState.theme,
                notifications = uiState.notificationsEnabled,
                units = uiState.units,
                language = uiState.language,
                onThemeChange = { onEvent(SettingsEvent.ThemeChanged(it)) },
                onNotificationsToggle = { onEvent(SettingsEvent.NotificationsToggled) },
                onUnitsChange = { onEvent(SettingsEvent.UnitsChanged(it)) },
                onLanguageChange = { onEvent(SettingsEvent.LanguageChanged(it)) }
            )
        }

        item {
            AccountSection(
                onBackupData = { onEvent(SettingsEvent.OpenBackupDialog) },
                onImportData = { onEvent(SettingsEvent.OpenImportDialog) },
                onDeleteAccount = { onEvent(SettingsEvent.DeleteAccount) },
                onSignOut = { onEvent(SettingsEvent.ToggleShowLogoutDialog) }
            )
        }

    }
}


// Helper function to get exercise emojis
fun getExerciseEmoji(exerciseName: String): String {
    return when {
        exerciseName.contains("bench", ignoreCase = true) -> "💪"
        exerciseName.contains("squat", ignoreCase = true) -> "🦵"
        exerciseName.contains("deadlift", ignoreCase = true) -> "🏋️"
        exerciseName.contains("pull", ignoreCase = true) -> "🔙"
        exerciseName.contains("press", ignoreCase = true) -> "⬆️"
        exerciseName.contains("curl", ignoreCase = true) -> "💪"
        exerciseName.contains("row", ignoreCase = true) -> "🚣"
        else -> "🏋️"
    }
}
