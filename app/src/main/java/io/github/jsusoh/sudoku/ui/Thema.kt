package io.github.jsusoh.sudoku.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Die Farben des Spielfelds, kräftig und gut unterscheidbar. */
@Immutable
data class FeldFarben(
    val hintergrund: Color,
    val linieDuenn: Color,
    val linieDick: Color,
    val vorgabe: Color,
    val eigene: Color,
    val fehler: Color,
    val notiz: Color,
    val auswahl: Color,
    val gleicheZahl: Color,
    val nachbar: Color,
    val fehlerFlaeche: Color,
)

private val hell = FeldFarben(
    hintergrund = Color(0xFFFFFFFF),
    linieDuenn = Color(0xFFB0B7C3),
    linieDick = Color(0xFF1F2937),
    vorgabe = Color(0xFF111827),
    eigene = Color(0xFF1E5AA8),
    fehler = Color(0xFFC62828),
    notiz = Color(0xFF4B5563),
    auswahl = Color(0xFFA7C7F2),
    gleicheZahl = Color(0xFFCFE0F7),
    nachbar = Color(0xFFEEF2F7),
    fehlerFlaeche = Color(0xFFFDE2E2),
)

private val dunkel = FeldFarben(
    hintergrund = Color(0xFF15181D),
    linieDuenn = Color(0xFF3B424D),
    linieDick = Color(0xFFC9CED6),
    vorgabe = Color(0xFFF3F4F6),
    eigene = Color(0xFF8AB8F5),
    fehler = Color(0xFFFF8A80),
    notiz = Color(0xFFB4BCC8),
    auswahl = Color(0xFF2F4F7E),
    gleicheZahl = Color(0xFF243957),
    nachbar = Color(0xFF1E232B),
    fehlerFlaeche = Color(0xFF4A2224),
)

val LocalFeldFarben = staticCompositionLocalOf { hell }

@Composable
fun SudokuThema(inhalt: @Composable () -> Unit) {
    val nacht = isSystemInDarkTheme()
    val schema = if (nacht) {
        darkColorScheme(
            primary = Color(0xFF8AB8F5),
            onPrimary = Color(0xFF0B2547),
            primaryContainer = Color(0xFF2F4F7E),
            onPrimaryContainer = Color(0xFFE3EDFC),
            background = Color(0xFF0F1115),
            surface = Color(0xFF0F1115),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF1E5AA8),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD6E4F8),
            onPrimaryContainer = Color(0xFF0B2547),
            background = Color(0xFFF6F7F9),
            surface = Color(0xFFF6F7F9),
        )
    }
    CompositionLocalProvider(LocalFeldFarben provides if (nacht) dunkel else hell) {
        MaterialTheme(colorScheme = schema, content = inhalt)
    }
}
