package com.example.smartmedicinebox.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmedicinebox.data.Medicine
import com.example.smartmedicinebox.domain.BoxState
import com.example.smartmedicinebox.domain.DateFormats
import com.example.smartmedicinebox.domain.ReminderTime
import com.example.smartmedicinebox.ui.components.InfoCardModern
import com.example.smartmedicinebox.ui.components.QuickCardModern
import com.example.smartmedicinebox.ui.components.SectionTitleModern
import com.example.smartmedicinebox.ui.theme.Bleu
import com.example.smartmedicinebox.ui.theme.BleuClair
import com.example.smartmedicinebox.ui.theme.Gris
import com.example.smartmedicinebox.ui.theme.Ivoire
import com.example.smartmedicinebox.ui.theme.Rouge
import com.example.smartmedicinebox.ui.theme.RougeClair
import com.example.smartmedicinebox.ui.theme.Texte
import com.example.smartmedicinebox.ui.theme.Vert
import com.example.smartmedicinebox.ui.theme.VertClair
import kotlinx.coroutines.delay
import kotlin.time.Clock

@Composable
internal fun HomeScreen(
    username: String,
    medicines: List<Medicine>,
    boxState: BoxState,
    onConnectBox: () -> Unit,
    onDisconnectBox: () -> Unit,
    onTestBox: () -> Unit,
    onOpenMedicines: () -> Unit,
    onOpenHistory: () -> Unit,
    onAddMedicine: () -> Unit
) {
    // Countdown refresh
    var nowMillis by remember {
        mutableLongStateOf(Clock.System.now().toEpochMilliseconds())
    }

    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = Clock.System.now().toEpochMilliseconds()
            delay(1000)
        }
    }

    Column {

        Text(
            "Bonjour, $username ! 👋",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Texte
        )

        Spacer(Modifier.height(4.dp))

        Text(
            "Voici votre tableau de bord.",
            fontSize = 13.sp,
            color = Gris
        )

        Spacer(Modifier.height(22.dp))

        DashboardCard(medicineCount = medicines.size)

        Spacer(Modifier.height(25.dp))

        // Upcoming reminder calculation

        val nextReminder = medicines.mapNotNull { medicine ->
            ReminderTime.nextOccurrenceMillis(medicine.time, nowMillis)
                ?.let { medicine to it }
        }.minByOrNull { it.second }

        SectionTitleModern(
            title = "Prochain rappel",
            subtitle = "Votre prochaine prise programmée"
        )

        Spacer(Modifier.height(12.dp))

        if (nextReminder != null) {
            NextReminderCard(
                medicine = nextReminder.first,
                atMillis = nextReminder.second,
                nowMillis = nowMillis
            )
        } else {
            InfoCardModern(
                symbol = "◷",
                title = "Aucun rappel programmé",
                subtitle = "Ajoutez un médicament avec une heure valide pour afficher le prochain rappel."
            )
        }

        Spacer(Modifier.height(26.dp))

        // ESP32 section

        SectionTitleModern(
            title = "Votre boîte connectée",
            subtitle = "Connexion avec votre appareil SmartMed"
        )

        Spacer(Modifier.height(12.dp))

        BoxCard(
            boxState = boxState,
            onConnect = onConnectBox,
            onDisconnect = onDisconnectBox,
            onTest = onTestBox
        )

        Spacer(Modifier.height(26.dp))

        // Quick access

        SectionTitleModern(
            title = "Accès rapide",
            subtitle = "Accédez facilement aux fonctionnalités"
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickCardModern(
                modifier = Modifier.weight(1f),
                symbol = "💊",
                title = "Médicaments",
                subtitle = "Gérer la liste",
                onClick = onOpenMedicines
            )

            QuickCardModern(
                modifier = Modifier.weight(1f),
                symbol = "◷",
                title = "Historique",
                subtitle = "Voir les prises",
                onClick = onOpenHistory
            )
        }

        Spacer(Modifier.height(20.dp))

        // Add medicine button

        Button(
            onClick = onAddMedicine,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Bleu
            )
        ) {
            Text(
                "+ Ajouter un médicament",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(15.dp))

        ImportantNoteCard()
    }
}

// ==========================================
// MAIN DASHBOARD CARD
// ==========================================

@Composable
private fun DashboardCard(medicineCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(27.dp),
        colors = CardDefaults.cardColors(
            containerColor = Bleu
        ),
        elevation = CardDefaults.cardElevation(5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(23.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "SMART HEALTH",
                        color = Color.White,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        ),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(Modifier.weight(1f))

                Text(
                    "✦",
                    color = Color.White,
                    fontSize = 24.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Prenez soin de vous.",
                fontSize = 25.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Gardez vos rappels et vos médicaments bien organisés.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 19.sp
            )

            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$medicineCount",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(10.dp))

                Column {
                    Text(
                        "Médicaments",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "enregistrés",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White
                ) {
                    Text(
                        "💊",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 23.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// NEXT REMINDER CARD
// ==========================================

@Composable
private fun NextReminderCard(
    medicine: Medicine,
    atMillis: Long,
    nowMillis: Long
) {
    val countdown = DateFormats.countdown(atMillis - nowMillis)
    val dateText = DateFormats.date(atMillis)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Ivoire
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(19.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(15.dp),
                    color = BleuClair
                ) {
                    Text(
                        "💊",
                        modifier = Modifier.padding(13.dp),
                        fontSize = 24.sp
                    )
                }

                Spacer(Modifier.width(13.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        medicine.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Texte
                    )

                    Text(
                        "Prochaine prise",
                        fontSize = 11.sp,
                        color = Gris
                    )
                }

                Surface(
                    color = BleuClair,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        medicine.time,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        ),
                        color = Bleu,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(21.dp))

            HorizontalDivider(
                color = BleuClair
            )

            Spacer(Modifier.height(17.dp))

            Text(
                "TEMPS RESTANT",
                color = Gris,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(Modifier.height(7.dp))

            Text(
                countdown,
                color = Vert,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(7.dp))

            Text(
                "Date prévue : $dateText",
                color = Gris,
                fontSize = 11.sp
            )
        }
    }
}

// ==========================================
// ESP32 BOX CARD
// ==========================================

@Composable
private fun BoxCard(
    boxState: BoxState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onTest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(
            containerColor = Ivoire
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = BleuClair
                ) {
                    Text(
                        "⌁",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 25.sp,
                        color = Bleu
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "ESP32 SmartMed_Box",
                        color = Texte,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Text(
                        boxState.message,
                        color = if (boxState.connected) Vert else Gris,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (boxState.connected) VertClair else BleuClair
                ) {
                    Box(
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(17.dp))

            Button(
                onClick = onConnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(49.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Bleu
                )
            ) {
                Text(
                    "Connecter ESP32",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(9.dp))

            OutlinedButton(
                onClick = onDisconnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(47.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Bleu
                )
            ) {
                Text("Déconnecter")
            }

            Spacer(Modifier.height(9.dp))

            Button(
                onClick = onTest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(47.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Vert
                )
            ) {
                Text(
                    "Tester le rappel",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ==========================================
// REMINDER NOTE
// ==========================================

@Composable
private fun ImportantNoteCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = RougeClair
        )
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "!",
                color = Rouge,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    "Information importante",
                    color = Rouge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Text(
                    "Pensez à vérifier vos rappels dans l'application.",
                    color = Texte,
                    fontSize = 11.sp
                )
            }
        }
    }
}
