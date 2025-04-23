package com.konovus.traintogether.presentation.ui.screens.authScreen.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.konovus.traintogether.data.utils.observeWithLifecycle
import com.konovus.traintogether.presentation.ui.composables.AnimatedBorderCard
import com.konovus.traintogether.presentation.ui.screens.authScreen.AuthEvent
import com.konovus.traintogether.presentation.ui.screens.authScreen.AuthState
import kotlinx.coroutines.flow.Flow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordBottomSheet(
    snackbarHostState: SnackbarHostState,
    uiState: AuthState,
    sendEvent: (event: AuthEvent) -> Unit,
    eventChannel: Flow<String>,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val primaryColor = MaterialTheme.colorScheme.primary

    eventChannel.observeWithLifecycle {
        snackbarHostState.showSnackbar(message = it)
    }

    ModalBottomSheet(
        onDismissRequest = { sendEvent(AuthEvent.ToggleBottomSheet) },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {

            TextField(
                value = uiState.resetPasswordEmail,
                onValueChange = { sendEvent(AuthEvent.OnResetPasswordChange(it)) },
                label = { Text("Email") },
                placeholder = { Text("Enter your email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = primaryColor,
                    unfocusedIndicatorColor = Color.Gray,
                    disabledIndicatorColor = Color.LightGray,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            )
            AnimatedVisibility(uiState.resetEmailMessage != null) {
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = uiState.resetEmailMessage ?: "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,

                    colors = TextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.error,
                        disabledContainerColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

            }
            Spacer(Modifier.height(32.dp))

            AnimatedBorderCard(
                isLoading = uiState.isLoadingResetPassword,
                shape = RoundedCornerShape(20.dp),
            ) {
                Button(
                    onClick = { sendEvent(AuthEvent.SubmitResetPassword) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Send Password Reset Email")
                }
            }
            Spacer(Modifier.height(32.dp))

            Text(
                text = "Back to login",
                color = primaryColor,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        sendEvent(AuthEvent.ToggleBottomSheet)
                    }
            )

            Spacer(Modifier.height(64.dp))
        }
    }
}
