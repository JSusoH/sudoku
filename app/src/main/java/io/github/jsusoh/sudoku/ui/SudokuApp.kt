package io.github.jsusoh.sudoku.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.jsusoh.sudoku.Bildschirm
import io.github.jsusoh.sudoku.SpielModell

@Composable
fun SudokuApp(modell: SpielModell) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        val spiel = modell.spiel
        if (modell.bildschirm == Bildschirm.SPIEL && spiel != null) {
            SpielBildschirm(modell, spiel)
        } else {
            StartBildschirm(modell)
        }
    }
}
