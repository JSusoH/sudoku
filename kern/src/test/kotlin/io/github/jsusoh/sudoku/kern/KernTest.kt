package io.github.jsusoh.sudoku.kern

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class KernTest {

    private fun gueltigeLoesung(w: IntArray) =
        w.all { it in 1..9 } && Raster.einheiten.all { e -> e.map { w[it] }.toSet().size == 9 }

    @Test
    fun rasterStimmt() {
        assertTrue(Raster.nachbarn.all { it.size == 20 })
        assertTrue(Raster.einheiten.all { it.toSet().size == 9 })
        assertEquals((0 until 81).toList(), Raster.einheiten.flatMap { it.toList() }.groupBy { it }
            .filter { it.value.size == 3 }.keys.sorted())
    }

    @Test
    fun loeserFindetLoesungUndZaehlt() {
        val leer = IntArray(81)
        val l = Loeser.loese(leer, Random(1))
        assertNotNull(l)
        assertTrue(gueltigeLoesung(l))
        assertEquals(2, Loeser.zaehle(leer, 2))
        assertEquals(1, Loeser.zaehle(l, 2))
        val kaputt = l.copyOf().also { it[0] = it[1] }
        assertEquals(0, Loeser.zaehle(kaputt))
        assertNull(Loeser.loese(kaputt))
    }

    @Test
    fun erzeugteRaetselSindEindeutigUndPassen() {
        for (stufe in Stufe.entries) {
            repeat(5) { n ->
                val zeit = System.currentTimeMillis()
                val r = Erzeuger.erzeuge(stufe, Random(100 + n))
                val dauer = System.currentTimeMillis() - zeit
                assertTrue(gueltigeLoesung(r.loesung))
                assertEquals(1, Loeser.zaehle(r.vorgabe, 2), "$stufe nicht eindeutig")
                assertTrue(r.vorgabe.indices.all { r.vorgabe[it] == 0 || r.vorgabe[it] == r.loesung[it] })
                val vorgaben = r.vorgabe.count { it != 0 }
                assertTrue(vorgaben >= stufe.mindestVorgaben)
                if (stufe.nurEiner) assertTrue(Logik.nurMitEinern(r.vorgabe), "$stufe braucht mehr als Einer")
                println("$stufe: $vorgaben Vorgaben, ${dauer} ms, nur Einer: ${Logik.nurMitEinern(r.vorgabe)}")
            }
        }
    }

    private fun spiel(): Spiel = Spiel.neu(Erzeuger.erzeuge(Stufe.LEICHT, Random(7)))

    @Test
    fun setzenStreichtNotizenDerNachbarn() {
        var s = spiel()
        val leer = (0 until 81).filter { s.werte[it] == 0 }
        val a = leer.first()
        val nachbar = leer.first { it in Raster.nachbarn[a] }
        val fern = leer.first { it != a && it !in Raster.nachbarn[a] }
        s = s.notiz(nachbar, 5).notiz(nachbar, 3).notiz(fern, 5).notiz(a, 5)
        assertTrue(s.hatNotiz(a, 5))
        s = s.setze(a, 5)
        assertEquals(5, s.werte[a])
        assertEquals(0, s.notizen[a])
        assertFalse(s.hatNotiz(nachbar, 5))
        assertTrue(s.hatNotiz(nachbar, 3))
        assertTrue(s.hatNotiz(fern, 5))
        // Noch einmal dieselbe Zahl: Zelle wird leer.
        assertEquals(0, s.setze(a, 5).werte[a])
    }

    @Test
    fun vorgabenBleibenUndNotizNurInLeereZellen() {
        val s = spiel()
        val vorgabe = (0 until 81).first { s.istVorgabe(it) }
        assertSame(s, s.setze(vorgabe, 1))
        assertSame(s, s.loesche(vorgabe))
        assertSame(s, s.notiz(vorgabe, 1))
        val leer = (0 until 81).first { s.werte[it] == 0 }
        val belegt = s.setze(leer, 4)
        assertSame(belegt, belegt.notiz(leer, 2))
    }

    @Test
    fun rueckgaengigNimmtSchrittFuerSchrittZurueck() {
        val s0 = spiel()
        val leer = (0 until 81).filter { s0.werte[it] == 0 }
        val s3 = s0.notiz(leer[0], 1).setze(leer[1], 2).loesche(leer[1])
        assertTrue(s3.kannRueckgaengig)
        val s2 = s3.rueckgaengig()
        assertEquals(2, s2.werte[leer[1]])
        val s0b = s2.rueckgaengig().rueckgaengig()
        assertContentEquals(s0.werte, s0b.werte)
        assertContentEquals(s0.notizen, s0b.notizen)
        assertFalse(s0b.kannRueckgaengig)
    }

    @Test
    fun konfliktUndFehler() {
        val s = spiel()
        val leer = (0 until 81).first { s.werte[it] == 0 }
        val nachbarWert = Raster.nachbarn[leer].map { s.werte[it] }.first { it != 0 }
        val k = s.setze(leer, nachbarWert)
        assertTrue(k.konflikt(leer))
        assertTrue(k.falsch(leer))
        val richtig = s.setze(leer, s.loesung[leer])
        assertFalse(richtig.konflikt(leer))
        assertFalse(richtig.falsch(leer))
    }

    @Test
    fun alleNotizenUndTippsFuehrenZurLoesung() {
        var s = spiel().alleNotizen()
        val leer = (0 until 81).filter { s.werte[it] == 0 }
        assertTrue(leer.all { s.notizen[it] != 0 && s.hatNotiz(it, s.loesung[it]) })
        var tipps = 0
        while (!s.geloest) {
            s = s.tipp(null).first
            tipps++
        }
        assertEquals(leer.size, tipps)
        assertEquals(tipps, s.tipps)
        assertTrue(s.notizen.all { it == 0 })
        // Gelöst: nichts mehr änderbar.
        assertSame(s, s.loesche(leer.first()))
    }

    @Test
    fun tippKorrigiertGewaehlteZelle() {
        val s = spiel()
        val leer = (0 until 81).first { s.werte[it] == 0 }
        val falscheZahl = (1..9).first { it != s.loesung[leer] }
        val (neu, zelle) = s.setze(leer, falscheZahl).tipp(leer)
        assertEquals(leer, zelle)
        assertEquals(s.loesung[leer], neu.werte[leer])
    }

    @Test
    fun speichernUndLaden() {
        val leer = (0 until 81).filter { spiel().werte[it] == 0 }
        val s = spiel().notiz(leer[0], 3).notiz(leer[0], 9).setze(leer[1], 4).mitSekunden(754)
        val l = Spiel.dekodieren(s.kodieren())
        assertNotNull(l)
        assertEquals(s.stufe, l.stufe)
        assertContentEquals(s.vorgabe, l.vorgabe)
        assertContentEquals(s.loesung, l.loesung)
        assertContentEquals(s.werte, l.werte)
        assertContentEquals(s.notizen, l.notizen)
        assertEquals(754, l.sekunden)
        assertNull(Spiel.dekodieren("Unsinn"))
        assertNull(Spiel.dekodieren(""))
    }

    @Test
    fun zeitAlsText() {
        assertEquals("0:05", zeitText(5))
        assertEquals("12:34", zeitText(754))
        assertEquals("1:02:03", zeitText(3723))
    }
}
