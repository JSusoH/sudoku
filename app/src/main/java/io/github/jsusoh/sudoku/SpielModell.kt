package io.github.jsusoh.sudoku

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.jsusoh.sudoku.kern.Erzeuger
import io.github.jsusoh.sudoku.kern.Spiel
import io.github.jsusoh.sudoku.kern.Stufe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Bildschirm { START, SPIEL }

/** Bestzeit und Zahl der gelösten Rätsel je Stufe. */
data class Bilanz(val geloest: Int, val bestzeit: Int?)

/** Was nach dem Lösen im Glückwunsch steht. */
data class Sieg(val stufe: Stufe, val sekunden: Int, val tipps: Int, val neueBestzeit: Boolean)

class SpielModell(app: Application) : AndroidViewModel(app) {

    private val speicher = app.getSharedPreferences("sudoku", Context.MODE_PRIVATE)

    var spiel by mutableStateOf(speicher.getString(SPIEL, null)?.let(Spiel::dekodieren))
        private set
    var bildschirm by mutableStateOf(Bildschirm.START)
        private set
    var auswahl by mutableStateOf<Int?>(null)
        private set
    var notizModus by mutableStateOf(false)
        private set
    var fehlerZeigen by mutableStateOf(speicher.getBoolean(FEHLER_ZEIGEN, false))
        private set
    var erzeugtGerade by mutableStateOf(false)
        private set
    var sieg by mutableStateOf<Sieg?>(null)
        private set
    var bilanzen by mutableStateOf(bilanzenLesen())
        private set

    /** Ein Spiel, das man weiterspielen kann. */
    val laufendesSpiel: Spiel? get() = spiel?.takeIf { !it.geloest }

    fun neuesSpiel(stufe: Stufe) {
        if (erzeugtGerade) return
        erzeugtGerade = true
        viewModelScope.launch {
            val r = withContext(Dispatchers.Default) { Erzeuger.erzeuge(stufe) }
            spiel = Spiel.neu(r)
            auswahl = null
            notizModus = false
            sieg = null
            erzeugtGerade = false
            bildschirm = Bildschirm.SPIEL
            speichern()
        }
    }

    fun weiterspielen() {
        if (laufendesSpiel != null) bildschirm = Bildschirm.SPIEL
    }

    fun zumStart() {
        speichern()
        bildschirm = Bildschirm.START
    }

    fun waehle(z: Int) {
        auswahl = if (auswahl == z) null else z
    }

    fun ziffer(d: Int, alsNotiz: Boolean = notizModus) {
        val z = auswahl ?: return
        val s = spiel ?: return
        aendern(if (alsNotiz) s.notiz(z, d) else s.setze(z, d))
    }

    fun loeschen() {
        val z = auswahl ?: return
        spiel?.let { aendern(it.loesche(z)) }
    }

    fun rueckgaengig() {
        spiel?.let { aendern(it.rueckgaengig()) }
    }

    fun tipp() {
        val s = spiel ?: return
        val (neu, zelle) = s.tipp(auswahl)
        if (zelle != null) auswahl = zelle
        aendern(neu)
    }

    fun alleNotizen() {
        spiel?.let { aendern(it.alleNotizen()) }
    }

    fun vonVorn() {
        spiel?.let { aendern(it.vonVorn()) }
    }

    fun notizModusUmschalten() {
        notizModus = !notizModus
    }

    fun fehlerZeigenUmschalten() {
        fehlerZeigen = !fehlerZeigen
        speicher.edit { putBoolean(FEHLER_ZEIGEN, fehlerZeigen) }
    }

    fun siegGesehen() {
        sieg = null
    }

    /** Läuft jede Sekunde, solange das Spiel sichtbar ist. */
    fun sekunde() {
        val s = spiel ?: return
        if (s.geloest || bildschirm != Bildschirm.SPIEL || sieg != null) return
        spiel = s.mitSekunden(s.sekunden + 1)
        if (s.sekunden % 5 == 0) speichern()
    }

    fun speichern() {
        speicher.edit { putString(SPIEL, spiel?.kodieren()) }
    }

    private fun aendern(neu: Spiel) {
        val vorher = spiel ?: return
        if (neu === vorher) return
        spiel = neu
        if (!vorher.geloest && neu.geloest) gewonnen(neu)
        speichern()
    }

    private fun gewonnen(s: Spiel) {
        auswahl = null
        notizModus = false
        val alt = bilanzen.getValue(s.stufe)
        // Mit Tipps zählt das Rätsel als gelöst, aber nicht für die Bestzeit.
        val neueBestzeit = s.tipps == 0 && (alt.bestzeit == null || s.sekunden < alt.bestzeit)
        speicher.edit {
            putInt(GELOEST + s.stufe.name, alt.geloest + 1)
            if (neueBestzeit) putInt(BESTZEIT + s.stufe.name, s.sekunden)
        }
        bilanzen = bilanzenLesen()
        sieg = Sieg(s.stufe, s.sekunden, s.tipps, neueBestzeit)
    }

    private fun bilanzenLesen(): Map<Stufe, Bilanz> = Stufe.entries.associateWith {
        Bilanz(
            geloest = speicher.getInt(GELOEST + it.name, 0),
            bestzeit = speicher.getInt(BESTZEIT + it.name, -1).takeIf { t -> t >= 0 },
        )
    }

    private companion object {
        const val SPIEL = "spiel"
        const val FEHLER_ZEIGEN = "fehlerZeigen"
        const val GELOEST = "geloest_"
        const val BESTZEIT = "bestzeit_"
    }
}
