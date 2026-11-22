package com.example.smartmedicinebox.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmedicinebox.data.Intake
import com.example.smartmedicinebox.domain.DateFormats
import com.example.smartmedicinebox.ui.components.InfoCardModern
import com.example.smartmedicinebox.ui.components.PageHeading
import com.example.smartmedicinebox.ui.theme.Bleu
import com.example.smartmedicinebox.ui.theme.Gris
import com.example.smartmedicinebox.ui.theme.Ivoire
import com.example.smartmedicinebox.ui.theme.Texte
import com.example.smartmedicinebox.ui.theme.Vert
import com.example.smartmedicinebox.ui.theme.VertClair

@Composable
internal fun HistoryScreen(
    history: List<Intake>
) {
    Column {

        PageHeading(
            title = "Historique",
            subtitle = "Retrouvez les confirmations enregistrées."
        )

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(19.dp),
            colors = CardDefaults.cardColors(
                containerColor = Bleu
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Suivi des prises",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        "Vos confirmations enregistrées",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }

                Text(
                    "◷",
                    color = Color.White,
                    fontSize = 31.sp
                )
            }
        }

        Spacer(Modifier.height(15.dp))

        if (history.isEmpty()) {
            InfoCardModern(
                symbol = "◷",
                title = "Aucune confirmation",
                subtitle = "Les confirmations enregistrées apparaîtront ici."
            )
        } else {
            // Already sorted newest first by the database query
            history.forEach { entry ->
                HistoryEntryCard(entry)

                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun HistoryEntryCard(entry: Intake) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(
            containerColor = Ivoire
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(13.dp),
                color = VertClair
            ) {
                Text(
                    "✓",
                    modifier = Modifier.padding(13.dp),
                    color = Vert,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(13.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    entry.medicineName,
                    color = Texte,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    DateFormats.dateTime(entry.takenAt) +
                            (entry.scheduledTime?.let { "  ·  prévu à $it" } ?: ""),
                    color = Gris,
                    fontSize = 11.sp
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    "Prise confirmée",
                    color = Vert,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
