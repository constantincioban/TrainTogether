package com.konovus.traintogether.presentation.ui.screens.authScreen.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.konovus.traintogether.presentation.ui.composables.AnimatedBorderCard
import com.konovus.traintogether.presentation.ui.screens.authScreen.AuthEvent
import com.konovus.traintogether.presentation.ui.screens.authScreen.AuthState


@Composable
fun CreateAccountForm(
    sendEvent: (event: AuthEvent) -> Unit,
    uiState: AuthState,
) {
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Email
        TextField(
            value = uiState.registerEmail,
            onValueChange = { sendEvent(AuthEvent.OnRegisterEmailChange(it)) },
            label = { Text("Email") },
            placeholder = { Text("Enter your email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() }
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = primaryColor,
                unfocusedIndicatorColor = Color.Gray,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        // Password
        TextField(
            value = uiState.registerPassword,
            onValueChange = { sendEvent(AuthEvent.OnRegisterPasswordChange(it)) },
            label = { Text("Password") },
            placeholder = { Text("Enter your password") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester),
            singleLine = true,
            visualTransformation = if (uiState.registerPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (uiState.registerPasswordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { sendEvent(AuthEvent.ToggleRegisterPasswordVisibility) }) {
                    Icon(icon, contentDescription = null)
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { confirmPasswordFocusRequester.requestFocus() }
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = primaryColor,
                unfocusedIndicatorColor = Color.Gray,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        // Confirm Password
        TextField(
            value = uiState.registerConfirmPassword,
            onValueChange = { sendEvent(AuthEvent.OnRegisterConfirmPasswordChange(it)) },
            label = { Text("Confirm Password") },
            placeholder = { Text("Confirm your password") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(confirmPasswordFocusRequester),
            singleLine = true,
            visualTransformation = if (uiState.registerConfirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (uiState.registerConfirmPasswordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { sendEvent(AuthEvent.ToggleRegisterConfirmPasswordVisibility) }) {
                    Icon(icon, contentDescription = null)
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = primaryColor,
                unfocusedIndicatorColor = Color.Gray,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedBorderCard(
            isLoading = uiState.isLoadingRegister,
            shape = RoundedCornerShape(20.dp),
        ) {
            Button(
                onClick = { sendEvent(AuthEvent.SubmitRegister) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Create Account")
            }
        }
    }
}
