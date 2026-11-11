package com.konovus.traintogether.presentation.ui.screens.settingsScreen.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Composable
fun AccountSection(
    onBackupData: () -> Unit,
    onImportData: () -> Unit,
    onDeleteAccount: () -> Unit,
    onSignOut: () -> Unit
) {
    SectionCard(title = "ACCOUNT") {
        PreferenceItem(
            title = "Backup Data",
            subtitle = "Save your workout data",
            icon = "☁️",
            onClick = onBackupData
        )

        PreferenceItem(
            title = "Import Data",
            subtitle = "Restore previous workout data",
            icon = "📊",
            onClick = onImportData
        )

        PreferenceItem(
            title = "Sign Out",
            subtitle = "Log out of your account",
            icon = "🚪",
            onClick = onSignOut,
            textColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )

        PreferenceItem(
            title = "Delete Account",
            subtitle = "Permanently remove account",
            icon = "⚠️",
            onClick = onDeleteAccount,
            textColor = Color(0xFFE57373)
        )
    }
}
