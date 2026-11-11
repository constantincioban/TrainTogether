package com.konovus.traintogether.presentation.ui.screens.settingsScreen.components

import androidx.compose.runtime.Composable
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.AppTheme
import com.konovus.traintogether.presentation.ui.screens.settingsScreen.Units


@Composable
fun PreferencesSection(
    theme: AppTheme,
    notifications: Boolean,
    units: Units,
    language: String,
    onThemeChange: (AppTheme) -> Unit,
    onNotificationsToggle: () -> Unit,
    onUnitsChange: (Units) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    SectionCard(title = "PREFERENCES") {
        PreferenceItem(
            title = "Theme",
            subtitle = theme.displayName,
            icon = "🎨",
            onClick = {
                val themes = AppTheme.values()
                val currentIndex = themes.indexOf(theme)
                val nextTheme = themes[(currentIndex + 1) % themes.size]
                onThemeChange(nextTheme)
            }
        )

        CustomToggle(
            title = "Notifications",
            subtitle = if (notifications) "Enabled" else "Disabled",
            icon = "🔔",
            checked = notifications,
            onToggle = onNotificationsToggle
        )

        PreferenceItem(
            title = "Units",
            subtitle = units.displayName,
            icon = "⚖️",
            onClick = {
                val nextUnits = if (units == Units.METRIC) Units.IMPERIAL else Units.METRIC
                onUnitsChange(nextUnits)
            }
        )

        PreferenceItem(
            title = "Language",
            subtitle = language,
            icon = "🌐",
            onClick = { onLanguageChange("Next Language") }
        )
    }
}
