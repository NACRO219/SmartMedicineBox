package com.example.smartmedicinebox.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmedicinebox.data.Medicine
import com.example.smartmedicinebox.ui.components.InfoCardModern
import com.example.smartmedicinebox.ui.components.MedicineCardModern
import com.example.smartmedicinebox.ui.components.PageHeading
import com.example.smartmedicinebox.ui.theme.Bleu
import com.example.smartmedicinebox.ui.theme.Gris

@Composable
internal fun MedicinesScreen(
    medicines: List<Medicine>,
    onAddMedicine: () -> Unit,
    onDeleteMedicine: (Medicine) -> Unit
) {
    Column {

        PageHeading(
            title = "Mes médicaments",
            subtitle = "Gérez vos médicaments et leurs rappels."
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "${medicines.size} médicament(s)",
                    color = Bleu,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Text(
                    "Dans votre liste",
                    color = Gris,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onAddMedicine,
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Bleu
                )
            ) {
                Text("+ Ajouter")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (medicines.isEmpty()) {
            InfoCardModern(
                symbol = "💊",
                title = "Votre liste est vide",
                subtitle = "Ajoutez votre premier médicament pour le retrouver ici."
            )
        }

        medicines.forEach { medicine ->
            MedicineCardModern(
                medicine = medicine,
                onDelete = { onDeleteMedicine(medicine) }
            )

            Spacer(Modifier.height(11.dp))
        }
    }
}
