package io.github.jsusoh.sudoku.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import io.github.jsusoh.sudoku.SpielModell
import io.github.jsusoh.sudoku.kern.Spiel
import io.github.jsusoh.sudoku.kern.zeitText
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpielBildschirm(modell: SpielModell, spiel: Spiel) {
    BackHandler(onBack = modell::zumStart)

    // Die Uhr läuft nur, solange die App im Vordergrund ist.
    val lebenszyklus = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lebenszyklus) {
        lebenszyklus.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                delay(1000)
                modell.sekunde()
            }
        }
    }

    var menuOffen by remember { mutableStateOf(false) }
    var vonVornFrage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(spiel.stufe.titel) },
                navigationIcon = {
                    IconButton(onClick = modell::zumStart) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    Text(
                        zeitText(spiel.sekunden),
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 4.dp),
                    )
                    IconButton(onClick = { menuOffen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menü")
                    }
                    DropdownMenu(expanded = menuOffen, onDismissRequest = { menuOffen = false }) {
                        DropdownMenuItem(
                            text = { Text("Alle Notizen eintragen") },
                            onClick = { menuOffen = false; modell.alleNotizen() },
                        )
                        DropdownMenuItem(
                            text = { Text("Fehler sofort zeigen") },
                            trailingIcon = { Checkbox(checked = modell.fehlerZeigen, onCheckedChange = null) },
                            onClick = { modell.fehlerZeigenUmschalten() },
                        )
                        DropdownMenuItem(
                            text = { Text("Rätsel von vorn beginnen") },
                            onClick = { menuOffen = false; vonVornFrage = true },
                        )
                        DropdownMenuItem(
                            text = { Text("Neues Spiel") },
                            onClick = { menuOffen = false; modell.zumStart() },
                        )
                    }
                },
            )
        },
    ) { innen ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innen)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.widthIn(max = 560.dp)) {
                Spielfeld(
                    spiel = spiel,
                    auswahl = modell.auswahl,
                    fehlerZeigen = modell.fehlerZeigen,
                    onZelle = modell::waehle,
                )
            }
            Spacer(Modifier.weight(1f))
            Column(Modifier.widthIn(max = 560.dp)) {
                Werkzeuge(modell, spiel)
                Spacer(Modifier.height(12.dp))
                Ziffern(modell, spiel)
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (vonVornFrage) {
        AlertDialog(
            onDismissRequest = { vonVornFrage = false },
            title = { Text("Von vorn beginnen?") },
            text = { Text("Alle Eintragungen und Notizen werden entfernt. Mit „Rückgängig“ holst du sie zurück.") },
            confirmButton = {
                TextButton(onClick = { vonVornFrage = false; modell.vonVorn() }) { Text("Von vorn") }
            },
            dismissButton = {
                TextButton(onClick = { vonVornFrage = false }) { Text("Abbrechen") }
            },
        )
    }

    modell.sieg?.let { sieg ->
        AlertDialog(
            onDismissRequest = modell::siegGesehen,
            title = { Text("Geschafft! 🎉", fontSize = 26.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${sieg.stufe.titel} in ${zeitText(sieg.sekunden)} gelöst.", fontSize = 18.sp)
                    if (sieg.neueBestzeit) Text("Neue Bestzeit!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (sieg.tipps == 1) Text("Mit einem Tipp.", fontSize = 16.sp)
                    if (sieg.tipps > 1) Text("Mit ${sieg.tipps} Tipps.", fontSize = 16.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { modell.siegGesehen(); modell.zumStart() }) { Text("Neues Spiel") }
            },
            dismissButton = {
                TextButton(onClick = modell::siegGesehen) { Text("Ansehen") }
            },
        )
    }
}

/** Rückgängig, Löschen, Notizen, Tipp. */
@Composable
private fun Werkzeuge(modell: SpielModell, spiel: Spiel) {
    val aus = spiel.geloest
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Werkzeug("Rückgängig", aktiv = spiel.kannRueckgaengig, onClick = modell::rueckgaengig) {
            Text("↶", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
        Werkzeug("Löschen", aktiv = !aus && modell.auswahl != null, onClick = modell::loeschen) {
            Icon(Icons.Filled.Clear, contentDescription = null)
        }
        Werkzeug(
            if (modell.notizModus) "Notizen: an" else "Notizen: aus",
            aktiv = !aus,
            hervorgehoben = modell.notizModus,
            onClick = modell::notizModusUmschalten,
        ) {
            Icon(Icons.Filled.Edit, contentDescription = null)
        }
        Werkzeug("Tipp", aktiv = !aus, onClick = modell::tipp) {
            Text("💡", fontSize = 24.sp)
        }
    }
}

@Composable
private fun Werkzeug(
    text: String,
    aktiv: Boolean,
    hervorgehoben: Boolean = false,
    onClick: () -> Unit,
    symbol: @Composable () -> Unit,
) {
    val farbe = when {
        hervorgehoben -> MaterialTheme.colorScheme.onPrimary
        aktiv -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    }
    val flaeche = if (hervorgehoben) MaterialTheme.colorScheme.primary else Color.Transparent
    CompositionLocalProvider(LocalContentColor provides farbe) {
        Column(
            Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(flaeche)
                .clickable(enabled = aktiv, onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .widthIn(min = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.height(34.dp), contentAlignment = Alignment.Center) { symbol() }
            Text(text, fontSize = 14.sp, color = farbe)
        }
    }
}

/** Die Ziffern 1–9. Langes Drücken trägt eine Notiz ein, auch ohne Notizmodus. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Ziffern(modell: SpielModell, spiel: Spiel) {
    val haptik = LocalHapticFeedback.current
    val notiz = modell.notizModus
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (d in 1..9) {
            val rest = 9 - spiel.anzahl(d)
            val fertig = rest <= 0
            val rand = if (notiz) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (notiz) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    .border(if (notiz) 2.dp else 1.dp, rand, RoundedCornerShape(12.dp))
                    .combinedClickable(
                        enabled = !spiel.geloest,
                        onClick = { modell.ziffer(d) },
                        onLongClick = {
                            haptik.performHapticFeedback(HapticFeedbackType.LongPress)
                            modell.ziffer(d, alsNotiz = true)
                        },
                    )
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val farbe = if (fertig) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                else if (notiz) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.primary
                Text(
                    d.toString(),
                    fontSize = if (notiz) 24.sp else 32.sp,
                    fontWeight = if (notiz) FontWeight.Normal else FontWeight.SemiBold,
                    color = farbe,
                )
                Text(
                    if (fertig) "✓" else "$rest",
                    fontSize = 12.sp,
                    color = farbe,
                )
            }
        }
    }
}
