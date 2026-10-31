package io.github.jsusoh.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import io.github.jsusoh.sudoku.ui.SudokuApp
import io.github.jsusoh.sudoku.ui.SudokuThema

class MainActivity : ComponentActivity() {

    private val modell: SpielModell by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SudokuThema { SudokuApp(modell) }
        }
    }

    override fun onStop() {
        modell.speichern()
        super.onStop()
    }
}
