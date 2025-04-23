package com.konovus.traintogether.presentation.ui.screens.authScreen.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.konovus.traintogether.presentation.ui.composables.AnimatedBorderCard
import com.konovus.traintogether.presentation.ui.screens.authScreen.AuthEvent
import com.konovus.traintogether.presentation.ui.screens.authScreen.AuthState

@Composable
fun LoginForm(
    sendEvent: (event: AuthEvent) -> Unit,
    uiState: AuthState,
) {

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Email Field
        TextField(
            value = uiState.loginEmail,
            onValueChange = { sendEvent(AuthEvent.OnLoginEmailChange(it)) },
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

        // Password Field with visibility toggle
        TextField(
            value = uiState.loginPassword,
            onValueChange = { sendEvent(AuthEvent.OnLoginPasswordChange(it)) },
            label = { Text("Password") },
            placeholder = { Text("Enter your password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (uiState.loginPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (uiState.loginPasswordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { sendEvent(AuthEvent.ToggleLoginPasswordVisibility) }) {
                    Icon(icon, contentDescription = if (uiState.loginPasswordVisibility) "Hide password" else "Show password")
                }
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = primaryColor,
                unfocusedIndicatorColor = Color.Gray,
                disabledIndicatorColor = Color.LightGray,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedBorderCard(
            isLoading = uiState.isLoadingLogin,
            shape = RoundedCornerShape(20.dp),
        ) {
            Button(
                onClick = { sendEvent(AuthEvent.SubmitLogin) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Sign In")
            }
        }

        // Forgot Password Text
        Text(
            text = "Forgot your password?",
            color = primaryColor,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { sendEvent(AuthEvent.ToggleBottomSheet) }
        )
    }
}