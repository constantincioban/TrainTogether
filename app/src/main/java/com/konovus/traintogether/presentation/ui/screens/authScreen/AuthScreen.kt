package com.konovus.traintogether.presentation.ui.screens.authScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AuthScreen(
    navigateTo: (route: String, params: List<Any>) -> Unit,
) {

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
        ) {
            tabs.forEachIndexed { index, tabItem ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = tabItem.title) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTabIndex == index) {
                                tabItem.selectedIcon
                            } else tabItem.unselectedIcon,
                            contentDescription = "Auth"
                        )
                    }
                )
            }
        }
    }
}

val tabs = listOf(
    TabItem(
        title = "Sign In",
        unselectedIcon = Icons.AutoMirrored.Outlined.Login,
        selectedIcon = Icons.AutoMirrored.Filled.Login
    ),
    TabItem(
        title = "Sign Up",
        unselectedIcon = Icons.Outlined.Create,
        selectedIcon = Icons.Filled.Create,
    )
)

data class TabItem(
    val title: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
)