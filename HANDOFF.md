# Handoff — Sudoku (App für Johannes' Mutter)
Stand: 1.11.2026 · Claude Code (Cloud-Sitzung)

## Zuerst lesen
- `README.md` — Funktionen, Installation, Aufbau (kern/app). Eine `CLAUDE.md` gibt es nicht.

## Stand
Erste Version fertig und von GitHub Actions gebaut (Lauf 3 grün): Android-App (Kotlin, Jetpack Compose),
nur Deutsch, drei Stufen, Notizen, Rückgängig, Tipp, Speichern, Bestzeiten. Die APK liegt als Release unter
https://github.com/JSusoH/sudoku/releases/latest/download/sudoku.apk. Alles im Zweig
`claude/epic-ramanujan-1dx47g`; `main` hat nur das erste README. Auf einem Handy hat die App noch NIEMAND gesehen.

## Nächster Schritt
Rückmeldung von Johannes' Mutter zur Bedienung abholen und danach anpassen.

## Entscheidungen
- Logik in `kern/` (reines Kotlin, ohne Android) — weil sie sich so überall testen lässt, auch ohne Android-SDK.
- Fester Signierschlüssel im Repo (`app/sudoku.keystore`, Passwort in `app/build.gradle.kts`) — damit jede neue
  APK sich über die alte installieren lässt und der Spielstand bleibt. Nicht für den Play Store.
- `versionCode` = `GITHUB_RUN_NUMBER` — damit Updates immer höher zählen.
- Release auch aus dem Arbeitszweig (`.github/workflows/app-bauen.yml`) — Johannes wollte einen Link ohne ZIP.
- Mittel: Untergrenze 28 Vorgaben (war 30, wurde zu leicht erreicht); Leicht 40; Schwer 22, soll mehr als Einer brauchen.
- Bewusst schlicht: kein R8/Minify, nur Icons aus `material-icons-core` (extended wäre ohne Minify riesig).

## Offen
- Nach `main` übernehmen? Nur auf Johannes' Wort; dann den Arbeitszweig aus der Release-Bedingung nehmen.
- Das Repo ist öffentlich (API ohne Anmeldung gab 200), der Schlüssel liegt also offen. Johannes wurde geraten, es
  privat zu stellen — dann braucht der Release-Link aber eine Anmeldung. Ungeklärt.
- Release-Tags zeigen auf `main` statt auf den gebauten Commit (`target_commitish: main` beim Release).
  Abhilfe: `target_commitish: ${{ github.sha }}` in der Workflow-Datei.
- „Schwer" ist in etwa jedem fünften Fall doch nur mit Einern lösbar (nach 6 Versuchen nimmt der Erzeuger das letzte).

## Sackgassen
- In der Cloud-Umgebung blockt der Proxy `dl.google.com`: kein Android-SDK, kein AGP → App nur in GitHub Actions
  baubar. `settings.gradle.kts`/`build.gradle.kts` binden `:app` deshalb nur mit `ANDROID_HOME` oder `local.properties` ein.
- `maven.google.com` leitet auf `dl.google.com` um, hilft also nicht.
- Artefakte herunterladen geht aus der Umgebung nicht (`*.blob.core.windows.net` gesperrt) — APK nicht in den Chat holbar.
- Repo anlegen darf Claude in diesen Sitzungen nicht (`create_repository` gesperrt); Johannes legt an, Claude hängt per `add_repo` an.
- Maven Central antwortet gelegentlich mit 429 — einfach noch einmal bauen, kein Code-Fehler.
- Dateien unter `.kotlin/` nie committen (steht inzwischen in `.gitignore`).

## Dateien
- `kern/src/main/kotlin/.../kern/` — `Raster`, `Loeser` (+ `Logik`), `Erzeuger` (Stufen), `Spiel` (Zustand, Speichern)
- `kern/src/test/.../KernTest.kt` — Tests: `./gradlew :kern:test`
- `app/src/main/java/.../SpielModell.kt` — ViewModel, SharedPreferences, Statistik
- `app/src/main/java/.../ui/` — `Start`, `SpielBildschirm` (Werkzeuge, Ziffern, Dialoge), `Spielfeld`, `Thema`
- `.github/workflows/app-bauen.yml` — Tests, APK, Artefakt, Release
