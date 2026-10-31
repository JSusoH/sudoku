# Sudoku

Eine schlichte Sudoku-App für Android, ohne Werbung, ohne Konto, ohne Internet.

## Was sie kann

- Drei Stufen: **Leicht**, **Mittel** (beide ohne Raten lösbar), **Schwer**
- Jedes Rätsel hat genau eine Lösung und wird neu erzeugt
- **Notizen:** Mit „Notizen: an“ trägt man kleine Kandidaten statt fester Zahlen ein.
  Langes Drücken auf eine Ziffer trägt eine Notiz ein, auch ohne den Notizmodus.
- Notizen räumen sich selbst auf: Eine feste 5 streicht die Notiz-5en in Zeile, Spalte und Block.
- Gleiche Zahlen und passende Notizen werden hervorgehoben, Doppelte rot markiert
- „Fehler sofort zeigen“ (im Menü, abschaltbar), „Alle Notizen eintragen“, Tipp, unbegrenztes Rückgängig
- Unter jeder Ziffer steht, wie oft sie noch fehlt
- Das Spiel bleibt gespeichert; Bestzeit und Zahl der gelösten Rätsel je Stufe

## Installieren

Bei jedem Push baut GitHub Actions die APK (Reiter **Actions** → letzter Lauf → **sudoku-apk**).
Auf `main` erscheint sie zusätzlich als Release, immer unter demselben Link:

https://github.com/JSusoH/sudoku/releases/latest/download/sudoku.apk

Auf dem Handy öffnen, „Installation aus unbekannten Quellen“ einmal erlauben, installieren.
Neue Versionen lassen sich einfach drüber installieren, der Spielstand bleibt.

## Aufbau

| Teil | Inhalt |
|---|---|
| `kern/` | Spiellogik in reinem Kotlin: Erzeuger, Löser, Spielstand. Mit Tests (`./gradlew :kern:test`). |
| `app/` | Android-App mit Jetpack Compose |

Die App wird mit einem festen Schlüssel signiert (`app/sudoku.keystore`), damit Updates über die alte
Version passen. Für den Play Store bräuchte es einen eigenen, geheimen Schlüssel.
