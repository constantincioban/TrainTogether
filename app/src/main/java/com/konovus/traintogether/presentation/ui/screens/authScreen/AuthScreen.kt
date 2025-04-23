package com.konovus.traintogether.presentation.ui.screens.authScreen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.konovus.traintogether.R
import com.konovus.traintogether.data.navigation.Screens
import com.konovus.traintogether.data.utils.observeWithLifecycle
import kotlinx.coroutines.launch
import com.konovus.traintogether.presentation.ui.screens.authScreen.composables.CreateAccountForm
import com.konovus.traintogether.presentation.ui.screens.authScreen.composables.ForgotPasswordBottomSheet
import com.konovus.traintogether.presentation.ui.screens.authScreen.composables.LoginForm

@Composable
fun AuthScreen(
    snackbarHostState: SnackbarHostState,
    navigateTo: (route: String, params: List<Any>) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {

    val uiState = viewModel.uiState.value

    val pagerState = rememberPagerState(0) { tabs.size }
    val scope = rememberCoroutineScope()
    var selectedTabIndex = pagerState.currentPage

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            selectedTabIndex = pagerState.targetPage
        }
    }

    LaunchedEffect(uiState.loginSuccessful) {
        if (uiState.loginSuccessful) {
            navigateTo(Screens.Home.route, emptyList())
        }
    }

    if (uiState.isBottomSheetOpen) {
        ForgotPasswordBottomSheet(
            snackbarHostState = snackbarHostState,
            uiState = uiState,
            sendEvent = viewModel::onEvent,
            eventChannel = viewModel.eventChannel,
        )
    }

    viewModel.eventChannel.observeWithLifecycle {
        snackbarHostState.showSnackbar(message = it)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
            ,
            contentAlignment = Alignment.Center

        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_tt),
                contentDescription = "TrainTogether Logo",
                modifier = Modifier
                    .size(196.dp)
            )

        }
        TabRow(
            selectedTabIndex = selectedTabIndex,
        ) {
            tabs.forEachIndexed { index, tabItem ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                page = index,
                                animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
                            )
                            selectedTabIndex = index
                        } },
                    text = { Text(text = tabItem.title) },
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                when (page) {
                    0 -> LoginForm(
                        sendEvent = viewModel::onEvent,
                        uiState = uiState,
                    )
                    1 -> CreateAccountForm(
                        sendEvent = viewModel::onEvent,
                        uiState = uiState
                    )
                }
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
        title = "Create Account",
        unselectedIcon = Icons.Outlined.Create,
        selectedIcon = Icons.Filled.Create,
    )
)

data class TabItem(
    val title: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
)