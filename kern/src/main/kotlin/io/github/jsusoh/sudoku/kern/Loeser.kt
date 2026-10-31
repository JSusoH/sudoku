package io.github.jsusoh.sudoku.kern

import kotlin.random.Random

/** Löst Sudokus durch Ausprobieren (Backtracking mit Bitmasken). */
object Loeser {

    /** Zählt die Lösungen von [werte] (0 = leer), höchstens bis [grenze]. */
    fun zaehle(werte: IntArray, grenze: Int = 2): Int {
        var anzahl = 0
        suche(werte, null) { anzahl++; anzahl >= grenze }
        return anzahl
    }

    /** Eine Lösung von [werte] oder null. Mit [zufall] in zufälliger Reihenfolge. */
    fun loese(werte: IntArray, zufall: Random? = null): IntArray? {
        var ergebnis: IntArray? = null
        suche(werte, zufall) { ergebnis = it.copyOf(); true }
        return ergebnis
    }

    /** Ruft [gefunden] für jede Lösung auf; gibt [gefunden] true zurück, endet die Suche. */
    private fun suche(werte: IntArray, zufall: Random?, gefunden: (IntArray) -> Boolean) {
        val z = werte.copyOf()
        val zeilen = IntArray(9)
        val spalten = IntArray(9)
        val bloecke = IntArray(9)
        for (i in 0 until 81) {
            val v = z[i]
            if (v == 0) continue
            val bit = 1 shl v
            val r = Raster.zeile[i]
            val c = Raster.spalte[i]
            val b = Raster.block[i]
            if ((zeilen[r] or spalten[c] or bloecke[b]) and bit != 0) return // widersprüchlich
            zeilen[r] = zeilen[r] or bit
            spalten[c] = spalten[c] or bit
            bloecke[b] = bloecke[b] or bit
        }

        fun schritt(): Boolean {
            var beste = -1
            var besteMaske = 0
            var besteZahl = 10
            for (i in 0 until 81) {
                if (z[i] != 0) continue
                val m = Raster.ALLE and
                    (zeilen[Raster.zeile[i]] or spalten[Raster.spalte[i]] or bloecke[Raster.block[i]]).inv()
                val n = m.countOneBits()
                if (n == 0) return false
                if (n < besteZahl) {
                    beste = i; besteMaske = m; besteZahl = n
                    if (n == 1) break
                }
            }
            if (beste == -1) return gefunden(z)

            val ziffern = (1..9).filter { besteMaske and (1 shl it) != 0 }
                .let { if (zufall != null) it.shuffled(zufall) else it }
            val r = Raster.zeile[beste]
            val c = Raster.spalte[beste]
            val b = Raster.block[beste]
            for (v in ziffern) {
                val bit = 1 shl v
                z[beste] = v
                zeilen[r] = zeilen[r] or bit
                spalten[c] = spalten[c] or bit
                bloecke[b] = bloecke[b] or bit
                if (schritt()) return true
                zeilen[r] = zeilen[r] and bit.inv()
                spalten[c] = spalten[c] and bit.inv()
                bloecke[b] = bloecke[b] and bit.inv()
            }
            z[beste] = 0
            return false
        }
        schritt()
    }
}

/** Löst nur mit einfacher Logik, wie ein Mensch: nackte und versteckte Einer. */
object Logik {

    /** true, wenn [werte] sich allein mit Einern vollständig lösen lässt. */
    fun nurMitEinern(werte: IntArray): Boolean {
        val z = werte.copyOf()
        var geaendert = true
        while (geaendert) {
            geaendert = false
            // Nackte Einer: In der Zelle passt nur noch eine Ziffer.
            for (i in 0 until 81) {
                if (z[i] != 0) continue
                val m = Raster.kandidaten(z, i)
                if (m == 0) return false
                if (m.countOneBits() == 1) {
                    z[i] = m.countTrailingZeroBits(); geaendert = true
                }
            }
            // Versteckte Einer: In einer Einheit passt eine Ziffer nur an eine Stelle.
            for (einheit in Raster.einheiten) {
                for (d in 1..9) {
                    var ort = -1
                    var anzahl = 0
                    var schonDa = false
                    for (i in einheit) {
                        if (z[i] == d) { schonDa = true; break }
                        if (z[i] == 0 && Raster.kandidaten(z, i) and (1 shl d) != 0) { ort = i; anzahl++ }
                    }
                    if (schonDa) continue
                    if (anzahl == 0) return false
                    if (anzahl == 1) { z[ort] = d; geaendert = true }
                }
            }
        }
        return z.none { it == 0 }
    }
}
