package io.github.jsusoh.sudoku.kern

/** Aufbau des 9×9-Felds: Zellen 0..80, zeilenweise von links oben. */
object Raster {
    /** Bits 1..9 gesetzt: alle Ziffern möglich. */
    const val ALLE = 0x3FE

    val zeile = IntArray(81) { it / 9 }
    val spalte = IntArray(81) { it % 9 }
    val block = IntArray(81) { (it / 27) * 3 + (it % 9) / 3 }

    /** Die 20 Nachbarn jeder Zelle: gleiche Zeile, Spalte oder gleicher Block. */
    val nachbarn: Array<IntArray> = Array(81) { z ->
        (0 until 81).filter {
            it != z && (zeile[it] == zeile[z] || spalte[it] == spalte[z] || block[it] == block[z])
        }.toIntArray()
    }

    /** Die 27 Einheiten: 9 Zeilen, 9 Spalten, 9 Blöcke. */
    val einheiten: Array<IntArray> = Array(27) { e ->
        when {
            e < 9 -> IntArray(9) { e * 9 + it }
            e < 18 -> IntArray(9) { it * 9 + (e - 9) }
            else -> {
                val b = e - 18
                IntArray(9) { (b / 3) * 27 + (b % 3) * 3 + (it / 3) * 9 + it % 3 }
            }
        }
    }

    /** Ziffern, die in Zelle [z] nach den eingetragenen [werte]n noch möglich sind (Bitmaske). */
    fun kandidaten(werte: IntArray, z: Int): Int {
        var belegt = 0
        for (n in nachbarn[z]) belegt = belegt or (1 shl werte[n])
        return ALLE and belegt.inv()
    }
}
