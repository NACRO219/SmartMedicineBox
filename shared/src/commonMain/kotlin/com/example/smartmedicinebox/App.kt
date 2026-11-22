package com.example.smartmedicinebox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartmedicinebox.domain.AuthRepository
import com.example.smartmedicinebox.domain.BoxConnection
import com.example.smartmedicinebox.domain.MedicineRepository
import com.example.smartmedicinebox.ui.AuthViewModel
import com.example.smartmedicinebox.ui.MedicineApp
import com.example.smartmedicinebox.ui.MedicineViewModel
import com.example.smartmedicinebox.ui.SessionState
import com.example.smartmedicinebox.ui.screens.AuthScreen
import com.example.smartmedicinebox.ui.theme.Fond
import com.example.smartmedicinebox.ui.theme.SmartMedTheme

/**
 * Root of the app, used by both platforms:
 *  - Android: MainActivity.setContent { App(...) }
 *  - iPhone:  ComposeUIViewController { App(...) } (phase 4)
 *
 * Shows the login / sign-up screen until someone is logged in.
 * The login is remembered, so reopening the app goes straight to the medicines.
 */
@Composable
fun App(
    repository: MedicineRepository,
    authRepository: AuthRepository,
    box: BoxConnection
) {
    val authViewModel = viewModel { AuthViewModel(authRepository) }
    val session by authViewModel.session.collectAsState()
    val form by authViewModel.form.collectAsState()

    SmartMedTheme {
        when (val s = session) {
            // Blank for a split second while the saved login is read (avoids a login-screen flash).
            SessionState.Loading -> Box(Modifier.fillMaxSize().background(Fond))

            SessionState.LoggedOut -> AuthScreen(
                form = form,
                onLogIn = authViewModel::logIn,
                onSignUp = authViewModel::signUp,
                onClearError = authViewModel::clearError
            )

            is SessionState.LoggedIn -> {
                val viewModel = viewModel { MedicineViewModel(repository, box) }
                MedicineApp(
                    viewModel = viewModel,
                    user = s.user,
                    onLogOut = authViewModel::logOut
                )
            }
        }
    }
}
