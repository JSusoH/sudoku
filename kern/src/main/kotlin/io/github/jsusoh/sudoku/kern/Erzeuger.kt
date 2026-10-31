package io.github.jsusoh.sudoku.kern

import kotlin.random.Random

enum class Stufe(val titel: String, val mindestVorgaben: Int, val nurEiner: Boolean) {
    /** Viele Vorgaben, mit einfacher Logik lösbar. */
    LEICHT("Leicht", 40, true),

    /** Weniger Vorgaben, aber immer noch ohne Raten lösbar. */
    MITTEL("Mittel", 28, true),

    /** So wenige Vorgaben wie möglich; braucht meist mehr als einfache Logik. */
    SCHWER("Schwer", 22, false),
}

class Raetsel(val vorgabe: IntArray, val loesung: IntArray, val stufe: Stufe)

object Erzeuger {

    fun erzeuge(stufe: Stufe, zufall: Random = Random.Default): Raetsel {
        var letztes: Raetsel? = null
        repeat(VERSUCHE_SCHWER) {
            val r = einmal(stufe, zufall)
            // Schwer soll nicht mit Einern allein lösbar sein; nach einigen Versuchen nehmen wir, was da ist.
            if (stufe != Stufe.SCHWER || !Logik.nurMitEinern(r.vorgabe)) return r
            letztes = r
        }
        return letztes!!
    }

    private fun einmal(stufe: Stufe, zufall: Random): Raetsel {
        val loesung = Loeser.loese(IntArray(81), zufall)!!
        val r = loesung.copyOf()
        var vorgaben = 81
        // Paarweise punktsymmetrisch entfernen (Zelle i und 80 − i), wie in gedruckten Rätseln.
        for (i in (0..40).shuffled(zufall)) {
            val j = 80 - i
            val weg = if (i == j) 1 else 2
            if (vorgaben - weg < stufe.mindestVorgaben) continue
            val a = r[i]
            val b = r[j]
            r[i] = 0
            r[j] = 0
            val gut = Loeser.zaehle(r, 2) == 1 && (!stufe.nurEiner || Logik.nurMitEinern(r))
            if (gut) vorgaben -= weg else { r[i] = a; r[j] = b }
        }
        return Raetsel(r, loesung, stufe)
    }

    private const val VERSUCHE_SCHWER = 6
}
