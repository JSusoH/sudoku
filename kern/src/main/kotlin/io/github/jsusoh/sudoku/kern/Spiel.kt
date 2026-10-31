package io.github.jsusoh.sudoku.kern

/**
 * Ein laufendes Spiel. Unveränderlich: Jede Handlung gibt ein neues Spiel zurück
 * (oder dasselbe, wenn sich nichts ändert).
 *
 * [notizen]: je Zelle eine Bitmaske der vorgemerkten Ziffern (Bit 1..9).
 */
class Spiel(
    val stufe: Stufe,
    val vorgabe: IntArray,
    val loesung: IntArray,
    val werte: IntArray,
    val notizen: IntArray,
    val sekunden: Int = 0,
    val tipps: Int = 0,
    private val verlauf: List<Stand> = emptyList(),
) {
    class Stand(val werte: IntArray, val notizen: IntArray)

    val geloest: Boolean get() = werte.contentEquals(loesung)
    val kannRueckgaengig: Boolean get() = verlauf.isNotEmpty()

    fun istVorgabe(z: Int) = vorgabe[z] != 0

    /** Die Zahl steht schon in Zeile, Spalte oder Block. */
    fun konflikt(z: Int): Boolean {
        val v = werte[z]
        return v != 0 && Raster.nachbarn[z].any { werte[it] == v }
    }

    /** Die Zahl stimmt nicht mit der Lösung überein. */
    fun falsch(z: Int) = werte[z] != 0 && werte[z] != loesung[z]

    /** Wie oft die Ziffer [d] schon im Feld steht. */
    fun anzahl(d: Int) = werte.count { it == d }

    fun hatNotiz(z: Int, d: Int) = notizen[z] and (1 shl d) != 0

    /** Trägt [d] fest ein. Steht [d] schon dort, wird die Zelle wieder leer. */
    fun setze(z: Int, d: Int): Spiel {
        if (istVorgabe(z) || geloest) return this
        val w = werte.copyOf()
        val n = notizen.copyOf()
        if (w[z] == d) {
            w[z] = 0
        } else {
            w[z] = d
            n[z] = 0
            // Die Ziffer aus den Notizen der Nachbarn streichen.
            for (x in Raster.nachbarn[z]) n[x] = n[x] and (1 shl d).inv()
        }
        return weiter(w, n)
    }

    /** Merkt [d] in der Zelle vor oder nimmt die Notiz wieder weg. */
    fun notiz(z: Int, d: Int): Spiel {
        if (istVorgabe(z) || werte[z] != 0 || geloest) return this
        val n = notizen.copyOf()
        n[z] = n[z] xor (1 shl d)
        return weiter(werte, n)
    }

    /** Leert die Zelle: erst die Zahl, sonst die Notizen. */
    fun loesche(z: Int): Spiel {
        if (istVorgabe(z) || geloest) return this
        return when {
            werte[z] != 0 -> weiter(werte.copyOf().also { it[z] = 0 }, notizen)
            notizen[z] != 0 -> weiter(werte, notizen.copyOf().also { it[z] = 0 })
            else -> this
        }
    }

    fun rueckgaengig(): Spiel {
        val letzter = verlauf.lastOrNull() ?: return this
        return Spiel(stufe, vorgabe, loesung, letzter.werte, letzter.notizen, sekunden, tipps, verlauf.dropLast(1))
    }

    /** Trägt in jede leere Zelle alle Ziffern ein, die dort noch möglich sind. */
    fun alleNotizen(): Spiel {
        if (geloest) return this
        val n = IntArray(81) { z -> if (werte[z] == 0) Raster.kandidaten(werte, z) else 0 }
        if (n.contentEquals(notizen)) return this
        return weiter(werte, n)
    }

    /** Setzt das Rätsel auf den Anfang zurück (rückgängig machbar). */
    fun vonVorn(): Spiel {
        if (werte.contentEquals(vorgabe) && notizen.all { it == 0 }) return this
        return weiter(vorgabe.copyOf(), IntArray(81))
    }

    /**
     * Ein Tipp: die richtige Zahl in die gewählte Zelle, wenn sie leer oder falsch ist,
     * sonst in die leere Zelle mit den wenigsten Möglichkeiten. Gibt die Zelle mit zurück.
     */
    fun tipp(auswahl: Int?): Pair<Spiel, Int?> {
        if (geloest) return this to null
        val ziel = auswahl?.takeIf { !istVorgabe(it) && werte[it] != loesung[it] }
            ?: werte.indices.firstOrNull { falsch(it) }
            ?: werte.indices.filter { werte[it] == 0 }
                .minByOrNull { Raster.kandidaten(werte, it).countOneBits() }
            ?: return this to null
        val d = loesung[ziel]
        val w = werte.copyOf().also { it[ziel] = d }
        val n = notizen.copyOf().also { it[ziel] = 0 }
        for (x in Raster.nachbarn[ziel]) n[x] = n[x] and (1 shl d).inv()
        val neu = weiter(w, n)
        return Spiel(stufe, vorgabe, loesung, neu.werte, neu.notizen, sekunden, tipps + 1, neu.verlauf) to ziel
    }

    fun mitSekunden(s: Int) = Spiel(stufe, vorgabe, loesung, werte, notizen, s, tipps, verlauf)

    private fun weiter(w: IntArray, n: IntArray): Spiel {
        val v = (verlauf + Stand(werte, notizen)).takeLast(VERLAUF_MAX)
        return Spiel(stufe, vorgabe, loesung, w, n, sekunden, tipps, v)
    }

    /** Als eine Zeile Text zum Speichern (ohne Verlauf). */
    fun kodieren(): String = listOf(
        "v1",
        stufe.name,
        vorgabe.joinToString(""),
        loesung.joinToString(""),
        werte.joinToString(""),
        notizen.joinToString(","),
        sekunden.toString(),
        tipps.toString(),
    ).joinToString(";")

    companion object {
        const val VERLAUF_MAX = 1000

        fun neu(r: Raetsel) = Spiel(r.stufe, r.vorgabe, r.loesung, r.vorgabe.copyOf(), IntArray(81))

        /** Liest ein gespeichertes Spiel; null, wenn der Text nicht passt. */
        fun dekodieren(text: String): Spiel? = runCatching {
            val t = text.split(";")
            require(t.size == 8 && t[0] == "v1")
            fun ziffern(s: String) = IntArray(81) { s[it].digitToInt() }.also { require(s.length == 81) }
            val notizen = t[5].split(",").map { it.toInt() }.toIntArray()
            require(notizen.size == 81)
            Spiel(
                Stufe.valueOf(t[1]), ziffern(t[2]), ziffern(t[3]), ziffern(t[4]), notizen,
                t[6].toInt(), t[7].toInt(),
            )
        }.getOrNull()
    }
}

/** 754 Sekunden → „12:34", ab einer Stunde „1:02:34". */
fun zeitText(sekunden: Int): String {
    val h = sekunden / 3600
    val m = sekunden / 60 % 60
    val s = sekunden % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
