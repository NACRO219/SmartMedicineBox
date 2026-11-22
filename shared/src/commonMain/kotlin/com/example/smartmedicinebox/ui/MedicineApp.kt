package com.example.smartmedicinebox.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmedicinebox.data.User
import com.example.smartmedicinebox.ui.components.modernNavigationColors
import com.example.smartmedicinebox.ui.screens.AddMedicineScreen
import com.example.smartmedicinebox.ui.screens.HistoryScreen
import com.example.smartmedicinebox.ui.screens.HomeScreen
import com.example.smartmedicinebox.ui.screens.MedicinesScreen
import com.example.smartmedicinebox.ui.theme.Bleu
import com.example.smartmedicinebox.ui.theme.BleuClair
import com.example.smartmedicinebox.ui.theme.Fond
import com.example.smartmedicinebox.ui.theme.Gris
import com.example.smartmedicinebox.ui.theme.Ivoire
import com.example.smartmedicinebox.ui.theme.Rouge
import com.example.smartmedicinebox.ui.theme.Texte

enum class Screen { Accueil, Medicaments, Historique, Ajouter }

@Composable
fun MedicineApp(
    viewModel: MedicineViewModel,
    user: User,
    onLogOut: () -> Unit
) {

    val medicines by viewModel.medicines.collectAsState()
    val history by viewModel.history.collectAsState()
    val boxState by viewModel.boxState.collectAsState()

    var screen by remember { mutableStateOf(Screen.Accueil) }

    Scaffold(
        containerColor = Fond,

        bottomBar = {
            if (screen != Screen.Ajouter) {
                NavigationBar(
                    containerColor = Ivoire,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = screen == Screen.Accueil,
                        onClick = { screen = Screen.Accueil },
                        icon = { Text("⌂", fontSize = 23.sp) },
                        label = { Text("Accueil", fontSize = 11.sp) },
                        colors = modernNavigationColors()
                    )

                    NavigationBarItem(
                        selected = screen == Screen.Medicaments,
                        onClick = { screen = Screen.Medicaments },
                        icon = { Text("✚", fontSize = 21.sp) },
                        label = { Text("Médicaments", fontSize = 11.sp) },
                        colors = modernNavigationColors()
                    )

                    NavigationBarItem(
                        selected = screen == Screen.Historique,
                        onClick = { screen = Screen.Historique },
                        icon = { Text("◷", fontSize = 23.sp) },
                        label = { Text("Historique", fontSize = 11.sp) },
                        colors = modernNavigationColors()
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 18.dp, bottom = 25.dp)
        ) {

            AppHeader(user = user, onLogOut = onLogOut)

            Spacer(Modifier.height(27.dp))

            when (screen) {
                Screen.Accueil -> HomeScreen(
                    username = user.username,
                    medicines = medicines,
                    boxState = boxState,
                    onConnectBox = viewModel::connectBox,
                    onDisconnectBox = viewModel::disconnectBox,
                    onTestBox = viewModel::sendTestReminder,
                    onOpenMedicines = { screen = Screen.Medicaments },
                    onOpenHistory = { screen = Screen.Historique },
                    onAddMedicine = { screen = Screen.Ajouter }
                )

                Screen.Medicaments -> MedicinesScreen(
                    medicines = medicines,
                    onAddMedicine = { screen = Screen.Ajouter },
                    onDeleteMedicine = viewModel::deleteMedicine
                )

                Screen.Historique -> HistoryScreen(
                    history = history
                )

                Screen.Ajouter -> AddMedicineScreen(
                    onSubmit = viewModel::addMedicine,
                    onDone = { screen = Screen.Medicaments }
                )
            }

            Spacer(Modifier.height(15.dp))
        }
    }
}

// ==========================================
// TOP HEADER
// ==========================================

@Composable
private fun AppHeader(
    user: User,
    onLogOut: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "SmartMed",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Bleu
            )

            Text(
                "Votre santé, notre priorité",
                fontSize = 11.sp,
                color = Gris
            )
        }

        // Account button: the user's initial; opens a menu with "Se déconnecter".
        Box {
            Surface(
                modifier = Modifier.clickable { menuOpen = true },
                shape = CircleShape,
                color = Ivoire,
                shadowElevation = 3.dp
            ) {
                Box(
                    modifier = Modifier.size(47.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user.username.take(1).uppercase(),
                        color = Bleu,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                containerColor = Ivoire
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        user.username,
                        color = Texte,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        user.email,
                        color = Gris,
                        fontSize = 11.sp
                    )
                }
                HorizontalDivider(color = BleuClair)
                DropdownMenuItem(
                    text = {
                        Text(
                            "Se déconnecter",
                            color = Rouge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    onClick = {
                        menuOpen = false
                        onLogOut()
                    }
                )
            }
        }
    }
}
