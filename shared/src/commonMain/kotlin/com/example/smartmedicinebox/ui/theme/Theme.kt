package com.example.smartmedicinebox.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.smartmedicinebox.resources.Res
import com.example.smartmedicinebox.resources.poppins_bold
import com.example.smartmedicinebox.resources.poppins_regular
import com.example.smartmedicinebox.resources.poppins_semibold
import org.jetbrains.compose.resources.Font

// ==========================================
// SMARTMED COLORS
// ==========================================

internal val Bleu = Color(0xFF315F91)
internal val BleuFonce = Color(0xFF26394D)
internal val BleuClair = Color(0xFFE8EEF4)
internal val Fond = Color(0xFFF5EBDD)
internal val Ivoire = Color(0xFFFFFDF9)
internal val Texte = Color(0xFF26394D)
internal val Gris = Color(0xFF718096)
internal val Rouge = Color(0xFFE34B59)
internal val Vert = Color(0xFF269B70)
internal val VertClair = Color(0xFFE7F5EF)
internal val RougeClair = Color(0xFFFFEEEE)
internal val BordureChamp = Color(0xFFE2DED6)

// ==========================================
// POPPINS + THEME
// ==========================================

@Composable
private fun poppins() = FontFamily(
    Font(Res.font.poppins_regular, FontWeight.Normal),
    Font(Res.font.poppins_semibold, FontWeight.SemiBold),
    Font(Res.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun SmartMedTheme(content: @Composable () -> Unit) {
    val poppins = poppins()

    val typography = Typography(
        displayLarge = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 32.sp),
        headlineLarge = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 27.sp),
        headlineMedium = TextStyle(fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 22.sp),
        titleLarge = TextStyle(fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
        bodyLarge = TextStyle(fontFamily = poppins, fontSize = 15.sp),
        bodyMedium = TextStyle(fontFamily = poppins, fontSize = 13.sp),
        labelLarge = TextStyle(fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    )

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Bleu,
            onPrimary = Color.White,
            secondary = BleuFonce,
            background = Fond,
            onBackground = Texte,
            surface = Ivoire,
            onSurface = Texte
        ),
        typography = typography,
        content = content
    )
}
