package com.example.smartmedicinebox.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmedicinebox.data.Medicine
import com.example.smartmedicinebox.domain.ThemeMode
import com.example.smartmedicinebox.ui.theme.Bleu
import com.example.smartmedicinebox.ui.theme.BleuClair
import com.example.smartmedicinebox.ui.theme.BordureChamp
import com.example.smartmedicinebox.ui.theme.Gris
import com.example.smartmedicinebox.ui.theme.Ivoire
import com.example.smartmedicinebox.ui.theme.Rouge
import com.example.smartmedicinebox.ui.theme.Texte

// ==========================================
// NAVIGATION COLORS
// ==========================================

@Composable
internal fun modernNavigationColors() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = Bleu,
        selectedTextColor = Bleu,
        indicatorColor = BleuClair,
        unselectedIconColor = Gris,
        unselectedTextColor = Gris
    )

// ==========================================
// TEXT FIELD COLORS
// ==========================================

@Composable
internal fun modernTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Bleu,
        unfocusedBorderColor = BordureChamp,
        focusedLabelColor = Bleu,
        cursorColor = Bleu,
        focusedContainerColor = Ivoire,
        unfocusedContainerColor = Ivoire
    )

// ==========================================
// LIGHT / DARK SELECTOR
// ==========================================

/** Three-way switch: Clair / Auto (phone setting) / Sombre. */
@Composable
internal fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = BleuClair
    ) {
        Row(Modifier.padding(3.dp)) {
            listOf(
                ThemeMode.Light to "☀ Clair",
                ThemeMode.System to "◐ Auto",
                ThemeMode.Dark to "☾ Sombre"
            ).forEach { (mode, label) ->
                val isSelected = mode == selected
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onSelect(mode) },
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) Bleu else Color.Transparent
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        color = if (isSelected) Color.White else Texte,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ==========================================
// SECTION TITLE
// ==========================================

@Composable
internal fun SectionTitleModern(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            title,
            color = Texte,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp
        )

        Spacer(Modifier.height(3.dp))

        Text(
            subtitle,
            color = Gris,
            fontSize = 11.sp
        )
    }
}

// ==========================================
// PAGE HEADING
// ==========================================

@Composable
internal fun PageHeading(
    title: String,
    subtitle: String
) {
    Text(
        title,
        color = Texte,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(Modifier.height(5.dp))

    Text(
        subtitle,
        color = Gris,
        fontSize = 12.sp
    )
}

// ==========================================
// FORM LABEL
// ==========================================

@Composable
internal fun FormLabel(text: String) {
    Text(
        text,
        color = Texte,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp
    )
}

// ==========================================
// EMPTY INFO CARD
// ==========================================

@Composable
internal fun InfoCardModern(
    symbol: String,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Ivoire
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(23.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = BleuClair
            ) {
                Text(
                    symbol,
                    modifier = Modifier.padding(15.dp),
                    fontSize = 25.sp,
                    color = Bleu
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                title,
                color = Texte,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                subtitle,
                color = Gris,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }
    }
}

// ==========================================
// QUICK ACCESS CARD
// ==========================================

@Composable
internal fun QuickCardModern(
    modifier: Modifier = Modifier,
    symbol: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(142.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(
            containerColor = Ivoire
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BleuClair
            ) {
                Text(
                    symbol,
                    modifier = Modifier.padding(10.dp),
                    fontSize = 22.sp
                )
            }

            Spacer(Modifier.height(11.dp))

            Text(
                title,
                color = Texte,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(3.dp))

            Text(
                subtitle,
                color = Gris,
                fontSize = 10.sp
            )
        }
    }
}

// ==========================================
// MEDICINE CARD
// ==========================================

@Composable
internal fun MedicineCardModern(
    medicine: Medicine,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(
            containerColor = Ivoire
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(15.dp),
                color = BleuClair
            ) {
                Text(
                    "💊",
                    modifier = Modifier.padding(13.dp),
                    fontSize = 23.sp
                )
            }

            Spacer(Modifier.width(13.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    medicine.name,
                    color = Texte,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    "Fréquence : ${medicine.frequency}",
                    color = Gris,
                    fontSize = 11.sp
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BleuClair
                    ) {
                        Text(
                            "◷  ${medicine.time}",
                            modifier = Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 5.dp
                            ),
                            color = Bleu,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete
            ) {
                Text(
                    "✕",
                    color = Rouge,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
