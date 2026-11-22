package com.example.smartmedicinebox.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmedicinebox.ui.components.FormLabel
import com.example.smartmedicinebox.ui.components.modernTextFieldColors
import com.example.smartmedicinebox.ui.theme.Bleu
import com.example.smartmedicinebox.ui.theme.Gris
import com.example.smartmedicinebox.ui.theme.Ivoire
import com.example.smartmedicinebox.ui.theme.Rouge
import com.example.smartmedicinebox.ui.theme.Texte

/**
 * @param onSubmit saves the medicine; returns an error message, or null on success.
 * @param onDone   leaves the screen (after saving or cancelling).
 */
@Composable
internal fun AddMedicineScreen(
    onSubmit: (name: String, time: String, frequency: String) -> String?,
    onDone: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.clickable { onDone() },
                shape = CircleShape,
                color = Ivoire
            ) {
                Text(
                    "‹",
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 5.dp
                    ),
                    fontSize = 30.sp,
                    color = Bleu
                )
            }

            Spacer(Modifier.width(13.dp))

            Column {
                Text(
                    "Nouveau médicament",
                    fontSize = 21.sp,
                    color = Texte,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Ajoutez les informations",
                    color = Gris,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(Modifier.height(23.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(23.dp),
            colors = CardDefaults.cardColors(
                containerColor = Ivoire
            ),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(
                modifier = Modifier.padding(19.dp)
            ) {
                Text(
                    "Informations du médicament",
                    color = Texte,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(Modifier.height(20.dp))

                FormLabel("Nom du médicament")

                Spacer(Modifier.height(7.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex : Paracétamol") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = modernTextFieldColors()
                )

                Spacer(Modifier.height(17.dp))

                FormLabel("Heure de prise")

                Spacer(Modifier.height(7.dp))

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex : 08:00") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = modernTextFieldColors()
                )

                Spacer(Modifier.height(17.dp))

                FormLabel("Fréquence")

                Spacer(Modifier.height(7.dp))

                OutlinedTextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex : 1 fois par jour") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = modernTextFieldColors()
                )

                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(13.dp))

                    Text(
                        error,
                        color = Rouge,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(22.dp))

                Button(
                    onClick = {
                        val result = onSubmit(name, time, frequency)
                        if (result == null) onDone() else error = result
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(53.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Bleu
                    )
                ) {
                    Text(
                        "Enregistrer",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(9.dp))

                OutlinedButton(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(49.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Bleu
                    )
                ) {
                    Text("Annuler")
                }
            }
        }
    }
}
