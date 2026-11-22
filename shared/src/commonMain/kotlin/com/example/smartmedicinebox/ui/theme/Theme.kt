package com.example.smartmedicinebox.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.smartmedicinebox.domain.ThemeMode
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
// SMARTMED COLORS (light + dark)
// ==========================================

@Immutable
internal data class SmartMedColors(
    val bleu: Color,         // brand blue: buttons (white text on it), titles
    val bleuFonce: Color,
    val bleuClair: Color,    // icon tiles, chips, dividers
    val fond: Color,         // screen background
    val ivoire: Color,       // cards, bars, menus
    val texte: Color,
    val gris: Color,         // secondary text
    val rouge: Color,
    val vert: Color,         // green buttons (white text on it)
    val vertClair: Color,
    val rougeClair: Color,
    val bordureChamp: Color, // text field border
    val isDark: Boolean
)

private val LightColors = SmartMedColors(
    bleu = Color(0xFF315F91),
    bleuFonce = Color(0xFF26394D),
    bleuClair = Color(0xFFE8EEF4),
    fond = Color(0xFFF5EBDD),
    ivoire = Color(0xFFFFFDF9),
    texte = Color(0xFF26394D),
    gris = Color(0xFF718096),
    rouge = Color(0xFFE34B59),
    vert = Color(0xFF269B70),
    vertClair = Color(0xFFE7F5EF),
    rougeClair = Color(0xFFFFEEEE),
    bordureChamp = Color(0xFFE2DED6),
    isDark = false
)

private val DarkColors = SmartMedColors(
    bleu = Color(0xFF5A8FCB),
    bleuFonce = Color(0xFFB8CBE0),
    bleuClair = Color(0xFF243447),
    fond = Color(0xFF12171D),
    ivoire = Color(0xFF1C232B),
    texte = Color(0xFFE6EBF0),
    gris = Color(0xFF9AA7B6),
    rouge = Color(0xFFF0707C),
    vert = Color(0xFF248F68),
    vertClair = Color(0xFF17352A),
    rougeClair = Color(0xFF3A1F23),
    bordureChamp = Color(0xFF34404D),
    isDark = true
)

private val LocalSmartMedColors = staticCompositionLocalOf { LightColors }

// The screens use these names; they follow the current light / dark choice.
internal val Bleu: Color @Composable @ReadOnlyComposable get() = LocalSmartMedColors.current.bleu
internal val BleuFonce: Color @Composable @ReadOnlyComposable get() = LocalSmartMedColors.current.bleuFonce
internal val BleuClair: Color @Composable @ReadOnlyComposable get() = LocalSmartMedColors.current.bleuClair
internal val Fond: Color @Composable @ReadOnlyComposable get() = LocalSmartMedColors.current.fond
internal val Ivoire: Color @Composable @ReadOnlyComposable get() = LocalSmartMedColors.current.ivoire
internal val Texte: Color @Composable @ReadOnlyComposable get() = LocalSmartMedColors.current.texte
internal val Gris: Color @Composable @ReadOnlyComposable get() = LocalSmartMedColors.current.gris
internal val Rouge: Color @Composable @ReadOnlyComposable get() = LocalSmartMedColors.current.rouge
internal val Vert: Color @Composable @ReadOnlyComposable get() = LocalSmartMedColors.current.vert
internal val VertClair: Color @Composable @ReadOnlyComposable get() = LocalSmartMedColors.current.vertClair
internal val RougeClair: Color @Composable @ReadOnlyComposable get() = LocalSmartMedColors.current.rougeClair
internal val BordureChamp: Color @Composable @ReadOnlyComposable get() = LocalSmartMedColors.current.bordureChamp

/** True when the dark palette is showing. */
val isSmartMedDark: Boolean @Composable @ReadOnlyComposable get() = LocalSmartMedColors.current.isDark

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
fun SmartMedTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val c = if (dark) DarkColors else LightColors
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

    val base = if (dark) darkColorScheme() else lightColorScheme()
    CompositionLocalProvider(LocalSmartMedColors provides c) {
        MaterialTheme(
            colorScheme = base.copy(
                primary = c.bleu,
                onPrimary = Color.White,
                secondary = c.bleuFonce,
                background = c.fond,
                onBackground = c.texte,
                surface = c.ivoire,
                onSurface = c.texte,
                onSurfaceVariant = c.gris,
                surfaceContainer = c.ivoire,
                outline = c.bordureChamp,
                error = c.rouge
            ),
            typography = typography,
            content = content
        )
    }
}
