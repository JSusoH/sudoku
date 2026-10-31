package io.github.jsusoh.sudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.jsusoh.sudoku.kern.Raster
import io.github.jsusoh.sudoku.kern.Spiel

@Composable
fun Spielfeld(
    spiel: Spiel,
    auswahl: Int?,
    fehlerZeigen: Boolean,
    onZelle: (Int) -> Unit,
) {
    val f = LocalFeldFarben.current
    val gewaehlterWert = auswahl?.let { spiel.werte[it] } ?: 0

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(f.hintergrund),
    ) {
        val zelle = maxWidth / 9
        Column {
            for (r in 0 until 9) {
                Row {
                    for (c in 0 until 9) {
                        val z = r * 9 + c
                        val wert = spiel.werte[z]
                        val rot = spiel.konflikt(z) || (fehlerZeigen && spiel.falsch(z))
                        val flaeche = when {
                            z == auswahl -> f.auswahl
                            rot && !spiel.istVorgabe(z) -> f.fehlerFlaeche
                            gewaehlterWert != 0 && wert == gewaehlterWert -> f.gleicheZahl
                            auswahl != null && z in Raster.nachbarn[auswahl] -> f.nachbar
                            else -> f.hintergrund
                        }
                        Zelle(
                            spiel = spiel,
                            z = z,
                            groesse = zelle,
                            flaeche = flaeche,
                            rot = rot,
                            hervorNotiz = gewaehlterWert,
                            onClick = { onZelle(z) },
                        )
                    }
                }
            }
        }
        // Gitterlinien: dünn zwischen den Zellen, dick um die Blöcke.
        Canvas(Modifier.fillMaxSize()) {
            val schritt = size.width / 9
            val duenn = 1.dp.toPx()
            val dick = 3.dp.toPx()
            for (i in 0..9) {
                if (i % 3 == 0) continue
                val p = i * schritt
                drawLine(f.linieDuenn, Offset(p, 0f), Offset(p, size.height), duenn)
                drawLine(f.linieDuenn, Offset(0f, p), Offset(size.width, p), duenn)
            }
            for (i in 0..9 step 3) {
                val p = (i * schritt).coerceIn(dick / 2, size.width - dick / 2)
                drawLine(f.linieDick, Offset(p, 0f), Offset(p, size.height), dick)
                drawLine(f.linieDick, Offset(0f, p), Offset(size.width, p), dick)
            }
        }
    }
}

@Composable
private fun Zelle(
    spiel: Spiel,
    z: Int,
    groesse: Dp,
    flaeche: androidx.compose.ui.graphics.Color,
    rot: Boolean,
    hervorNotiz: Int,
    onClick: () -> Unit,
) {
    val f = LocalFeldFarben.current
    val dichte = LocalDensity.current
    val wert = spiel.werte[z]
    val vorgabe = spiel.istVorgabe(z)
    val beschreibung = "Zeile ${z / 9 + 1}, Spalte ${z % 9 + 1}: " + when {
        wert != 0 -> "$wert"
        spiel.notizen[z] != 0 -> "Notizen " + (1..9).filter { spiel.hatNotiz(z, it) }.joinToString(", ")
        else -> "leer"
    }
    Box(
        Modifier
            .size(groesse)
            .background(flaeche)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics { contentDescription = beschreibung },
        contentAlignment = Alignment.Center,
    ) {
        if (wert != 0) {
            // Feste Größe, unabhängig von der Schriftgröße des Systems, damit nichts überläuft.
            val schrift = with(dichte) { (groesse * 0.64f).toSp() }
            Text(
                wert.toString(),
                fontSize = schrift,
                fontWeight = if (vorgabe) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    vorgabe -> f.vorgabe
                    rot -> f.fehler
                    else -> f.eigene
                },
            )
        } else if (spiel.notizen[z] != 0) {
            val schrift = with(dichte) { (groesse * 0.27f).toSp() }
            Column(Modifier.fillMaxSize()) {
                for (zr in 0 until 3) {
                    Row(Modifier.weight(1f)) {
                        for (zc in 0 until 3) {
                            val d = zr * 3 + zc + 1
                            val zeigen = spiel.hatNotiz(z, d)
                            val hervor = zeigen && d == hervorNotiz
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(if (hervor) f.gleicheZahl else androidx.compose.ui.graphics.Color.Transparent),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (zeigen) {
                                    Text(
                                        d.toString(),
                                        fontSize = schrift,
                                        fontWeight = if (hervor) FontWeight.Bold else FontWeight.Normal,
                                        color = if (hervor) f.eigene else f.notiz,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
