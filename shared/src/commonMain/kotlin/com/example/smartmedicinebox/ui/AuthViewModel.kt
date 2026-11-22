package com.example.smartmedicinebox.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartmedicinebox.data.User
import com.example.smartmedicinebox.domain.AuthRepository
import com.example.smartmedicinebox.domain.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SessionState {
    /** Reading the saved login from the database (a split second at start-up). */
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class LoggedIn(val user: User) : SessionState
}

data class AuthFormState(
    val busy: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val auth: AuthRepository
) : ViewModel() {

    val session: StateFlow<SessionState> =
        auth.currentUser
            .map { user -> if (user == null) SessionState.LoggedOut else SessionState.LoggedIn(user) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    private val _form = MutableStateFlow(AuthFormState())
    val form: StateFlow<AuthFormState> = _form.asStateFlow()

    fun signUp(email: String, username: String, password: String, confirmPassword: String) =
        submit { auth.signUp(email, username, password, confirmPassword) }

    fun logIn(identifier: String, password: String) =
        submit { auth.logIn(identifier, password) }

    fun logOut() {
        viewModelScope.launch { auth.logOut() }
    }

    fun clearError() = _form.update { it.copy(error = null) }

    private fun submit(action: suspend () -> AuthResult) {
        if (_form.value.busy) return
        _form.value = AuthFormState(busy = true)
        viewModelScope.launch {
            val result = action()
            // On success the session flow switches the app to the main screens.
            _form.value = AuthFormState(error = (result as? AuthResult.Error)?.message)
        }
    }
}
