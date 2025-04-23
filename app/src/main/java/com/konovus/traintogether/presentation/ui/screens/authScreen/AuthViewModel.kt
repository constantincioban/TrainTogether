package com.konovus.traintogether.presentation.ui.screens.authScreen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konovus.traintogether.data.auth.IAuthHelper
import com.konovus.traintogether.presentation.ui.screens.authScreen.ToastMessage.EmailEmpty
import com.konovus.traintogether.presentation.ui.screens.authScreen.ToastMessage.InvalidEmailFormat
import com.konovus.traintogether.presentation.ui.screens.authScreen.ToastMessage.PasswordEmpty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: IAuthHelper,
) : ViewModel() {

    private val _uiState = mutableStateOf(AuthState())
    val uiState: State<AuthState> = _uiState

    private var messageIsActive = false
    private val _eventChannel = Channel<String>(0)
    val eventChannel = _eventChannel.receiveAsFlow()


    fun onEvent(event: AuthEvent) = viewModelScope.launch {
        when (event) {
            is AuthEvent.OnLoginEmailChange -> onLoginEmailChange(event.email)
            is AuthEvent.OnLoginPasswordChange -> onLoginPasswordChange(event.password)
            is AuthEvent.OnRegisterEmailChange -> onRegisterEmailChange(event.email)
            is AuthEvent.OnRegisterPasswordChange -> onRegisterPasswordChange(event.password)
            is AuthEvent.OnRegisterConfirmPasswordChange -> onRegisterConfirmPasswordChange(event.confirmPassword)

            AuthEvent.ToggleLoginPasswordVisibility -> toggleLoginPasswordVisibility()
            AuthEvent.ToggleRegisterPasswordVisibility -> toggleRegisterPasswordVisibility()
            AuthEvent.ToggleRegisterConfirmPasswordVisibility -> toggleRegisterConfirmPasswordVisibility()

            is AuthEvent.OnError -> setError(event.message)
            AuthEvent.ClearError -> clearError()

            AuthEvent.SubmitLogin -> submitLogin()
            AuthEvent.SubmitRegister -> submitRegister()
            is AuthEvent.OnResetPasswordChange -> onResetPasswordChange(event.email)
            AuthEvent.SubmitResetPassword -> submitResetPassword()
            AuthEvent.ToggleBottomSheet -> toggleBottomSheet()
        }
    }

    private fun toggleIsLoadingLogin() {
        _uiState.value = _uiState.value.copy(isLoadingLogin = !_uiState.value.isLoadingLogin)
    }

    private fun toggleIsLoadingRegister() {
        _uiState.value = _uiState.value.copy(isLoadingRegister = !_uiState.value.isLoadingRegister)
    }

    private fun toggleIsLoadingResetPassword() {
        _uiState.value =
            _uiState.value.copy(isLoadingResetPassword = !_uiState.value.isLoadingResetPassword)
    }

    private fun onResetMessageChange(message: String?) {
        _uiState.value = _uiState.value.copy(resetEmailMessage = message)
    }

    private fun toggleBottomSheet() {
        _uiState.value = _uiState.value.copy(isBottomSheetOpen = !_uiState.value.isBottomSheetOpen)
    }

    private fun onResetPasswordChange(email: String) {
        _uiState.value = _uiState.value.copy(resetPasswordEmail = email)
    }

    private fun submitResetPassword() = viewModelScope.launch {
        if (_uiState.value.resetPasswordEmail.isBlank()) {
            onResetMessageChange(EmailEmpty.message)
            return@launch
        }
        if (!_uiState.value.resetPasswordEmail
                .matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
        ) {
            onResetMessageChange(InvalidEmailFormat.message)
            return@launch
        }
        toggleIsLoadingResetPassword()

        auth.resetPassword(
            email = _uiState.value.resetPasswordEmail,
        ) { success, error ->
            if (success) {
                onResetMessageChange(ToastMessage.ResetEmailSent.message)
            } else {
                onResetMessageChange(ToastMessage.ResetFailed(error.toString()).message)
            }
            toggleIsLoadingResetPassword()

        }
    }

    private fun onLoginEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(loginEmail = email)
    }

    private fun onLoginPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(loginPassword = password)
    }

    private fun onRegisterEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(registerEmail = email)
    }

    private fun onRegisterPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(registerPassword = password)
    }

    private fun onRegisterConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(registerConfirmPassword = confirmPassword)
    }

    private fun toggleLoginPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            loginPasswordVisibility = !_uiState.value.loginPasswordVisibility
        )
    }

    private fun toggleRegisterPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            registerPasswordVisibility = !_uiState.value.registerPasswordVisibility
        )
    }

    private fun toggleRegisterConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            registerConfirmPasswordVisibility = !_uiState.value.registerConfirmPasswordVisibility
        )
    }

    private fun setError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun submitLogin() {
        if (!_uiState.value.loginEmail.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))) {
            sendEvent(InvalidEmailFormat)
            return
        }
        if (_uiState.value.loginPassword.isEmpty()) {
            sendEvent(PasswordEmpty)
            return
        }

        toggleIsLoadingLogin()
        auth.signInWithEmailPassword(
            email = _uiState.value.loginEmail,
            password = _uiState.value.loginPassword,
        ) { success, error ->
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (success) {
                _uiState.value = _uiState.value.copy(loginSuccessful = true)
            } else {
                sendEvent(ToastMessage.AuthFailed(error.toString()))
            }
            toggleIsLoadingLogin()
        }
    }

    private suspend fun submitRegister() {
        if (!validate(
                email = _uiState.value.registerEmail,
                password = _uiState.value.registerPassword,
                confirmPassword = _uiState.value.registerConfirmPassword,
            )
        ) return

        toggleIsLoadingRegister()

        auth.createUserAccount(
            email = _uiState.value.registerEmail,
            password = _uiState.value.registerPassword,
        ) { success, error ->
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (success) {
                _uiState.value = _uiState.value.copy(loginSuccessful = true)
            } else {
                sendEvent(ToastMessage.AuthFailed(error.toString()))
            }
            toggleIsLoadingRegister()
        }
    }

    private fun sendEvent(toast: ToastMessage) = viewModelScope.launch {
        if (!messageIsActive) {
            _eventChannel.send(toast.message)
            messageIsActive = true
            delay(3500)
            messageIsActive = false
        }
    }

    private fun validate(email: String, password: String, confirmPassword: String): Boolean {
        val error = when {
            email.isBlank() -> ToastMessage.EmailEmpty
            password.isBlank() -> ToastMessage.PasswordEmpty
            !email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) -> ToastMessage.InvalidEmailFormat
            password.length < 6 -> ToastMessage.PasswordTooShort
            password != confirmPassword -> ToastMessage.PasswordsDoNotMatch
            else -> null
        }

        error?.let {
            sendEvent(it)
            return false
        }

        return true
    }


}

data class AuthState(
    val loginEmail: String = "",
    val loginPassword: String = "",
    val registerEmail: String = "",
    val registerConfirmPassword: String = "",
    val registerPassword: String = "",
    val loginPasswordVisibility: Boolean = false,
    val registerPasswordVisibility: Boolean = false,
    val registerConfirmPasswordVisibility: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccessful: Boolean = false,
    val resetPasswordEmail: String = "",
    val isBottomSheetOpen: Boolean = false,
    val resetEmailMessage: String? = null,
    val isLoadingLogin: Boolean = false,
    val isLoadingRegister: Boolean = false,
    val isLoadingResetPassword: Boolean = false,
)

sealed interface AuthEvent {
    // Login form changes
    data class OnLoginEmailChange(val email: String) : AuthEvent
    data class OnLoginPasswordChange(val password: String) : AuthEvent
    data object ToggleLoginPasswordVisibility : AuthEvent

    // Register form changes
    data class OnRegisterEmailChange(val email: String) : AuthEvent
    data class OnRegisterPasswordChange(val password: String) : AuthEvent
    data class OnRegisterConfirmPasswordChange(val confirmPassword: String) : AuthEvent
    data object ToggleRegisterPasswordVisibility : AuthEvent
    data object ToggleRegisterConfirmPasswordVisibility : AuthEvent

    data class OnResetPasswordChange(val email: String) : AuthEvent

    // Actions
    data object SubmitLogin : AuthEvent
    data object SubmitRegister : AuthEvent
    data object SubmitResetPassword : AuthEvent
    data object ToggleBottomSheet : AuthEvent

    // Feedback
    data class OnError(val message: String) : AuthEvent
    data object ClearError : AuthEvent
}

sealed interface ToastMessage {
    val message: String

    object EmailEmpty : ToastMessage {
        override val message = "Email cannot be empty"
    }

    object PasswordEmpty : ToastMessage {
        override val message = "Password cannot be empty"
    }

    object InvalidEmailFormat : ToastMessage {
        override val message = "Please enter a valid email address"
    }

    object PasswordTooShort : ToastMessage {
        override val message = "Password must be at least 6 characters"
    }

    object PasswordsDoNotMatch : ToastMessage {
        override val message = "Passwords do not match"
    }

    data class AuthFailed(val reason: String) : ToastMessage {
        override val message = "Authentication failed. $reason"
    }

    object AccountCreated : ToastMessage {
        override val message = "Account successfully created"
    }

    object LoginSuccess : ToastMessage {
        override val message = "Login successful"
    }

    object ResetEmailSent : ToastMessage {
        override val message: String = "Password reset email sent"
    }

    data class ResetFailed(val reason: String) : ToastMessage {
        override val message: String = "Failed to send reset email. $reason"
    }
}
