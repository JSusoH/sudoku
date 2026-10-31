package io.github.jsusoh.sudoku.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import io.github.jsusoh.sudoku.SpielModell
import io.github.jsusoh.sudoku.kern.Stufe
import io.github.jsusoh.sudoku.kern.zeitText

@Composable
fun StartBildschirm(modell: SpielModell) {
    val laufend = modell.laufendesSpiel
    // Stufe, für die gefragt wird, ob das laufende Spiel aufgegeben werden soll.
    var nachfrage by remember { mutableStateOf<Stufe?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Sudoku", fontSize = 44.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        if (modell.erzeugtGerade) {
            CircularProgressIndicator(Modifier.size(48.dp))
            Text("Rätsel wird erstellt …", style = MaterialTheme.typography.titleMedium)
            return@Column
        }

        if (laufend != null) {
            Button(
                onClick = modell::weiterspielen,
                modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Weiterspielen", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    Text("${laufend.stufe.titel} · ${zeitText(laufend.sekunden)}", fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Text("Neues Spiel", style = MaterialTheme.typography.titleLarge)
        for (stufe in Stufe.entries) {
            val bilanz = modell.bilanzen.getValue(stufe)
            OutlinedButton(
                onClick = { if (laufend != null) nachfrage = stufe else modell.neuesSpiel(stufe) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stufe.titel, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    val zeile = when {
                        bilanz.geloest == 0 -> "noch keins gelöst"
                        bilanz.bestzeit != null -> "${bilanz.geloest}× gelöst · Bestzeit ${zeitText(bilanz.bestzeit)}"
                        else -> "${bilanz.geloest}× gelöst"
                    }
                    Text(zeile, fontSize = 15.sp)
                }
            }
        }
    }

    nachfrage?.let { stufe ->
        AlertDialog(
            onDismissRequest = { nachfrage = null },
            title = { Text("Neues Spiel beginnen?") },
            text = { Text("Das angefangene Spiel geht dabei verloren.") },
            confirmButton = {
                TextButton(onClick = { nachfrage = null; modell.neuesSpiel(stufe) }) {
                    Text("Neues Spiel")
                }
            },
            dismissButton = {
                TextButton(onClick = { nachfrage = null }) { Text("Abbrechen") }
            },
        )
    }
}
